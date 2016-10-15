package fi.thl.termed.domain;

/**
 * Empty simply represents an object that has a one possible value, Empty.INSTANCE. Can be used for
 * example to represent a value that has not other property that it exists.
 */
public final class Empty {

  public static final Empty INSTANCE = new Empty();

  private Empty() {
  }

  @Override
  public int hashCode() {
    return Empty.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof Empty;
  }

}
