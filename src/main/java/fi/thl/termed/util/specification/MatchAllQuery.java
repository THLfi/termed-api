package fi.thl.termed.util.specification;

import java.io.Serializable;
import java.util.List;

public class MatchAllQuery<K extends Serializable, V> extends Query<K, V> {

  public MatchAllQuery() {
    super(new MatchAll<>());
  }

  public MatchAllQuery(Engine engine) {
    super(new MatchAll<>(), engine);
  }

  public MatchAllQuery(List<String> orderBy, int max, Engine engine) {
    super(new MatchAll<>(), orderBy, max, engine);
  }

}
