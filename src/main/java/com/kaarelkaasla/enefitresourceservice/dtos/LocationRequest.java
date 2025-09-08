package com.kaarelkaasla.enefitresourceservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.kaarelkaasla.enefitresourceservice.validation.ValidCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidPostalCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidationConstants;

public record LocationRequest(
    @NotBlank(message = "Street address is required") String streetAddress,
    @NotBlank(message = "City is required") String city,
    @NotBlank(message = "Postal code is required")
        @Pattern(
            regexp = ValidationConstants.POSTAL_CODE_PATTERN,
            message = ValidationConstants.POSTAL_CODE_MESSAGE)
        @ValidPostalCode
        String postalCode,
    @Pattern(
            regexp = ValidationConstants.COUNTRY_CODE_PATTERN,
            message = ValidationConstants.COUNTRY_CODE_MESSAGE)
        @ValidCountryCode
        String countryCode) {}
