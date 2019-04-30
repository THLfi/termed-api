package fi.thl.termed.util.query;

import java.util.Objects;

public abstract class AbstractSelectQualifiedField extends AbstractSelectQualified {

  protected final String field;

  public AbstractSelectQualifiedField(String field) {
    super();
    this.field = field;
  }

  public AbstractSelectQualifiedField(String qualifier, String field) {
    super(qualifier);
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
    if (!super.equals(o)) {
      return false;
    }
    AbstractSelectQualifiedField that = (AbstractSelectQualifiedField) o;
    return Objects.equals(field, that.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), field);
  }

  @Override
  public String toString() {
    return qualifier + field;
  }

}
