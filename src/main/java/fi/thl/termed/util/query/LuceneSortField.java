package fi.thl.termed.util.query;

import static org.apache.lucene.search.SortField.Type.STRING;

public class LuceneSortField extends SortField {

  LuceneSortField(String field) {
    super(field);
  }

  LuceneSortField(String field, boolean desc) {
    super(field, desc);
  }

  public org.apache.lucene.search.SortField toLuceneSortField() {
    return new org.apache.lucene.search.SortField(
        field + ".sortable", STRING, desc);
  }

}
