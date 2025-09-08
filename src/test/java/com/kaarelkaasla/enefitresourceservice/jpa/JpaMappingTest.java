package com.kaarelkaasla.enefitresourceservice.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kaarelkaasla.enefitresourceservice.entities.Characteristic;
import com.kaarelkaasla.enefitresourceservice.entities.Location;
import com.kaarelkaasla.enefitresourceservice.entities.Resource;

class JpaMappingTest {

  @Test
  @DisplayName("Resource entity has versioning and correct table/columns")
  void resourceEntityMapping() throws NoSuchFieldException {
    assertThat(Resource.class.isAnnotationPresent(Entity.class)).isTrue();
    Table table = Resource.class.getAnnotation(Table.class);
    assertThat(table).isNotNull();
    assertThat(table.name()).isEqualTo("resources");

    Field version = Resource.class.getDeclaredField("version");
    assertThat(version.isAnnotationPresent(Version.class)).isTrue();

    Field country = Resource.class.getDeclaredField("countryCode");
    Column countryCol = country.getAnnotation(Column.class);
    assertThat(countryCol).isNotNull();
    assertThat(countryCol.length()).isEqualTo(2);
    assertThat(countryCol.nullable()).isFalse();
  }

  @Test
  @DisplayName("Location entity has constrained columns")
  void locationEntityMapping() throws NoSuchFieldException {
    assertThat(Location.class.isAnnotationPresent(Entity.class)).isTrue();
    Table table = Location.class.getAnnotation(Table.class);
    assertThat(table).isNotNull();
    assertThat(table.name()).isEqualTo("locations");

    Field postal = Location.class.getDeclaredField("postalCode");
    Column postalCol = postal.getAnnotation(Column.class);
    assertThat(postalCol).isNotNull();
    assertThat(postalCol.length()).isEqualTo(5);

    Field cc = Location.class.getDeclaredField("countryCode");
    Column ccCol = cc.getAnnotation(Column.class);
    assertThat(ccCol).isNotNull();
    assertThat(ccCol.length()).isEqualTo(2);
  }

  @Test
  @DisplayName("Characteristic entity has constrained code field")
  void characteristicEntityMapping() throws NoSuchFieldException {
    assertThat(Characteristic.class.isAnnotationPresent(Entity.class)).isTrue();
    Table table = Characteristic.class.getAnnotation(Table.class);
    assertThat(table).isNotNull();
    assertThat(table.name()).isEqualTo("characteristics");

    Field code = Characteristic.class.getDeclaredField("code");
    Column codeCol = code.getAnnotation(Column.class);
    assertThat(codeCol).isNotNull();
    assertThat(codeCol.length()).isEqualTo(5);
    assertThat(codeCol.nullable()).isFalse();
  }

  @SuppressWarnings("unused")
  private static <A extends Annotation> A getAnnotation(Field f, Class<A> type) {
    return f.getAnnotation(type);
  }
}
