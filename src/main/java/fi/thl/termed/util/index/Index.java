package fi.thl.termed.util.index;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;

public interface Index<K extends Serializable, V> {

  /**
   * Index values, may be processed parallel/async. If provider gives an empty Optional, any
   * previous values will be removed from this index.
   */
  void index(List<K> keys, Function<K, Optional<V>> valueProvider);

  void index(K key, V value);

  void delete(K key);

  boolean isEmpty();

  Results<V> get(Query<K, V> specification);

  Results<K> getKeys(Query<K, V> specification);

  List<V> get(List<K> ids);

  Optional<V> get(K id);

  void close();

}
