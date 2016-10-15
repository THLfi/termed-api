package fi.thl.termed.util.index;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.util.specification.SpecificationQuery;

public interface Index<K extends Serializable, V> {

  void reindex(List<K> ids, java.util.function.Function<K, V> objectLoadingFunction);

  void reindex(K key, V object);

  List<V> query(SpecificationQuery<K, V> specification);

  void deleteFromIndex(K id);

  int indexSize();

}
