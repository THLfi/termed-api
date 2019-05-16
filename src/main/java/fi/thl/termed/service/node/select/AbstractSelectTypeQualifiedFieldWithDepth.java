package fi.thl.termed.service.node.select;

import fi.thl.termed.domain.TypeId;
import java.util.Objects;

public abstract class AbstractSelectTypeQualifiedFieldWithDepth
    extends AbstractSelectTypeQualifiedField {

  protected int depth;

  AbstractSelectTypeQualifiedFieldWithDepth(TypeId typeId, String field) {
    this(typeId, field, 1);
  }

  AbstractSelectTypeQualifiedFieldWithDepth(TypeId typeId, String field, int depth) {
    super(typeId, field);
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
    AbstractSelectTypeQualifiedFieldWithDepth that = (AbstractSelectTypeQualifiedFieldWithDepth) o;
    return depth == that.depth;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), depth);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + depth;
  }

}
