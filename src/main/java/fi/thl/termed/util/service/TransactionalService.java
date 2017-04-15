package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
  public List<V> get(Specification<K, V> specification, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.get(specification, args, user));
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.getKeys(specification, args, user));
  }

  @Override
  public List<V> get(List<K> ids, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.get(ids, args, user));
  }

  @Override
  public Optional<V> get(K id, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.get(id, args, user));
  }

  @Override
  public List<K> save(List<V> values, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.save(values, args, user));
  }

  @Override
  public K save(V value, Map<String, Object> args, User user) {
    return runInTransaction(() -> delegate.save(value, args, user));
  }

  @Override
  public void delete(List<K> ids, Map<String, Object> args, User user) {
    runInTransaction(() -> delegate.delete(ids, args, user));
  }

  @Override
  public void delete(K id, Map<String, Object> args, User user) {
    runInTransaction(() -> delegate.delete(id, args, user));
  }

  private void runInTransaction(Runnable runnable) {
    runInTransaction((Supplier<Void>) () -> {
      runnable.run();
      return null;
    });
  }

  private <E> E runInTransaction(Supplier<E> supplier) {
    TransactionStatus tx = manager.getTransaction(definition);
    E results;
    try {
      results = supplier.get();
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return results;
  }

}
