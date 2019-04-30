package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.Select;

public class SelectId implements Select {

  @Override
  public String toString() {
    return "id";
  }

  @Override
  public int hashCode() {
    return SelectId.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof SelectId;
  }

}
