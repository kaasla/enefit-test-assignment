package com.kaarelkaasla.enefitresourceservice.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BatchNotificationResponse(
    UUID operationId,
    int resourceCount,
    String status,
    OffsetDateTime processedAt,
    ResourceEventType operation) {}
