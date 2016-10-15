package fi.thl.termed.domain;

/**
 * Represents a value for granted permission. Has only one possible value
 * GrantedPermission.INSTANCE.
 */
public final class GrantedPermission {

  public static final GrantedPermission INSTANCE = new GrantedPermission();

  private GrantedPermission() {
  }

  @Override
  public int hashCode() {
    return GrantedPermission.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof GrantedPermission;
  }

}
