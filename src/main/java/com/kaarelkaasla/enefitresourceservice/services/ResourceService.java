package com.kaarelkaasla.enefitresourceservice.services;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.entities.Resource;
import com.kaarelkaasla.enefitresourceservice.exceptions.OptimisticLockingException;
import com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException;
import com.kaarelkaasla.enefitresourceservice.repositories.ResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Business logic for CRUD operations and batch notifications for resources.
 * Wraps mutations in transactions, loads with fetch-joins, maps entities/DTOs,
 * and publishes CREATED/UPDATED/DELETED/BATCH events while translating optimistic locking conflicts to domain exceptions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

  private final ResourceRepository resourceRepository;
  private final ResourceMappingService mappingService;
  private final ResourceEventService eventService;
  private final TimeProvider timeProvider;

  @Transactional
  public ResourceResponse createResource(ResourceRequest request) {
    log.debug(
        "Creating resource with type: {} and country code: {}",
        request.type(),
        request.countryCode());

    Resource resource = mappingService.toEntity(request);
    Resource savedResource = resourceRepository.save(resource);

    ResourceResponse response = mappingService.toResponse(savedResource);

    eventService.publishResourceCreated(response);

    log.info("Created resource with id: {}", savedResource.getId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<ResourceResponse> getAllResources() {
    log.debug("Retrieving all resources");
    List<Resource> resources = resourceRepository.findAllWithDetails();
    List<ResourceResponse> result = resources.stream().map(mappingService::toResponse).toList();
    log.info("Retrieved {} resources", result.size());
    return result;
  }

  @Transactional(readOnly = true)
  public ResourceResponse getResourceById(Long id) {
    log.debug("Retrieving resource with id: {}", id);
    Resource resource =
        resourceRepository
            .findByIdWithDetails(id)
            .orElseThrow(
                () -> {
                  log.warn("Resource not found when retrieving id: {}", id);
                  return new ResourceNotFoundException("Resource not found with id: " + id);
                });
    ResourceResponse response = mappingService.toResponse(resource);
    log.info("Retrieved resource with id: {}", id);
    return response;
  }

  @Transactional
  public ResourceResponse updateResource(Long id, ResourceRequest request) {
    log.debug("Updating resource with id: {}", id);

    Resource existingResource =
        resourceRepository
            .findByIdWithDetails(id)
            .orElseThrow(
                () -> {
                  log.warn("Resource not found when updating id: {}", id);
                  return new ResourceNotFoundException("Resource not found with id: " + id);
                });

    try {
      mappingService.updateEntity(existingResource, request);
      Resource updatedResource = resourceRepository.save(existingResource);

      ResourceResponse response = mappingService.toResponse(updatedResource);

      eventService.publishResourceUpdated(response);

      log.info("Updated resource with id: {}", id);
      return response;

    } catch (OptimisticLockingFailureException e) {
      log.warn("Optimistic locking conflict when updating resource with id: {}", id);
      throw new OptimisticLockingException(
          "Resource was modified by another transaction. Please refresh and try again.");
    }
  }

  @Transactional
  public ResourceResponse patchResource(Long id, PatchResourceRequest request) {
    log.debug("Patching resource with id: {}", id);

    Resource existingResource =
        resourceRepository
            .findByIdWithDetails(id)
            .orElseThrow(
                () -> {
                  log.warn("Resource not found when patching id: {}", id);
                  return new ResourceNotFoundException("Resource not found with id: " + id);
                });

    try {
      mappingService.patchEntity(existingResource, request);
      Resource updatedResource = resourceRepository.save(existingResource);

      ResourceResponse response = mappingService.toResponse(updatedResource);

      eventService.publishResourceUpdated(response);

      log.info("Patched resource with id: {}", id);
      return response;

    } catch (OptimisticLockingFailureException e) {
      log.warn("Optimistic locking conflict when patching resource with id: {}", id);
      throw new OptimisticLockingException(
          "Resource was modified by another transaction. Please refresh and try again.");
    }
  }

  @Transactional
  public void deleteResource(Long id) {
    log.debug("Deleting resource with id: {}", id);

    if (!resourceRepository.existsById(id)) {
      log.warn("Resource not found when deleting id: {}", id);
      throw new ResourceNotFoundException("Resource not found with id: " + id);
    }

    try {
      resourceRepository.deleteById(id);

      eventService.publishResourceDeleted(id);

      log.info("Deleted resource with id: {}", id);

    } catch (OptimisticLockingFailureException e) {
      log.warn("Optimistic locking conflict when deleting resource with id: {}", id);
      throw new OptimisticLockingException(
          "Resource was modified by another transaction. Please refresh and try again.");
    }
  }

  @Transactional(readOnly = true)
  public BatchNotificationResponse notifyAllResources() {
    log.debug("Starting batch notification for all resources");

    List<Resource> allResources = resourceRepository.findAllWithDetails();
    List<ResourceResponse> responses =
        allResources.stream().map(mappingService::toResponse).toList();

    if (responses.isEmpty()) {
      log.info("No resources found for batch notification; nothing to publish");
    } else {
      log.debug("Publishing batch notifications for {} resources", responses.size());
    }

    // Simplified: fire-and-forget publishing; does not persist an outbox or operation state.
    // For production, prefer a transactional outbox and add an operation tracker API.
    try {
      eventService.publishBatchNotification(responses);
    } catch (RuntimeException e) {
      log.error("Batch notification failed while publishing {} resources", responses.size(), e);
      throw e;
    }

    // Simplified status reporting for this demo implementation.
    BatchNotificationResponse response =
        new BatchNotificationResponse(
            java.util.UUID.randomUUID(),
            responses.size(),
            "COMPLETED",
            timeProvider.now(),
            ResourceEventType.BATCH_NOTIFICATION);

    log.info("Completed batch notification for {} resources", responses.size());
    return response;
  }
}
