package com.kaarelkaasla.enefitresourceservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.kaarelkaasla.enefitresourceservice.validation.ValidCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidPostalCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidationConstants;

import lombok.*;

/**
 * JPA entity for a resource's address and country code.
 * Mapped one-to-one via resource_id and holds the back-reference to the owning Resource.
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @NotBlank(message = "Street address is required")
  @Column(name = "street_address", nullable = false)
  private String streetAddress;

  @NotBlank(message = "City is required")
  @Column(nullable = false)
  private String city;

  @NotBlank(message = "Postal code is required")
  @Pattern(
      regexp = ValidationConstants.POSTAL_CODE_PATTERN,
      message = ValidationConstants.POSTAL_CODE_MESSAGE)
  @ValidPostalCode
  @Column(name = "postal_code", nullable = false, length = 5)
  private String postalCode;

  @Pattern(
      regexp = ValidationConstants.COUNTRY_CODE_PATTERN,
      message = ValidationConstants.COUNTRY_CODE_MESSAGE)
  @ValidCountryCode
  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resource_id", nullable = false)
  private Resource resource;
}
