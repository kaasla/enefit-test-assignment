package com.kaarelkaasla.enefitresourceservice.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceResponse;

@ExtendWith(MockitoExtension.class)
public class ResourceEventServiceRetryTest {

  @Mock private KafkaTemplate<String, ResourceEvent> kafkaTemplate;

  @Mock private TimeProvider timeProvider;

  @Mock private SendResult<String, ResourceEvent> sendResult;

  private ResourceEventService resourceEventService;

  @BeforeEach
  void setUp() {
    resourceEventService = new ResourceEventService(kafkaTemplate, timeProvider);
    // Inject private field without a setter to avoid spinning up Spring context
    ReflectionTestUtils.setField(resourceEventService, "topicName", "resource-updates");

    when(timeProvider.now()).thenReturn(OffsetDateTime.now());
  }

  @Test
  void shouldHandleSuccessfulPublish() {
    CompletableFuture<SendResult<String, ResourceEvent>> future =
        CompletableFuture.completedFuture(sendResult);
    when(kafkaTemplate.send(anyString(), anyString(), any(ResourceEvent.class))).thenReturn(future);

    ResourceResponse resource =
        new ResourceResponse(
            1L, null, "US", null, OffsetDateTime.now(), OffsetDateTime.now(), null, Set.of());

    resourceEventService.publishResourceCreated(resource);

    verify(kafkaTemplate).send(eq("resource-updates"), eq("1"), any(ResourceEvent.class));
  }

  @Test
  void shouldHandleFailedPublish() {
    CompletableFuture<SendResult<String, ResourceEvent>> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("Kafka is down"));
    when(kafkaTemplate.send(anyString(), anyString(), any(ResourceEvent.class))).thenReturn(future);

    ResourceResponse resource =
        new ResourceResponse(
            2L, null, "US", null, OffsetDateTime.now(), OffsetDateTime.now(), null, Set.of());

    resourceEventService.publishResourceCreated(resource);

    verify(kafkaTemplate).send(eq("resource-updates"), eq("2"), any(ResourceEvent.class));
  }

  @Test
  void shouldRetryOnFailureWithProducerConfiguration() {
    when(kafkaTemplate.send(anyString(), anyString(), any(ResourceEvent.class)))
        .thenReturn(new CompletableFuture<>());

    ResourceResponse resource =
        new ResourceResponse(
            3L, null, "US", null, OffsetDateTime.now(), OffsetDateTime.now(), null, Set.of());

    resourceEventService.publishResourceCreated(resource);

    verify(kafkaTemplate).send(eq("resource-updates"), eq("3"), any(ResourceEvent.class));
  }
}
