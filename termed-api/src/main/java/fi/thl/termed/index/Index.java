package fi.thl.termed.index;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;

public interface Index<K extends Serializable, V> {

  void reindex(List<K> ids, Function<K, V> objectLoadingFunction);

  void reindex(K key, V object);

  List<V> query(Query query);

  void deleteFromIndex(K id);

  int indexSize();

}
