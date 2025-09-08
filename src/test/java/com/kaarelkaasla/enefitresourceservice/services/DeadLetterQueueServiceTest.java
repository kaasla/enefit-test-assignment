package com.kaarelkaasla.enefitresourceservice.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEventType;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueServiceTest {

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private DeadLetterQueueService deadLetterQueueService;

  private ResourceEvent testEvent;
  private ListAppender<ILoggingEvent> logWatcher;

  @BeforeEach
  void setUp() {
    testEvent =
        new ResourceEvent(
            ResourceEventType.CREATED, 1L, null, java.time.OffsetDateTime.now(), "test-event-id");

    // Capture logs from DeadLetterQueueService for assertions
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(DeadLetterQueueService.class))
        .addAppender(logWatcher);
  }

  @Test
  void handleDltMessage_ShouldLogErrorWithAllDetails() throws JsonProcessingException {
    String testJson = "{\"eventId\":\"test-event-id\"}";
    when(objectMapper.writeValueAsString(any())).thenReturn(testJson);

    deadLetterQueueService.handleDltMessage(testEvent, "test-topic", 0, 123L, "Test error message");

    verify(objectMapper).writeValueAsString(testEvent);

    logWatcher.list.stream()
        .filter(event -> event.getLevel().equals(Level.ERROR))
        .filter(event -> event.getMessage().contains("DLT_MESSAGE_RECEIVED"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected error log not found"));
  }

  @Test
  void handleDltMessage_WithSerializationError_ShouldLogSerializationCategory()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(
        testEvent, "test-topic", 0, 123L, "JSON deserialization error");

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=SERIALIZATION"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected serialization category log not found"));
  }

  @Test
  void handleDltMessage_WithTimeoutError_ShouldLogTransientCategory()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(
        testEvent, "test-topic", 0, 123L, "Connection timeout occurred");

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=TRANSIENT"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected transient category log not found"));
  }

  @Test
  void handleDltMessage_WithValidationError_ShouldLogValidationCategory()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(
        testEvent, "test-topic", 0, 123L, "Data validation failed");

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=VALIDATION"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected validation category log not found"));
  }

  @Test
  void handleDltMessage_WithBusinessError_ShouldLogBusinessCategory()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(
        testEvent, "test-topic", 0, 123L, "Business rule violation");

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=BUSINESS"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected business category log not found"));
  }

  @Test
  void handleDltMessage_WithNullError_ShouldLogUnknownCategory() throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(testEvent, "test-topic", 0, 123L, null);

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=UNKNOWN"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected unknown category log not found"));
  }

  @Test
  void handleDltMessage_WithJsonProcessingException_ShouldLogFallbackJson()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("JSON error") {});

    deadLetterQueueService.handleDltMessage(testEvent, "test-topic", 0, 123L, "Test error");

    logWatcher.list.stream()
        .filter(event -> event.getLevel().equals(Level.WARN))
        .filter(event -> event.getMessage().contains("Failed to serialize event"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected JSON serialization warning not found"));
  }

  @Test
  void handleDltMessage_MultipleErrorKeywords_ShouldCategorizeByFirstMatch()
      throws JsonProcessingException {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    deadLetterQueueService.handleDltMessage(
        testEvent, "test-topic", 0, 123L, "JSON parse error with timeout");

    logWatcher.list.stream()
        .filter(event -> event.getMessage().contains("DLT_CATEGORY=SERIALIZATION"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected serialization category log not found"));
  }
}
