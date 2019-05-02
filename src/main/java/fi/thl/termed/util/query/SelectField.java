package fi.thl.termed.util.query;

import java.util.Objects;

public final class SelectField implements Select {

  private final String field;

  public SelectField(String field) {
    this.field = field;
  }

  public String getField() {
    return field;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SelectField that = (SelectField) o;
    return Objects.equals(field, that.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field);
  }

  @Override
  public String toString() {
    return field;
  }

}
