package com.kaarelkaasla.enefitresourceservice.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacteristicTest {

  @Test
  void equals_WithSameId_ShouldReturnTrue() {
    Characteristic char1 =
        Characteristic.builder()
            .id(1L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    Characteristic char2 =
        Characteristic.builder()
            .id(1L)
            .code("DIFFERENT")
            .type(CharacteristicType.CONNECTION_POINT_STATUS)
            .value("DIFFERENT")
            .build();

    assertThat(char1).isEqualTo(char2);
  }

  @Test
  void equals_WithDifferentId_ShouldReturnFalse() {
    Characteristic char1 =
        Characteristic.builder()
            .id(1L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    Characteristic char2 =
        Characteristic.builder()
            .id(2L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    assertThat(char1).isNotEqualTo(char2);
  }

  @Test
  void equals_WithNullIds_SameBusinessKey_ShouldReturnTrue() {
    Characteristic char1 =
        Characteristic.builder()
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    Characteristic char2 =
        Characteristic.builder()
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("DIFFERENT_VALUE")
            .build();

    assertThat(char1).isEqualTo(char2);
  }

  @Test
  void equals_WithNullIds_DifferentCode_ShouldReturnFalse() {
    Characteristic char1 =
        Characteristic.builder()
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    Characteristic char2 =
        Characteristic.builder()
            .code("TEST2")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    assertThat(char1).isNotEqualTo(char2);
  }

  @Test
  void equals_WithNullIds_DifferentType_ShouldReturnFalse() {
    Characteristic char1 =
        Characteristic.builder()
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .value("RESIDENTIAL")
            .build();

    Characteristic char2 =
        Characteristic.builder()
            .code("TEST1")
            .type(CharacteristicType.CONNECTION_POINT_STATUS)
            .value("RESIDENTIAL")
            .build();

    assertThat(char1).isNotEqualTo(char2);
  }

  @Test
  void equals_WithNullCode_ShouldReturnFalse() {
    Characteristic char1 =
        Characteristic.builder().code(null).type(CharacteristicType.CONSUMPTION_TYPE).build();

    Characteristic char2 =
        Characteristic.builder().code("TEST1").type(CharacteristicType.CONSUMPTION_TYPE).build();

    assertThat(char1).isNotEqualTo(char2);
  }

  @Test
  void equals_WithBothNullCodes_SameType_ShouldReturnTrue() {
    Characteristic char1 =
        Characteristic.builder().type(CharacteristicType.CONSUMPTION_TYPE).build();

    Characteristic char2 =
        Characteristic.builder().type(CharacteristicType.CONSUMPTION_TYPE).build();

    assertThat(char1).isEqualTo(char2);
  }

  @Test
  void equals_WithSameInstance_ShouldReturnTrue() {
    Characteristic characteristic =
        Characteristic.builder().code("TEST1").type(CharacteristicType.CONSUMPTION_TYPE).build();

    assertThat(characteristic).isEqualTo(characteristic);
  }

  @Test
  void equals_WithNull_ShouldReturnFalse() {
    Characteristic characteristic =
        Characteristic.builder().code("TEST1").type(CharacteristicType.CONSUMPTION_TYPE).build();

    assertThat(characteristic).isNotEqualTo(null);
  }

  @Test
  void equals_WithDifferentClass_ShouldReturnFalse() {
    Characteristic characteristic =
        Characteristic.builder().code("TEST1").type(CharacteristicType.CONSUMPTION_TYPE).build();

    assertThat(characteristic).isNotEqualTo("string");
  }

  @Test
  void hashCode_WithId_ShouldUseIdHashCode() {
    Characteristic characteristic =
        Characteristic.builder()
            .id(1L)
            .code("TEST1")
            .type(CharacteristicType.CONSUMPTION_TYPE)
            .build();

    assertThat(characteristic.hashCode()).isEqualTo(1L);
  }

  @Test
  void hashCode_WithoutId_ShouldUseBusinessKeyHashCode() {
    Characteristic characteristic =
        Characteristic.builder().code("TEST1").type(CharacteristicType.CONSUMPTION_TYPE).build();

    int result = "TEST1".hashCode();
    result = 31 * result + CharacteristicType.CONSUMPTION_TYPE.hashCode();
    assertThat(characteristic.hashCode()).isEqualTo(result);
  }

  @Test
  void hashCode_WithNullCodeAndType_ShouldNotThrowException() {
    Characteristic characteristic = Characteristic.builder().build();

    assertThat(characteristic.hashCode()).isZero();
  }

  @Test
  void hashCode_WithNullCode_ShouldHandleGracefully() {
    Characteristic characteristic =
        Characteristic.builder().type(CharacteristicType.CONSUMPTION_TYPE).build();

    int result = 0;
    result = 31 * result + CharacteristicType.CONSUMPTION_TYPE.hashCode();
    assertThat(characteristic.hashCode()).isEqualTo(result);
  }
}
