package fi.thl.termed.util.service;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.util.specification.SpecificationQuery;

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
  public List<V> get(List<K> ids, User currentUser) {
    return repository.get(ids, currentUser);
  }

  @Override
  public Optional<V> get(K id, User currentUser) {
    return repository.get(id, currentUser);
  }

  @Override
  public List<K> save(List<V> values, User currentUser) {
    return repository.save(values, currentUser);
  }

  @Override
  public K save(V value, User currentUser) {
    return repository.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    repository.delete(id, currentUser);
  }

}
