package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.Select;

public abstract class SelectWithDepth extends Select {

  private int depth;

  SelectWithDepth(String field) {
    this(field, 1);
  }

  SelectWithDepth(String field, int depth) {
    super(field);
    this.depth = depth;
  }

  public int getDepth() {
    return depth;
  }

}
