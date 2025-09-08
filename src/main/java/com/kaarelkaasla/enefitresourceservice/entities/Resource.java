package com.kaarelkaasla.enefitresourceservice.entities;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kaarelkaasla.enefitresourceservice.validation.ValidCountryCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidResourceType;
import com.kaarelkaasla.enefitresourceservice.validation.ValidationConstants;

import lombok.*;

/**
 * JPA entity representing a resource (metering or connection point) with location and characteristics.
 * Uses @Version for optimistic locking and JPA auditing;
 * helper methods maintain both sides of the relationships with orphan removal for characteristics.
 */
@Entity
@Table(
    name = "resources",
    indexes = {
      @Index(name = "idx_resource_country_code", columnList = "countryCode"),
      @Index(name = "idx_resource_type", columnList = "type")
    })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Enumerated(EnumType.STRING)
  @NotNull
  @ValidResourceType
  @Column(nullable = false)
  private ResourceType type;

  @NotNull
  @Pattern(
      regexp = ValidationConstants.COUNTRY_CODE_PATTERN,
      message = ValidationConstants.COUNTRY_CODE_MESSAGE)
  @ValidCountryCode
  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Version
  @Column(nullable = false)
  private Long version;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @OneToOne(mappedBy = "resource", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Location location;

  @OneToMany(
      mappedBy = "resource",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Builder.Default
  private Set<Characteristic> characteristics = new HashSet<>();

  public void addCharacteristic(Characteristic characteristic) {
    characteristics.add(characteristic);
    characteristic.setResource(this);
  }

  public void setLocation(Location location) {
    this.location = location;
    if (location != null) {
      location.setResource(this);
    }
  }
}
