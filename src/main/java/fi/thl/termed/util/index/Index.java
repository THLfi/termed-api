package fi.thl.termed.util.index;

import fi.thl.termed.util.query.Sort;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Index<K extends Serializable, V> {

  void index(K key, V value);

  void delete(K key);

  boolean isEmpty();

  Stream<V> get(Specification<K, V> specification, List<Sort> sort, int max);

  Stream<K> getKeys(Specification<K, V> specification, List<Sort> sort, int max);

  long count(Specification<K, V> spec);

  Optional<V> get(K id);

  void close();

}
