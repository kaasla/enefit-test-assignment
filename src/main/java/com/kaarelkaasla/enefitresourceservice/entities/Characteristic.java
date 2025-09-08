package com.kaarelkaasla.enefitresourceservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.kaarelkaasla.enefitresourceservice.validation.ValidCharacteristicCode;
import com.kaarelkaasla.enefitresourceservice.validation.ValidCharacteristicType;

import lombok.*;

/**
 * JPA entity for a key/value characteristic associated with a resource.
 * Many-to-one to Resource; equality prefers id when present, otherwise falls back to (code,type).
 */
@Entity
@Table(
    name = "characteristics",
    indexes = {
      @Index(name = "idx_characteristic_code", columnList = "code"),
      @Index(name = "idx_characteristic_type", columnList = "type")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Characteristic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Code is required")
  @Size(max = 5, message = "Code must be maximum 5 characters")
  @ValidCharacteristicCode
  @Column(nullable = false, length = 5)
  private String code;

  @Enumerated(EnumType.STRING)
  @NotNull
  @ValidCharacteristicType
  @Column(nullable = false)
  private CharacteristicType type;

  @NotBlank(message = "Value is required")
  @Column(name = "char_value", nullable = false)
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resource_id", nullable = false)
  private Resource resource;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Characteristic that = (Characteristic) o;
    // Prefer identity: if both are persisted (have IDs), compare by id
    if (this.id != null && that.id != null) {
      return this.id.equals(that.id);
    }
    // For transient objects, fall back to a business key (code + type)
    if (this.code != null ? !this.code.equals(that.code) : that.code != null) return false;
    if (this.type != that.type) return false;
    return true;
  }

  @Override
  public int hashCode() {
    // Keep consistent with equals: id-based for persisted entities
    if (id != null) {
      return id.hashCode();
    }
    // Business key for transient instances
    int result = (code != null ? code.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
