package fi.thl.termed.util.query;

import java.util.Objects;

public class Select {

  private final String field;

  public Select(String field) {
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
    Select select = (Select) o;
    return Objects.equals(field, select.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field);
  }

}
