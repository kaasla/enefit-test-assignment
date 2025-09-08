package com.kaarelkaasla.enefitresourceservice.dtos;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;
import com.kaarelkaasla.enefitresourceservice.validation.MatchingCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidResourceType;
import com.kaarelkaasla.enefitresourceservice.validation.ValidationConstants;

@MatchingCountryCode
public record ResourceRequest(
    @NotNull(message = "Resource type is required") @ValidResourceType ResourceType type,
    @NotNull(message = "Country code is required")
        @Pattern(
            regexp = ValidationConstants.COUNTRY_CODE_PATTERN,
            message = ValidationConstants.COUNTRY_CODE_MESSAGE)
        @ValidCountryCode
        String countryCode,
    @NotNull(message = "Location is required") @Valid LocationRequest location,
    @Valid Set<CharacteristicRequest> characteristics) {}
