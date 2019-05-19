package fi.thl.termed.util.query;

import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Query<K extends Serializable, V> {

  private List<Select> select;
  private Specification<K, V> where;
  private List<Sort> sort;
  private int max;

  public Query(Specification<K, V> where) {
    this.select = singletonList(new SelectAll());
    this.where = where;
    this.sort = singletonList(new SortRelevance());
    this.max = -1;
  }

  public Query(Iterable<Select> select, Specification<K, V> where) {
    this.select = ImmutableList.copyOf(select);
    this.where = where;
    this.sort = singletonList(new SortRelevance());
    this.max = -1;
  }

  public Query(Specification<K, V> where, List<Sort> sort, int max) {
    this.select = singletonList(new SelectAll());
    this.where = where;
    this.sort = sort;
    this.max = max;
  }

  public Query(Iterable<Select> select, Specification<K, V> where, List<Sort> sort, int max) {
    this.select = ImmutableList.copyOf(select);
    this.where = where;
    this.sort = sort;
    this.max = max;
  }

  public List<Select> getSelect() {
    return select;
  }

  public Specification<K, V> getWhere() {
    return where;
  }

  public List<Sort> getSort() {
    return sort;
  }

  public int getMax() {
    return max;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Query<?, ?> query = (Query<?, ?>) o;
    return max == query.max &&
        Objects.equals(select, query.select) &&
        Objects.equals(where, query.where) &&
        Objects.equals(sort, query.sort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(select, where, sort, max);
  }

  @Override
  public String toString() {
    String selectString = "SELECT " + select.stream()
        .map(Select::toString)
        .sorted()
        .collect(Collectors.joining(", "));

    String whereString = " WHERE " + where;

    String sortString = sort.isEmpty() ? "" : " SORT " + sort.stream()
        .map(Sort::toString)
        .collect(Collectors.joining(", "));

    String maxString = max == -1 ? "" : " MAX " + max;

    return selectString + whereString + sortString + maxString;
  }

}
