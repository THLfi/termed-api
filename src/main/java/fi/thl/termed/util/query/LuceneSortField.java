package fi.thl.termed.util.query;

public interface LuceneSortField extends Sort {

  org.apache.lucene.search.SortField toLuceneSortField();

}
