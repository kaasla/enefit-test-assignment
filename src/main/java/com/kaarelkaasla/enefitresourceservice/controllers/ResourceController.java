package com.kaarelkaasla.enefitresourceservice.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kaarelkaasla.enefitresourceservice.api.ResourceApi;
import com.kaarelkaasla.enefitresourceservice.dtos.*;
import com.kaarelkaasla.enefitresourceservice.services.ResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing resource management endpoints.
 * Delegates to ResourceService and maps responses to a HTTP status with simple logging.
 */
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController implements ResourceApi {

  private final ResourceService resourceService;

  @Override
  @PostMapping
  public ResponseEntity<ResourceResponse> createResource(
      @Valid @RequestBody ResourceRequest request) {
    log.info("Creating resource: {}", request);
    ResourceResponse response = resourceService.createResource(request);
    log.info("Created resource successfully: id={} status=201", response.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @GetMapping
  public ResponseEntity<List<ResourceResponse>> getAllResources() {
    log.info("Retrieving all resources");
    List<ResourceResponse> resources = resourceService.getAllResources();
    log.info("Returning {} resources", resources.size());
    return ResponseEntity.ok(resources);
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Long id) {
    log.info("Retrieving resource with id: {}", id);
    ResourceResponse response = resourceService.getResourceById(id);
    log.info("Found resource with id: {}", id);
    return ResponseEntity.ok(response);
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<ResourceResponse> updateResource(
      @PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
    log.info("Updating resource with id: {} with data: {}", id, request);
    ResourceResponse response = resourceService.updateResource(id, request);
    log.info("Updated resource successfully: id={}", id);
    return ResponseEntity.ok(response);
  }

  @Override
  @PatchMapping("/{id}")
  public ResponseEntity<ResourceResponse> patchResource(
      @PathVariable Long id, @Valid @RequestBody PatchResourceRequest request) {
    log.info("Patching resource with id: {} with data: {}", id, request);
    ResourceResponse response = resourceService.patchResource(id, request);
    log.info("Patched resource successfully: id={}", id);
    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
    log.info("Deleting resource with id: {}", id);
    resourceService.deleteResource(id);
    log.info("Deleted resource successfully: id={} status=204", id);
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/send-all")
  public ResponseEntity<BatchNotificationResponse> sendAllResources() {
    log.info("Starting batch notification for all resources");
    BatchNotificationResponse response = resourceService.notifyAllResources();
    log.info(
        "Batch notification completed: operationId={} resourceCount={} status={}",
        response.operationId(),
        response.resourceCount(),
        response.status());
    return ResponseEntity.ok(response);
  }
}
