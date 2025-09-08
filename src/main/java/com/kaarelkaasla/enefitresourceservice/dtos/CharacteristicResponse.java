package com.kaarelkaasla.enefitresourceservice.dtos;

import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;

public record CharacteristicResponse(Long id, String code, CharacteristicType type, String value) {}
