package com.kaarelkaasla.enefitresourceservice.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes DLT messages and logs structured diagnostics for triage.
 * Reads from the configured DLT topic, heuristically categorizes common failures,
 * and logs the full event as JSON for quick recovery.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeadLetterQueueService {

  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "${app.kafka.topic.resource-updates-dlt}")
  public void handleDltMessage(
      @Payload ResourceEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {

    // Structured log with topic/partition/offset to make tracing easier
    log.error(
        "DLT_MESSAGE_RECEIVED topic={} partition={} offset={} eventId={} resourceId={} eventType={}"
            + " error={}",
        topic,
        partition,
        offset,
        event.eventId(),
        event.resourceId(),
        event.eventType(),
        exceptionMessage);

    // Log full event JSON to simplify manual replay or deep inspection
    String eventJson = toJson(event);
    log.warn("DLT_EVENT_JSON eventId={} json={}", event.eventId(), eventJson);

    // Heuristic categorization by substring; adjust predicates as error patterns evolve
    categorizeError(exceptionMessage, event);
  }

  private void categorizeError(String error, ResourceEvent event) {
    if (error != null) {
      // Order matters: the first matching category is chosen
      if (error.contains("deserializ") || error.contains("JSON") || error.contains("parse")) {
        log.error(
            "DLT_CATEGORY=SERIALIZATION eventId={} - Malformed message, check data format",
            event.eventId());
      } else if (error.contains("timeout")
          || error.contains("connection")
          || error.contains("unavailable")) {
        log.warn(
            "DLT_CATEGORY=TRANSIENT eventId={} - May retry later when service is available",
            event.eventId());
      } else if (error.contains("validation") || error.contains("constraint")) {
        log.error(
            "DLT_CATEGORY=VALIDATION eventId={} - Data validation failed, fix data and retry",
            event.eventId());
      } else {
        log.error(
            "DLT_CATEGORY=BUSINESS eventId={} - Business logic error, needs investigation",
            event.eventId());
      }
    } else {
      log.warn("DLT_CATEGORY=UNKNOWN eventId={} - No error message available", event.eventId());
    }
  }

  private String toJson(ResourceEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize event {} to JSON: {}", event.eventId(), e.getMessage());
      return String.format(
          "{\"eventId\":\"%s\",\"eventType\":\"%s\",\"resourceId\":\"%s\",\"error\":\"serialization_failed\"}",
          event.eventId(), event.eventType(), event.resourceId());
    }
  }
}
