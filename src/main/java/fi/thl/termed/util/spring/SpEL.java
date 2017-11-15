package fi.thl.termed.util.spring;

public final class SpEL {

  private SpEL() {
  }

  public static final String EMPTY_LIST = "#{T(java.util.Collections).emptyList()}";

}
