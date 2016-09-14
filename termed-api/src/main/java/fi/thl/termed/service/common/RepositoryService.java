package fi.thl.termed.service.common;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;

/**
 * Simple implementation of a service that delegates all requests to a repository.
 */
public class RepositoryService<K extends Serializable, V> implements Service<K, V> {

  private Repository<K, V> repository;

  public RepositoryService(Repository<K, V> repository) {
    this.repository = repository;
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User currentUser) {
    return repository.get(specification, currentUser);
  }

  @Override
  public V get(K id, User currentUser) {
    return repository.get(id, currentUser);
  }

  @Override
  public void save(List<V> values, User currentUser) {
    repository.save(values, currentUser);
  }

  @Override
  public void save(V value, User currentUser) {
    repository.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    repository.delete(id, currentUser);
  }

}
