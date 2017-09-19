package fi.thl.termed.util.index;

import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Index<K extends Serializable, V> {

  /**
   * Index values, may be processed parallel/async. If provider gives an empty Optional, any
   * previous values will be removed from this index.
   */
  void index(List<K> keys, Function<K, Optional<V>> valueProvider);

  void index(K key, V value);

  void delete(K key);

  boolean isEmpty();

  Stream<V> get(Specification<K, V> specification, List<String> sort, int max);

  Stream<K> getKeys(Specification<K, V> specification, List<String> sort, int max);

  long count(Specification<K, V> spec);

  Stream<V> get(List<K> ids);

  Optional<V> get(K id);

  void close();

}
