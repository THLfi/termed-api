package fi.thl.termed.repository.dao;

import java.io.Serializable;
import java.util.Map;

/**
 * Separate interface classes providing just mass inserting functionality.
 */
public interface BulkInsertDao<K extends Serializable, V> {

  void insert(Map<K, V> resources);

  boolean isSupported();

}
