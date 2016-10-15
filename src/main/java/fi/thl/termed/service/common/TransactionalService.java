package fi.thl.termed.service.common;

import com.google.common.base.Optional;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;

public class TransactionalService<K extends Serializable, V> extends ForwardingService<K, V> {

  private PlatformTransactionManager manager;
  private TransactionDefinition definition;

  public TransactionalService(Service<K, V> delegate, PlatformTransactionManager manager) {
    this(delegate, manager, new DefaultTransactionDefinition());
  }

  public TransactionalService(Service<K, V> delegate, PlatformTransactionManager manager,
                              TransactionDefinition definition) {
    super(delegate);
    this.manager = manager;
    this.definition = definition;
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    List<V> values;
    try {
      values = super.get(specification, currentUser);
    } catch (RuntimeException e) {
      manager.rollback(tx);
      throw e;
    } catch (Error e) {
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
      value = super.get(id, currentUser);
    } catch (RuntimeException e) {
      manager.rollback(tx);
      throw e;
    } catch (Error e) {
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
      keys = super.save(values, currentUser);
    } catch (RuntimeException e) {
      manager.rollback(tx);
      throw e;
    } catch (Error e) {
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
      key = super.save(value, currentUser);
    } catch (RuntimeException e) {
      manager.rollback(tx);
      throw e;
    } catch (Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return key;
  }

  @Override
  public void delete(K id, User currentUser) {
    TransactionStatus tx = manager.getTransaction(definition);
    try {
      super.delete(id, currentUser);
    } catch (RuntimeException e) {
      manager.rollback(tx);
      throw e;
    } catch (Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
  }

}
