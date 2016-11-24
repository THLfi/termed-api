package fi.thl.termed.util.index;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import fi.thl.termed.util.specification.Specification;

public interface Index<K extends Serializable, V> {

  /**
   * Index values, may be processed parallel/async. If provider gives an empty Optional, any
   * previous values will be removed from this index.
   */
  void index(List<K> keys, Function<K, Optional<V>> valueProvider);

  void index(K key, V value);

  void delete(K key);

  boolean isEmpty();

  List<V> get(Specification<K, V> specification, List<String> sort, int max);

  List<K> getKeys(Specification<K, V> specification, List<String> sort, int max);

  List<V> get(List<K> ids);

  Optional<V> get(K id);

  void close();

}
