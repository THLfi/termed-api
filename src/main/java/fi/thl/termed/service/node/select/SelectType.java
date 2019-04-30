package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.Select;

public class SelectType implements Select {

  @Override
  public String toString() {
    return "type";
  }

  @Override
  public int hashCode() {
    return SelectType.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof SelectType;
  }

}
