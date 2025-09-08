package com.kaarelkaasla.enefitresourceservice.dtos;

public record LocationResponse(
    Long id, String streetAddress, String city, String postalCode, String countryCode) {}
