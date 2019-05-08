package fi.thl.termed.util.query;

import java.util.Objects;

public class SortField implements Sort {

  final String field;
  final boolean desc;

  SortField(String field) {
    this.field = field;
    this.desc = false;
  }

  SortField(String field, boolean desc) {
    this.field = field;
    this.desc = desc;
  }

  public String getField() {
    return field;
  }

  public boolean isDesc() {
    return desc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SortField sortField = (SortField) o;
    return desc == sortField.desc &&
        Objects.equals(field, sortField.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, desc);
  }

  @Override
  public String toString() {
    return "'" + field + "'" + (desc ? " DESC" : "");
  }

}
