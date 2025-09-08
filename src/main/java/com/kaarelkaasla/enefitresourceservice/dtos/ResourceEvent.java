package com.kaarelkaasla.enefitresourceservice.dtos;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ResourceEvent(
    ResourceEventType eventType,
    Long resourceId,
    ResourceResponse resource,
    @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime eventTimestamp,
    String eventId) {}
