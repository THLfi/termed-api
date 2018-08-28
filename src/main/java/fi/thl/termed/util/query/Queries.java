package fi.thl.termed.util.query;

import java.io.Serializable;

public final class Queries {

  private Queries() {
  }

  public static <K extends Serializable, V> Query<K, V> matchAll() {
    return new Query<>(new MatchAll<>());
  }

  public static <K extends Serializable, V> Query<K, V> matchNone() {
    return new Query<>(new MatchNone<>());
  }

}
