package fi.thl.termed.util.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;

public class TransactionalService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  private PlatformTransactionManager manager;
  private TransactionDefinition definition;

  public TransactionalService(Service<K, V> delegate, PlatformTransactionManager manager) {
    this(delegate, manager, new DefaultTransactionDefinition());
  }

  public TransactionalService(Service<K, V> delegate, PlatformTransactionManager manager,
                              TransactionDefinition definition) {
    this.delegate = delegate;
    this.manager = manager;
    this.definition = definition;
  }

  @Override
  public Results<V> get(Query<K, V> specification, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    Results<V> results;
    try {
      results = delegate.get(specification, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return results;
  }

  @Override
  public Results<K> getKeys(Query<K, V> query, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    Results<K> results;
    try {
      results = delegate.getKeys(query, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return results;
  }

  @Override
  public List<V> get(List<K> ids, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    List<V> values;
    try {
      values = delegate.get(ids, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return values;
  }

  @Override
  public Optional<V> get(K id, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    Optional<V> value;
    try {
      value = delegate.get(id, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return value;
  }

  @Override
  public List<K> save(List<V> values, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    List<K> keys;
    try {
      keys = delegate.save(values, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return keys;
  }

  @Override
  public K save(V value, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    K key;
    try {
      key = delegate.save(value, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return key;
  }

  @Override
  public void delete(List<K> ids, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    try {
      delegate.delete(ids, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
  }

  @Override
  public void delete(K id, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    try {
      delegate.delete(id, currentUser);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
  }

}
