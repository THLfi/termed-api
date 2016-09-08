package fi.thl.termed.index;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.spesification.SpecificationQuery;

public interface Index<K extends Serializable, V> {

  void reindex(List<K> ids, Function<K, V> objectLoadingFunction);

  void reindex(K key, V object);

  List<V> query(SpecificationQuery<K, V> specification);

  void deleteFromIndex(K id);

  int indexSize();

}
