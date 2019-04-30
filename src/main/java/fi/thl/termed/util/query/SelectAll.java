package fi.thl.termed.util.query;

public final class SelectAll implements Select {

  @Override
  public String toString() {
    return "*";
  }

  @Override
  public int hashCode() {
    return SelectAll.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof SelectAll;
  }

}
