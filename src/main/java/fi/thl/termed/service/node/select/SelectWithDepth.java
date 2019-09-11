package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.AbstractSelectQualifiedField;
import java.util.Objects;

abstract class SelectWithDepth extends AbstractSelectQualifiedField {

  protected int depth;

  public SelectWithDepth(String field) {
    super(field);
    this.depth = 1;
  }

  public SelectWithDepth(String field, int depth) {
    super(field);
    this.depth = depth;
  }

  SelectWithDepth(String qualifier, String field) {
    super(qualifier, field);
    this.depth = 1;
  }

  SelectWithDepth(String qualifier, String field, int depth) {
    super(qualifier, field);
    this.depth = depth;
  }

  public int getDepth() {
    return depth;
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
    SelectWithDepth that = (SelectWithDepth) o;
    return depth == that.depth;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), depth);
  }

}
