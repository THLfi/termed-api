package fi.thl.termed.util.query;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Query<K extends Serializable, V> {

  private List<Select> select;
  private Specification<K, V> where;
  private List<String> sort;
  private int max;

  public Query(Specification<K, V> where) {
    this.select = singletonList(new SelectAll());
    this.where = where;
    this.sort = emptyList();
    this.max = -1;
  }

  public Query(Iterable<Select> select, Specification<K, V> where) {
    this.select = ImmutableList.copyOf(select);
    this.where = where;
    this.sort = emptyList();
    this.max = -1;
  }

  public Query(Specification<K, V> where, List<String> sort, int max) {
    this.select = singletonList(new SelectAll());
    this.where = where;
    this.sort = sort;
    this.max = max;
  }

  public Query(Iterable<Select> select, Specification<K, V> where, List<String> sort, int max) {
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

  public List<String> getSort() {
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

}
