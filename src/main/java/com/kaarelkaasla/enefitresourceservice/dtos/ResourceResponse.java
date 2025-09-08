package com.kaarelkaasla.enefitresourceservice.dtos;

import java.time.OffsetDateTime;
import java.util.Set;

import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;

public record ResourceResponse(
    Long id,
    ResourceType type,
    String countryCode,
    Long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    LocationResponse location,
    Set<CharacteristicResponse> characteristics) {}
