package com.kaarelkaasla.enefitresourceservice.dtos;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import com.kaarelkaasla.enefitresourceservice.entities.ResourceType;
import com.kaarelkaasla.enefitresourceservice.validation.MatchingCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidResourceType;
import com.kaarelkaasla.enefitresourceservice.validation.ValidationConstants;

@MatchingCountryCode
public record PatchResourceRequest(
    Optional<@ValidResourceType ResourceType> type,
    Optional<
            @Pattern(
                regexp = ValidationConstants.COUNTRY_CODE_PATTERN,
                message = ValidationConstants.COUNTRY_CODE_MESSAGE)
            @ValidCountryCode String>
        countryCode,
    @Valid Optional<LocationRequest> location,
    @Valid Optional<Set<@Valid CharacteristicRequest>> characteristics) {}
