package fi.thl.termed.util.query;

import java.util.Objects;

public abstract class AbstractSelectQualified implements Select {

  protected final String qualifier;

  public AbstractSelectQualified() {
    this("");
  }

  public AbstractSelectQualified(String qualifier) {
    this.qualifier = qualifier;
  }

  public String getQualifier() {
    return qualifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractSelectQualified that = (AbstractSelectQualified) o;
    return Objects.equals(qualifier, that.qualifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(qualifier);
  }

}
