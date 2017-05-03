package fi.thl.termed.service.node.select;

public abstract class SingletonSelect implements Select {

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return this == other || getClass().isInstance(other);
  }

}
