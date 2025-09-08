package com.kaarelkaasla.enefitresourceservice.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.entities.*;
import com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException;
import com.kaarelkaasla.enefitresourceservice.repositories.ResourceRepository;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

  @Mock private ResourceRepository resourceRepository;

  @Mock private ResourceMappingService mappingService;

  @Mock private ResourceEventService resourceEventService;

  @Mock private TimeProvider timeProvider;

  @InjectMocks private ResourceService resourceService;

  private ResourceRequest createRequest;
  private ResourceRequest updateRequest;
  private PatchResourceRequest patchRequest;
  private Resource existingResource;
  private LocationRequest locationRequest;
  private CharacteristicRequest characteristicRequest;

  @BeforeEach
  void setUp() {
    locationRequest = new LocationRequest("123 Main Street", "New York", "10001", "US");

    LocationRequest caLocationRequest =
        new LocationRequest("456 Queen Street", "Toronto", "12345", "CA");

    characteristicRequest =
        new CharacteristicRequest("CT001", CharacteristicType.CONSUMPTION_TYPE, "Residential");

    createRequest =
        new ResourceRequest(
            ResourceType.METERING_POINT, "US", locationRequest, Set.of(characteristicRequest));

    updateRequest =
        new ResourceRequest(
            ResourceType.CONNECTION_POINT, "CA", caLocationRequest, Set.of(characteristicRequest));

    patchRequest =
        new PatchResourceRequest(
            Optional.of(ResourceType.CONNECTION_POINT),
            Optional.of("CA"),
            Optional.empty(),
            Optional.empty());

    Location location = new Location();
    location.setStreetAddress("123 Main Street");
    location.setCity("New York");
    location.setPostalCode("10001");
    location.setCountryCode("US");

    Characteristic characteristic = new Characteristic();
    characteristic.setCode("CT001");
    characteristic.setType(CharacteristicType.CONSUMPTION_TYPE);
    characteristic.setValue("Residential");

    existingResource = new Resource();
    existingResource.setId(1L);
    existingResource.setType(ResourceType.METERING_POINT);
    existingResource.setCountryCode("US");
    existingResource.setVersion(1L);
    existingResource.setCreatedAt(OffsetDateTime.now());
    existingResource.setUpdatedAt(OffsetDateTime.now());
    existingResource.setLocation(location);
    existingResource.setCharacteristics(Set.of(characteristic));

    // Avoid strict-stubbing warnings for non-essential time calls
    lenient().when(timeProvider.now()).thenReturn(OffsetDateTime.now());
  }

  private ResourceResponse createMockResourceResponse() {
    return new ResourceResponse(
        1L,
        ResourceType.METERING_POINT,
        "US",
        1L,
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        new LocationResponse(1L, "123 Main Street", "New York", "10001", "US"),
        Set.of(
            new CharacteristicResponse(
                1L, "CT001", CharacteristicType.CONSUMPTION_TYPE, "Residential")));
  }

  @Test
  void createResource_ValidRequest_ReturnsResourceResponse() {
    Resource savedResource = new Resource();
    savedResource.setId(1L);
    savedResource.setType(ResourceType.METERING_POINT);
    savedResource.setCountryCode("US");
    savedResource.setVersion(1L);
    savedResource.setCreatedAt(OffsetDateTime.now());
    savedResource.setUpdatedAt(OffsetDateTime.now());

    when(mappingService.toEntity(createRequest)).thenReturn(existingResource);
    when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);
    when(mappingService.toResponse(savedResource)).thenReturn(createMockResourceResponse());

    ResourceResponse result = resourceService.createResource(createRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.METERING_POINT);
    assertThat(result.countryCode()).isEqualTo("US");
    assertThat(result.version()).isEqualTo(1L);

    verify(resourceRepository).save(any(Resource.class));
    verify(resourceEventService).publishResourceCreated(any(ResourceResponse.class));
  }

  @Test
  void createResource_RepositoryThrowsException_PropagatesException() {
    when(mappingService.toEntity(createRequest)).thenReturn(existingResource);
    when(resourceRepository.save(any(Resource.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThatThrownBy(() -> resourceService.createResource(createRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Database error");

    verify(resourceRepository).save(any(Resource.class));
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void getAllResources_ReturnsListOfResources() {
    List<Resource> resources = List.of(existingResource);
    when(resourceRepository.findAllWithDetails()).thenReturn(resources);
    when(mappingService.toResponse(existingResource)).thenReturn(createMockResourceResponse());

    List<ResourceResponse> result = resourceService.getAllResources();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).type()).isEqualTo(ResourceType.METERING_POINT);

    verify(resourceRepository).findAllWithDetails();
  }

  @Test
  void getAllResources_EmptyRepository_ReturnsEmptyList() {
    when(resourceRepository.findAllWithDetails()).thenReturn(List.of());

    List<ResourceResponse> result = resourceService.getAllResources();

    assertThat(result).isEmpty();
    verify(resourceRepository).findAllWithDetails();
  }

  @Test
  void getResourceById_ExistingId_ReturnsResource() {
    when(resourceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(existingResource));
    when(mappingService.toResponse(existingResource)).thenReturn(createMockResourceResponse());

    ResourceResponse result = resourceService.getResourceById(1L);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.METERING_POINT);

    verify(resourceRepository).findByIdWithDetails(1L);
  }

  @Test
  void getResourceById_NonExistingId_ThrowsResourceNotFoundException() {
    when(resourceRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> resourceService.getResourceById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Resource not found with id: 999");

    verify(resourceRepository).findByIdWithDetails(999L);
  }

  @Test
  void updateResource_ExistingResource_ReturnsUpdatedResource() {
    Resource updatedResource = new Resource();
    updatedResource.setId(1L);
    updatedResource.setType(ResourceType.CONNECTION_POINT);
    updatedResource.setCountryCode("CA");
    updatedResource.setVersion(2L);
    updatedResource.setCreatedAt(existingResource.getCreatedAt());
    updatedResource.setUpdatedAt(OffsetDateTime.now());

    when(resourceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(existingResource));
    when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);
    when(mappingService.toResponse(updatedResource))
        .thenReturn(
            new ResourceResponse(
                1L,
                ResourceType.CONNECTION_POINT,
                "CA",
                2L,
                existingResource.getCreatedAt(),
                OffsetDateTime.now(),
                new LocationResponse(1L, "456 Queen Street", "Toronto", "12345", "CA"),
                Set.of(
                    new CharacteristicResponse(
                        1L, "CT001", CharacteristicType.CONSUMPTION_TYPE, "Residential"))));

    ResourceResponse result = resourceService.updateResource(1L, updateRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.CONNECTION_POINT);
    assertThat(result.countryCode()).isEqualTo("CA");
    assertThat(result.version()).isEqualTo(2L);

    verify(resourceRepository).findByIdWithDetails(1L);
    verify(resourceRepository).save(any(Resource.class));
    verify(resourceEventService).publishResourceUpdated(any(ResourceResponse.class));
  }

  @Test
  void updateResource_NonExistingId_ThrowsResourceNotFoundException() {
    when(resourceRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> resourceService.updateResource(999L, updateRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Resource not found with id: 999");

    verify(resourceRepository).findByIdWithDetails(999L);
    verify(resourceRepository, never()).save(any(Resource.class));
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void updateResource_OptimisticLockingFailure_ThrowsException() {
    when(resourceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(existingResource));
    when(resourceRepository.save(any(Resource.class)))
        .thenThrow(new OptimisticLockingFailureException("Version conflict"));

    assertThatThrownBy(() -> resourceService.updateResource(1L, updateRequest))
        .isInstanceOf(
            com.kaarelkaasla.enefitresourceservice.exceptions.OptimisticLockingException.class)
        .hasMessage("Resource was modified by another transaction. Please refresh and try again.");

    verify(resourceRepository).findByIdWithDetails(1L);
    verify(resourceRepository).save(any(Resource.class));
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void patchResource_PartialUpdate_ReturnsUpdatedResource() {
    Resource patchedResource = new Resource();
    patchedResource.setId(1L);
    patchedResource.setType(ResourceType.CONNECTION_POINT);
    patchedResource.setCountryCode("CA");
    patchedResource.setVersion(2L);

    when(resourceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(existingResource));
    when(resourceRepository.save(any(Resource.class))).thenReturn(patchedResource);
    when(mappingService.toResponse(patchedResource))
        .thenReturn(
            new ResourceResponse(
                1L,
                ResourceType.CONNECTION_POINT,
                "CA",
                2L,
                existingResource.getCreatedAt(),
                OffsetDateTime.now(),
                new LocationResponse(1L, "123 Main Street", "New York", "10001", "US"),
                Set.of(
                    new CharacteristicResponse(
                        1L, "CT001", CharacteristicType.CONSUMPTION_TYPE, "Residential"))));

    ResourceResponse result = resourceService.patchResource(1L, patchRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.CONNECTION_POINT);
    assertThat(result.countryCode()).isEqualTo("CA");

    verify(resourceRepository).findByIdWithDetails(1L);
    verify(resourceRepository).save(any(Resource.class));
    verify(resourceEventService).publishResourceUpdated(any(ResourceResponse.class));
  }

  @Test
  void patchResource_EmptyPatch_ReturnsUnchangedResource() {
    PatchResourceRequest emptyPatch =
        new PatchResourceRequest(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    when(resourceRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(existingResource));
    when(resourceRepository.save(any(Resource.class))).thenReturn(existingResource);
    when(mappingService.toResponse(existingResource)).thenReturn(createMockResourceResponse());

    ResourceResponse result = resourceService.patchResource(1L, emptyPatch);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.type()).isEqualTo(ResourceType.METERING_POINT);

    verify(resourceRepository).findByIdWithDetails(1L);
    verify(resourceRepository).save(any(Resource.class));
    verify(resourceEventService).publishResourceUpdated(any(ResourceResponse.class));
  }

  @Test
  void patchResource_NonExistingId_ThrowsResourceNotFoundException() {
    when(resourceRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> resourceService.patchResource(999L, patchRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Resource not found with id: 999");

    verify(resourceRepository).findByIdWithDetails(999L);
    verify(resourceRepository, never()).save(any(Resource.class));
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void deleteResource_ExistingResource_DeletesSuccessfully() {
    when(resourceRepository.existsById(1L)).thenReturn(true);

    resourceService.deleteResource(1L);

    verify(resourceRepository).existsById(1L);
    verify(resourceRepository).deleteById(1L);
    verify(resourceEventService).publishResourceDeleted(eq(1L));
  }

  @Test
  void deleteResource_NonExistingId_ThrowsResourceNotFoundException() {
    when(resourceRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> resourceService.deleteResource(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Resource not found with id: 999");

    verify(resourceRepository).existsById(999L);
    verify(resourceRepository, never()).deleteById(any(Long.class));
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void deleteResource_OptimisticLockingFailure_ThrowsException() {
    when(resourceRepository.existsById(1L)).thenReturn(true);
    doThrow(new OptimisticLockingFailureException("Version conflict"))
        .when(resourceRepository)
        .deleteById(1L);

    assertThatThrownBy(() -> resourceService.deleteResource(1L))
        .isInstanceOf(
            com.kaarelkaasla.enefitresourceservice.exceptions.OptimisticLockingException.class)
        .hasMessage("Resource was modified by another transaction. Please refresh and try again.");

    verify(resourceRepository).existsById(1L);
    verify(resourceRepository).deleteById(1L);
    verifyNoInteractions(resourceEventService);
  }

  @Test
  void notifyAllResources_WithResources_ReturnsSuccessResponse() {
    List<Resource> resources = List.of(existingResource);
    when(resourceRepository.findAllWithDetails()).thenReturn(resources);

    BatchNotificationResponse result = resourceService.notifyAllResources();

    assertThat(result).isNotNull();
    assertThat(result.resourceCount()).isEqualTo(1);
    assertThat(result.status()).isEqualTo("COMPLETED");
    assertThat(result.operation()).isEqualTo(ResourceEventType.BATCH_NOTIFICATION);
    assertThat(result.operationId()).isNotNull();
    assertThat(result.processedAt()).isNotNull();

    verify(resourceRepository).findAllWithDetails();
    verify(resourceEventService).publishBatchNotification(any(List.class));
  }

  @Test
  void notifyAllResources_EmptyRepository_ReturnsZeroCount() {
    when(resourceRepository.findAllWithDetails()).thenReturn(List.of());

    BatchNotificationResponse result = resourceService.notifyAllResources();

    assertThat(result).isNotNull();
    assertThat(result.resourceCount()).isEqualTo(0);
    assertThat(result.status()).isEqualTo("COMPLETED");

    verify(resourceRepository).findAllWithDetails();
    verify(resourceEventService).publishBatchNotification(List.of());
  }

  @Test
  void notifyAllResources_ServiceThrowsException_PropagatesException() {
    List<Resource> resources = List.of(existingResource);
    when(resourceRepository.findAllWithDetails()).thenReturn(resources);
    doThrow(new RuntimeException("Kafka error"))
        .when(resourceEventService)
        .publishBatchNotification(any(List.class));

    assertThatThrownBy(() -> resourceService.notifyAllResources())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Kafka error");

    verify(resourceRepository).findAllWithDetails();
    verify(resourceEventService).publishBatchNotification(any(List.class));
  }
}
