package com.kaarelkaasla.enefitresourceservice.validation;

public final class ValidationConstants {

  public static final String COUNTRY_CODE_PATTERN = "^[A-Z]{2}$";
  public static final String COUNTRY_CODE_MESSAGE =
      "Country code must be ISO 3166-1 alpha-2 format";

  public static final String POSTAL_CODE_PATTERN = "^\\d{5}$";
  public static final String POSTAL_CODE_MESSAGE = "Postal code must be exactly 5 digits";

  private ValidationConstants() {}
}
