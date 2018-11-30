package fi.thl.termed.util.query;

import java.io.Serializable;

public final class Specifications {

  private Specifications() {
  }

  public static <K extends Serializable, V> Specification<K, V> matchAll() {
    return new MatchAll<>();
  }

  public static <K extends Serializable, V> Specification<K, V> matchNone() {
    return new MatchNone<>();
  }

  /**
   * Wrap given specification to forwarding SQL specification to ensure that specification is "SQL
   * only".
   */
  public static <K extends Serializable, V> SqlSpecification<K, V> asSql(
      SqlSpecification<K, V> specification) {
    return new ForwardingSqlSpecification<>(specification);
  }

  /**
   * Wrap given specification to forwarding Lucene specification to ensure that specification is
   * "Lucene only".
   */
  public static <K extends Serializable, V> LuceneSpecification<K, V> asLucene(
      LuceneSpecification<K, V> specification) {
    return new ForwardingLuceneSpecification<>(specification);
  }

}
