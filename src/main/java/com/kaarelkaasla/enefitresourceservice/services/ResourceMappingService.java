package com.kaarelkaasla.enefitresourceservice.services;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.entities.Characteristic;
import com.kaarelkaasla.enefitresourceservice.entities.Location;
import com.kaarelkaasla.enefitresourceservice.entities.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps between JPA entities and API DTOs for resources.
 * Ensures location.countryCode aligns with resource.countryCode, patches via Optionals, and clears/rebuilds the characteristics set while keeping associations consistent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceMappingService {
  private final TimeProvider timeProvider;

  public ResourceResponse toResponse(Resource resource) {
    return new ResourceResponse(
        resource.getId(),
        resource.getType(),
        resource.getCountryCode(),
        resource.getVersion(),
        timeProvider.toApplicationOffset(resource.getCreatedAt()),
        timeProvider.toApplicationOffset(resource.getUpdatedAt()),
        toLocationResponse(resource.getLocation()),
        resource.getCharacteristics().stream()
            .map(this::toCharacteristicResponse)
            .collect(Collectors.toSet()));
  }

  public LocationResponse toLocationResponse(Location location) {
    if (location == null) {
      return null;
    }
    return new LocationResponse(
        location.getId(),
        location.getStreetAddress(),
        location.getCity(),
        location.getPostalCode(),
        location.getCountryCode());
  }

  public CharacteristicResponse toCharacteristicResponse(Characteristic characteristic) {
    return new CharacteristicResponse(
        characteristic.getId(),
        characteristic.getCode(),
        characteristic.getType(),
        characteristic.getValue());
  }

  public Resource toEntity(ResourceRequest request) {
    Resource resource =
        Resource.builder().type(request.type()).countryCode(request.countryCode()).build();

    if (request.location() != null) {
      Location location = toLocationEntity(request.location());
      // Normalize: ensure location country matches resource
      location.setCountryCode(request.countryCode());
      log.debug(
          "Aligning location countryCode to resource countryCode for new resource: {} -> {}",
          request.location().countryCode(),
          request.countryCode());
      resource.setLocation(location);
    }

    if (request.characteristics() != null) {
      Set<Characteristic> characteristics =
          request.characteristics().stream()
              .map(this::toCharacteristicEntity)
              .collect(Collectors.toSet());
      // Use helper to keep bidirectional association consistent
      characteristics.forEach(resource::addCharacteristic);
    }

    return resource;
  }

  public Location toLocationEntity(LocationRequest request) {
    return Location.builder()
        .streetAddress(request.streetAddress())
        .city(request.city())
        .postalCode(request.postalCode())
        .countryCode(request.countryCode())
        .build();
  }

  public Characteristic toCharacteristicEntity(CharacteristicRequest request) {
    return Characteristic.builder()
        .code(request.code())
        .type(request.type())
        .value(request.value())
        .build();
  }

  public void updateEntity(Resource resource, ResourceRequest request) {
    resource.setType(request.type());
    resource.setCountryCode(request.countryCode());

    if (request.location() != null) {
      if (resource.getLocation() != null) {
        updateLocationEntity(resource.getLocation(), request.location());
        // Keep location country aligned with resource country on updates
        String previous = resource.getLocation().getCountryCode();
        resource.getLocation().setCountryCode(request.countryCode());
        if (previous == null || !previous.equals(request.countryCode())) {
          log.debug(
              "Updated location countryCode to match resource update: {} -> {}",
              previous,
              request.countryCode());
        }
      } else {
        Location location = toLocationEntity(request.location());
        location.setCountryCode(request.countryCode());
        log.debug(
            "Setting location for resource and aligning countryCode to {}", request.countryCode());
        resource.setLocation(location);
      }
    }

    if (request.characteristics() != null) {
      // Replace entire set so orphanRemoval can clean up removed rows
      resource.getCharacteristics().clear();
      Set<Characteristic> characteristics =
          request.characteristics().stream()
              .map(this::toCharacteristicEntity)
              .collect(Collectors.toSet());
      characteristics.forEach(resource::addCharacteristic);
    }
  }

  public void updateLocationEntity(Location location, LocationRequest request) {
    location.setStreetAddress(request.streetAddress());
    location.setCity(request.city());
    location.setPostalCode(request.postalCode());
    location.setCountryCode(request.countryCode());
  }

  public void patchEntity(Resource resource, PatchResourceRequest request) {
    request.type().ifPresent(resource::setType);
    request.countryCode().ifPresent(resource::setCountryCode);

    request
        .location()
        .ifPresent(
            locationRequest -> {
              if (resource.getLocation() != null) {
                updateLocationEntity(resource.getLocation(), locationRequest);
                if (resource.getCountryCode() != null) {
                  // If resource country is known, keep location in sync
                  String previous = resource.getLocation().getCountryCode();
                  resource.getLocation().setCountryCode(resource.getCountryCode());
                  if (previous == null || !previous.equals(resource.getCountryCode())) {
                    log.debug(
                        "Patched location countryCode to match resource countryCode: {} -> {}",
                        previous,
                        resource.getCountryCode());
                  }
                }
              } else {
                Location location = toLocationEntity(locationRequest);
                if (resource.getCountryCode() != null) {
                  location.setCountryCode(resource.getCountryCode());
                  log.debug(
                      "Added location via patch and aligned countryCode to {}",
                      resource.getCountryCode());
                }
                resource.setLocation(location);
              }
            });

    request
        .characteristics()
        .ifPresent(
            characteristicsRequest -> {
              // Replace set to avoid stale associations; add via helper for both sides
              resource.getCharacteristics().clear();
              Set<Characteristic> characteristics =
                  characteristicsRequest.stream()
                      .map(this::toCharacteristicEntity)
                      .collect(Collectors.toSet());
              characteristics.forEach(resource::addCharacteristic);
            });
  }
}
