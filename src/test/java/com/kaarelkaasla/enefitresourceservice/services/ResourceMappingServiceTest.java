package com.kaarelkaasla.enefitresourceservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kaarelkaasla.enefitresourceservice.dtos.CharacteristicRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.CharacteristicResponse;
import com.kaarelkaasla.enefitresourceservice.dtos.LocationRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.LocationResponse;
import com.kaarelkaasla.enefitresourceservice.dtos.PatchResourceRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceRequest;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceResponse;
import com.kaarelkaasla.enefitresourceservice.entities.Characteristic;
import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;
import com.kaarelkaasla.enefitresourceservice.entities.Location;
import com.kaarelkaasla.enefitresourceservice.entities.Resource;
import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

@ExtendWith(MockitoExtension.class)
class ResourceMappingServiceTest {

  @Mock private TimeProvider timeProvider;

  @InjectMocks private ResourceMappingService resourceMappingService;

  private OffsetDateTime testTime;
  private Resource testResource;
  private ResourceRequest testResourceRequest;

  @BeforeEach
  void setUp() {
    testTime = OffsetDateTime.now(ZoneOffset.UTC);

    testResource =
        Resource.builder()
            .id(1L)
            .type(ResourceType.METERING_POINT)
            .countryCode("US")
            .version(1L)
            .createdAt(testTime)
            .updatedAt(testTime)
            .build();

    Location location =
        Location.builder()
            .id(1L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();
    testResource.setLocation(location);

    Characteristic characteristic =
        Characteristic.builder()
            .id(1L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();
    testResource.addCharacteristic(characteristic);

    testResourceRequest =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "US",
            new LocationRequest("123 Main St", "New York", "10001", "US"),
            Set.of(
                new CharacteristicRequest(
                    "TEST1", CharacteristicType.CONSUMPTION_TYPE, "RESIDENTIAL")));
  }

  @Test
  void toResponse_WithCompleteResource_ShouldMapAllFields() {
    when(timeProvider.toApplicationOffset(any(OffsetDateTime.class))).thenReturn(testTime);

    ResourceResponse result = resourceMappingService.toResponse(testResource);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.METERING_POINT);
    assertThat(result.countryCode()).isEqualTo("US");
    assertThat(result.version()).isEqualTo(1L);
    assertThat(result.createdAt()).isEqualTo(testTime);
    assertThat(result.updatedAt()).isEqualTo(testTime);
    assertThat(result.location()).isNotNull();
    assertThat(result.characteristics()).hasSize(1);

    verify(timeProvider, times(2)).toApplicationOffset(any(OffsetDateTime.class));
  }

  @Test
  void toLocationResponse_WithNullLocation_ShouldReturnNull() {
    LocationResponse result = resourceMappingService.toLocationResponse(null);
    assertThat(result).isNull();
  }

  @Test
  void toLocationResponse_WithLocation_ShouldMapAllFields() {
    Location location =
        Location.builder()
            .id(1L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    LocationResponse result = resourceMappingService.toLocationResponse(location);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.streetAddress()).isEqualTo("123 Main St");
    assertThat(result.city()).isEqualTo("New York");
    assertThat(result.postalCode()).isEqualTo("10001");
    assertThat(result.countryCode()).isEqualTo("US");
  }

  @Test
  void toCharacteristicResponse_ShouldMapAllFields() {
    Characteristic characteristic =
        Characteristic.builder()
            .id(1L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    CharacteristicResponse result = resourceMappingService.toCharacteristicResponse(characteristic);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.code()).isEqualTo("TEST1");
    assertThat(result.type()).isEqualTo(CharacteristicType.CONSUMPTION_TYPE);
    assertThat(result.value()).isEqualTo("RESIDENTIAL");
  }

  @Test
  void toEntity_WithCompleteRequest_ShouldCreateResourceWithAllFields() {
    Resource result = resourceMappingService.toEntity(testResourceRequest);

    assertThat(result.getType()).isEqualTo(ResourceType.METERING_POINT);
    assertThat(result.getCountryCode()).isEqualTo("US");
    assertThat(result.getLocation()).isNotNull();
    assertThat(result.getLocation().getCountryCode()).isEqualTo("US");
    assertThat(result.getCharacteristics()).hasSize(1);
  }

  @Test
  void toEntity_WithNullLocation_ShouldCreateResourceWithoutLocation() {
    ResourceRequest requestWithoutLocation =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "US",
            null,
            Set.of(
                new CharacteristicRequest(
                    "TEST1", CharacteristicType.CONSUMPTION_TYPE, "RESIDENTIAL")));

    Resource result = resourceMappingService.toEntity(requestWithoutLocation);

    assertThat(result.getLocation()).isNull();
    assertThat(result.getCharacteristics()).hasSize(1);
  }

  @Test
  void toEntity_WithNullCharacteristics_ShouldCreateResourceWithoutCharacteristics() {
    ResourceRequest requestWithoutCharacteristics =
        new ResourceRequest(
            ResourceType.METERING_POINT,
            "US",
            new LocationRequest("123 Main St", "New York", "10001", "US"),
            null);

    Resource result = resourceMappingService.toEntity(requestWithoutCharacteristics);

    assertThat(result.getLocation()).isNotNull();
    assertThat(result.getCharacteristics()).isEmpty();
  }

  @Test
  void toLocationEntity_ShouldMapAllFields() {
    LocationRequest request = new LocationRequest("123 Main St", "New York", "10001", "US");

    Location result = resourceMappingService.toLocationEntity(request);

    assertThat(result.getStreetAddress()).isEqualTo("123 Main St");
    assertThat(result.getCity()).isEqualTo("New York");
    assertThat(result.getPostalCode()).isEqualTo("10001");
    assertThat(result.getCountryCode()).isEqualTo("US");
  }

  @Test
  void toCharacteristicEntity_ShouldMapAllFields() {
    CharacteristicRequest request =
        new CharacteristicRequest("TEST1", CharacteristicType.CONSUMPTION_TYPE, "RESIDENTIAL");

    Characteristic result = resourceMappingService.toCharacteristicEntity(request);

    assertThat(result.getCode()).isEqualTo("TEST1");
    assertThat(result.getType()).isEqualTo(CharacteristicType.CONSUMPTION_TYPE);
    assertThat(result.getValue()).isEqualTo("RESIDENTIAL");
  }

  @Test
  void updateEntity_WithExistingLocation_ShouldUpdateLocation() {
    Resource resource =
        Resource.builder().type(ResourceType.CONNECTION_POINT).countryCode("DE").build();
    Location existingLocation =
        Location.builder()
            .streetAddress("Old Street")
            .city("Old City")
            .postalCode("00000")
            .countryCode("DE")
            .build();
    resource.setLocation(existingLocation);

    resourceMappingService.updateEntity(resource, testResourceRequest);

    assertThat(resource.getType()).isEqualTo(ResourceType.METERING_POINT);
    assertThat(resource.getCountryCode()).isEqualTo("US");
    assertThat(resource.getLocation().getStreetAddress()).isEqualTo("123 Main St");
    assertThat(resource.getLocation().getCity()).isEqualTo("New York");
    assertThat(resource.getLocation().getCountryCode()).isEqualTo("US");
  }

  @Test
  void updateEntity_WithoutExistingLocation_ShouldCreateNewLocation() {
    Resource resource =
        Resource.builder().type(ResourceType.CONNECTION_POINT).countryCode("DE").build();

    resourceMappingService.updateEntity(resource, testResourceRequest);

    assertThat(resource.getLocation()).isNotNull();
    assertThat(resource.getLocation().getCountryCode()).isEqualTo("US");
  }

  @Test
  void updateLocationEntity_ShouldUpdateAllFields() {
    Location location =
        Location.builder()
            .streetAddress("Old Street")
            .city("Old City")
            .postalCode("00000")
            .countryCode("DE")
            .build();
    LocationRequest request = new LocationRequest("New Street", "New City", "11111", "US");

    resourceMappingService.updateLocationEntity(location, request);

    assertThat(location.getStreetAddress()).isEqualTo("New Street");
    assertThat(location.getCity()).isEqualTo("New City");
    assertThat(location.getPostalCode()).isEqualTo("11111");
    assertThat(location.getCountryCode()).isEqualTo("US");
  }

  @Test
  void patchEntity_WithTypeAndCountryCode_ShouldUpdateFields() {
    Resource resource =
        Resource.builder().type(ResourceType.CONNECTION_POINT).countryCode("DE").build();
    PatchResourceRequest patchRequest =
        new PatchResourceRequest(
            Optional.of(ResourceType.METERING_POINT),
            Optional.of("US"),
            Optional.empty(),
            Optional.empty());

    resourceMappingService.patchEntity(resource, patchRequest);

    assertThat(resource.getType()).isEqualTo(ResourceType.METERING_POINT);
    assertThat(resource.getCountryCode()).isEqualTo("US");
  }

  @Test
  void patchEntity_WithLocation_ExistingLocation_ShouldUpdateLocation() {
    Resource resource = Resource.builder().countryCode("US").build();
    Location existingLocation =
        Location.builder()
            .streetAddress("Old Street")
            .city("Old City")
            .postalCode("00000")
            .countryCode("DE")
            .build();
    resource.setLocation(existingLocation);

    LocationRequest newLocationRequest =
        new LocationRequest("New Street", "New City", "11111", "FR");
    PatchResourceRequest patchRequest =
        new PatchResourceRequest(
            Optional.empty(), Optional.empty(), Optional.of(newLocationRequest), Optional.empty());

    resourceMappingService.patchEntity(resource, patchRequest);

    assertThat(resource.getLocation().getStreetAddress()).isEqualTo("New Street");
    assertThat(resource.getLocation().getCountryCode()).isEqualTo("US");
  }

  @Test
  void patchEntity_WithLocation_NoExistingLocation_ShouldCreateLocation() {
    Resource resource = Resource.builder().countryCode("US").build();

    LocationRequest newLocationRequest =
        new LocationRequest("New Street", "New City", "11111", "FR");
    PatchResourceRequest patchRequest =
        new PatchResourceRequest(
            Optional.empty(), Optional.empty(), Optional.of(newLocationRequest), Optional.empty());

    resourceMappingService.patchEntity(resource, patchRequest);

    assertThat(resource.getLocation()).isNotNull();
    assertThat(resource.getLocation().getStreetAddress()).isEqualTo("New Street");
    assertThat(resource.getLocation().getCountryCode()).isEqualTo("US");
  }

  @Test
  void patchEntity_WithCharacteristics_ShouldReplaceCharacteristics() {
    Resource resource = Resource.builder().build();
    Characteristic oldChar =
        Characteristic.builder()
            .code("OLD")
            .type(CharacteristicType.CONNECTION_POINT_STATUS)
            .value("INACTIVE")
            .build();
    resource.addCharacteristic(oldChar);

    Set<CharacteristicRequest> newCharacteristics =
        Set.of(
            new CharacteristicRequest("NEW1", CharacteristicType.CONSUMPTION_TYPE, "COMMERCIAL"),
            new CharacteristicRequest(
                "NEW2", CharacteristicType.CONNECTION_POINT_STATUS, "ACTIVE"));
    PatchResourceRequest patchRequest =
        new PatchResourceRequest(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(newCharacteristics));

    resourceMappingService.patchEntity(resource, patchRequest);

    assertThat(resource.getCharacteristics()).hasSize(2);
    assertThat(resource.getCharacteristics().stream().anyMatch(c -> "NEW1".equals(c.getCode())))
        .isTrue();
    assertThat(resource.getCharacteristics().stream().anyMatch(c -> "NEW2".equals(c.getCode())))
        .isTrue();
  }
}
