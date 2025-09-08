package com.kaarelkaasla.enefitresourceservice.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocationTest {

  @Test
  void equals_WithSameId_ShouldReturnTrue() {
    Location location1 =
        Location.builder()
            .id(1L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    Location location2 =
        Location.builder()
            .id(1L)
            .streetAddress("456 Different St")
            .city("Different City")
            .postalCode("99999")
            .countryCode("CA")
            .build();

    assertThat(location1).isEqualTo(location2);
  }

  @Test
  void equals_WithDifferentId_ShouldReturnFalse() {
    Location location1 =
        Location.builder()
            .id(1L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    Location location2 =
        Location.builder()
            .id(2L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    assertThat(location1).isNotEqualTo(location2);
  }

  @Test
  void equals_WithNullIds_SameData_ShouldReturnTrue() {
    Location location1 =
        Location.builder()
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    Location location2 =
        Location.builder()
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    assertThat(location1).isEqualTo(location2);
  }

  @Test
  void equals_WithSameInstance_ShouldReturnTrue() {
    Location location = Location.builder().id(1L).streetAddress("123 Main St").build();

    assertThat(location).isEqualTo(location);
  }

  @Test
  void equals_WithNull_ShouldReturnFalse() {
    Location location = Location.builder().id(1L).streetAddress("123 Main St").build();

    assertThat(location).isNotEqualTo(null);
  }

  @Test
  void equals_WithDifferentClass_ShouldReturnFalse() {
    Location location = Location.builder().id(1L).streetAddress("123 Main St").build();

    assertThat(location).isNotEqualTo("string");
  }

  @Test
  void hashCode_WithId_ShouldBeConsistentWithEquals() {
    Location location =
        Location.builder().id(1L).streetAddress("123 Main St").city("New York").build();

    int hashCode = location.hashCode();
    assertThat(hashCode).isEqualTo(location.hashCode());
    assertThat(hashCode).isNotZero();
  }

  @Test
  void hashCode_WithNullId_ShouldUseDefaultHashCode() {
    Location location = Location.builder().streetAddress("123 Main St").city("New York").build();

    assertThat(location.hashCode()).isNotZero();
  }

  @Test
  void builder_ShouldCreateLocationWithAllFields() {
    Location location =
        Location.builder()
            .id(1L)
            .streetAddress("123 Main St")
            .city("New York")
            .postalCode("10001")
            .countryCode("US")
            .build();

    assertThat(location.getId()).isEqualTo(1L);
    assertThat(location.getStreetAddress()).isEqualTo("123 Main St");
    assertThat(location.getCity()).isEqualTo("New York");
    assertThat(location.getPostalCode()).isEqualTo("10001");
    assertThat(location.getCountryCode()).isEqualTo("US");
  }

  @Test
  void settersAndGetters_ShouldWorkCorrectly() {
    Location location = new Location();

    location.setId(1L);
    location.setStreetAddress("123 Main St");
    location.setCity("New York");
    location.setPostalCode("10001");
    location.setCountryCode("US");

    assertThat(location.getId()).isEqualTo(1L);
    assertThat(location.getStreetAddress()).isEqualTo("123 Main St");
    assertThat(location.getCity()).isEqualTo("New York");
    assertThat(location.getPostalCode()).isEqualTo("10001");
    assertThat(location.getCountryCode()).isEqualTo("US");
  }

  @Test
  void allArgsConstructor_ShouldCreateLocationWithAllFields() {
    Resource testResource = Resource.builder().build();
    Location location = new Location(1L, "123 Main St", "New York", "10001", "US", testResource);

    assertThat(location.getId()).isEqualTo(1L);
    assertThat(location.getStreetAddress()).isEqualTo("123 Main St");
    assertThat(location.getCity()).isEqualTo("New York");
    assertThat(location.getPostalCode()).isEqualTo("10001");
    assertThat(location.getCountryCode()).isEqualTo("US");
    assertThat(location.getResource()).isEqualTo(testResource);
  }
}
