package com.kaarelkaasla.enefitresourceservice.dtos;

public record FieldError(String field, Object rejectedValue, String message) {}
