package com.kaarelkaasla.enefitresourceservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.kaarelkaasla.enefitresourceservice.entities.CharacteristicType;
import com.kaarelkaasla.enefitresourceservice.validation.ValidCharacteristicCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidCharacteristicType;

public record CharacteristicRequest(
    @NotBlank(message = "Code is required")
        @Size(max = 5, message = "Code must be maximum 5 characters")
        @ValidCharacteristicCode
        String code,
    @NotNull(message = "Type is required") @ValidCharacteristicType CharacteristicType type,
    @NotBlank(message = "Value is required") String value) {}
