package com.kaarelkaasla.enefitresourceservice.services;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEventType;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes resource lifecycle events to Kafka.
 * Builds events with a UUID and timestamp, sends asynchronously keyed by resource id,
 * and logs success/failure while producer retries/backoff handle transient failures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceEventService {

  private final KafkaTemplate<String, ResourceEvent> kafkaTemplate;
  private final TimeProvider timeProvider;

  @Value("${app.kafka.topic.resource-updates}")
  private String topicName;

  public void publishResourceCreated(ResourceResponse resource) {
    ResourceEvent event =
        new ResourceEvent(
            ResourceEventType.CREATED,
            resource.id(),
            resource,
            timeProvider.now(),
            UUID.randomUUID().toString());
    publishEvent(event, resource.id().toString());
  }

  public void publishResourceUpdated(ResourceResponse resource) {
    ResourceEvent event =
        new ResourceEvent(
            ResourceEventType.UPDATED,
            resource.id(),
            resource,
            timeProvider.now(),
            UUID.randomUUID().toString());
    publishEvent(event, resource.id().toString());
  }

  public void publishResourceDeleted(Long resourceId) {
    ResourceEvent event =
        new ResourceEvent(
            ResourceEventType.DELETED,
            resourceId,
            null,
            timeProvider.now(),
            UUID.randomUUID().toString());
    publishEvent(event, resourceId.toString());
  }

  public void publishBatchNotification(List<ResourceResponse> resources) {
    resources.forEach(
        resource -> {
          ResourceEvent event =
              new ResourceEvent(
                  ResourceEventType.BATCH_NOTIFICATION,
                  resource.id(),
                  resource,
                  timeProvider.now(),
                  UUID.randomUUID().toString());
          publishEvent(event, resource.id().toString());
        });
  }

  private void publishEvent(ResourceEvent event, String key) {
    log.debug("Publishing event: {} for resource: {}", event.eventType(), event.resourceId());

    CompletableFuture<SendResult<String, ResourceEvent>> future =
        kafkaTemplate.send(topicName, key, event);

    // Async callback purely for logging; retries/backoff are controlled by producer settings
    future.whenComplete(
        (result, ex) -> {
          if (ex == null) {
            log.info(
                "Successfully published event: {} for resource: {} with offset: {}",
                event.eventType(),
                event.resourceId(),
                result.getRecordMetadata().offset());
          } else {
            log.error(
                "Failed to publish event: {} for resource: {} - EventId: {} - Exception: {}",
                event.eventType(),
                event.resourceId(),
                event.eventId(),
                ex.getMessage());
            // No manual retry here; allow KafkaTemplate/producer retry config to handle it
          }
        });
  }
}
