package fi.thl.termed.util.query;

import java.io.Serializable;

public final class Queries {

  private Queries() {
  }

  public static <K extends Serializable, V> Query<K, V> query(
      Specification<K, V> specification) {
    return new Query<>(specification);
  }

  public static <K extends Serializable, V> Query<K, V> sqlQuery(
      SqlSpecification<K, V> specification) {
    return query(Specifications.asSql(specification));
  }

  public static <K extends Serializable, V> Query<K, V> matchAll() {
    return query(Specifications.matchAll());
  }

  public static <K extends Serializable, V> Query<K, V> matchNone() {
    return query(Specifications.matchNone());
  }

}
