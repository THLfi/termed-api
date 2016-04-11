package fi.thl.termed.repository;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;

public interface Index<V, K extends Serializable> {

  void reindex(List<K> ids, Function<List<K>, List<V>> objectLoadingFunction);

  void reindex(K key, V object);

  List<V> query(Query query);

  void deleteFromIndex(K id);

  int indexSize();

}
