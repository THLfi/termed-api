package fi.thl.termed.util.query;

public final class Sorts {

  private Sorts() {
  }

  public static Sort sort(String field) {
    return new LuceneSortField(field);
  }

  public static Sort sort(String field, boolean desc) {
    return new LuceneSortField(field, desc);
  }

  public static Sort sortDesc(String field) {
    return new LuceneSortField(field, true);
  }

}
