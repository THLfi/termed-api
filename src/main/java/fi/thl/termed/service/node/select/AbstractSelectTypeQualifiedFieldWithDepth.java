package fi.thl.termed.service.node.select;

import fi.thl.termed.domain.TypeId;

public abstract class AbstractSelectTypeQualifiedFieldWithDepth extends
    AbstractSelectTypeQualifiedField {

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

}
