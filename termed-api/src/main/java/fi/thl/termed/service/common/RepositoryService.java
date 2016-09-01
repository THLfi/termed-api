package fi.thl.termed.service.common;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

/**
 * Simple implementation of a service that delegates all requests to a repository.
 */
public class RepositoryService<K extends Serializable, V> implements Service<K, V> {

  private Repository<K, V> repository;

  public RepositoryService(Repository<K, V> repository) {
    this.repository = repository;
  }

  @Override
  public List<V> get(User currentUser) {
    return repository.get();
  }

  @Override
  public List<V> get(Specification<K, V> specification, User currentUser) {
    return repository.get(specification);
  }

  @Override
  public List<V> get(Query query, User currentUser) {
    // text search is not supported by Repository, fall back to returning all
    return repository.get();
  }

  @Override
  public V get(K id, User currentUser) {
    return repository.get(id);
  }

  @Override
  public void save(List<V> values, User currentUser) {
    repository.save(values);
  }

  @Override
  public void save(V value, User currentUser) {
    repository.save(value);
  }

  @Override
  public void delete(K id, User currentUser) {
    repository.delete(id);
  }

}
