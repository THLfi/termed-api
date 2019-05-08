package fi.thl.termed.util.query;

public interface Sort {

  static Sort sort(String field) {
    return new LuceneSortField(field);
  }

  static Sort sort(String field, boolean desc) {
    return new LuceneSortField(field, desc);
  }

  static Sort sortDesc(String field) {
    return new LuceneSortField(field, true);
  }

}
