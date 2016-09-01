package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.util.List;

public class Query {

  public static final String MATCH_ALL_QUERY = "*:*";
  public static final int DEFAULT_MAX_RESULTS = 100;

  private String query;

  private int max;

  private List<String> orderBy;

  public Query() {
    this(MATCH_ALL_QUERY);
  }

  public Query(String query) {
    this(query, DEFAULT_MAX_RESULTS);
  }

  public Query(int max) {
    this(MATCH_ALL_QUERY, max);
  }

  public Query(String query, int max) {
    this.query = query;
    this.max = max;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public List<String> getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(List<String> orderBy) {
    this.orderBy = orderBy;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("query", query)
        .add("max", max)
        .add("orderBy", orderBy)
        .toString();
  }

}
