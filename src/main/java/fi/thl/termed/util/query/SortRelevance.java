package fi.thl.termed.util.query;

import org.apache.lucene.search.SortField;

public class SortRelevance implements LuceneSortField {

  public static final SortRelevance INSTANCE = new SortRelevance();

  @Override
  public SortField toLuceneSortField() {
    return SortField.FIELD_SCORE;
  }

  @Override
  public int hashCode() {
    return SortRelevance.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof SortRelevance;
  }

  @Override
  public String toString() {
    return "RELEVANCE";
  }

}
