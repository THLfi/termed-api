package fi.thl.termed.util;

public final class RegularExpressions {

  public static final String ALL = "(?s)^.*$";

  public static final String CODE = "^[A-Za-z0-9_\\-]+$";

  public static final String UUID =
      "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

  private RegularExpressions() {
  }

}
