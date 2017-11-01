package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
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
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    return runInTransaction(() -> delegate.save(values, mode, opts, user));
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return runInTransaction(() -> delegate.save(value, mode, opts, user));
  }

  @Override
  public void delete(List<K> ids, WriteOptions opts, User user) {
    runInTransaction(() -> {
      delegate.delete(ids, opts, user);
      return null;
    });
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    runInTransaction(() -> {
      delegate.delete(id, opts, user);
      return null;
    });
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, SaveMode mode, WriteOptions opts,
      User user) {
    return runInTransaction(() -> delegate.deleteAndSave(deletes, saves, mode, opts, user));
  }

  @Override
  public List<V> getValues(User user) {
    return runInTransaction(() -> delegate.getValues(user));
  }

  @Override
  public List<V> getValues(Specification<K, V> spec, User user) {
    return runInTransaction(() -> delegate.getValues(spec, user));
  }

  @Override
  public List<V> getValues(Query<K, V> query, User user) {
    return runInTransaction(() -> delegate.getValues(query, user));
  }

  @Override
  public Stream<V> getValueStream(User user) {
    return runInTransaction(() -> delegate.getValueStream(user));
  }

  @Override
  public Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return runInTransaction(() -> delegate.getValueStream(spec, user));
  }

  @Override
  public Stream<V> getValueStream(Query<K, V> query, User user) {
    return runInTransaction(() -> delegate.getValueStream(query, user));
  }

  @Override
  public List<K> getKeys(User user) {
    return runInTransaction(() -> delegate.getKeys(user));
  }

  @Override
  public List<K> getKeys(Specification<K, V> spec, User user) {
    return runInTransaction(() -> delegate.getKeys(spec, user));
  }

  @Override
  public List<K> getKeys(Query<K, V> query, User user) {
    return runInTransaction(() -> delegate.getKeys(query, user));
  }

  @Override
  public Stream<K> getKeyStream(User user) {
    return runInTransaction(() -> delegate.getKeyStream(user));
  }

  @Override
  public Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return runInTransaction(() -> delegate.getKeyStream(spec, user));
  }

  @Override
  public Stream<K> getKeyStream(Query<K, V> query, User user) {
    return runInTransaction(() -> delegate.getKeyStream(query, user));
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    return runInTransaction(() -> delegate.count(spec, user));
  }

  @Override
  public boolean exists(K id, User user) {
    return runInTransaction(() -> delegate.exists(id, user));
  }

  @Override
  public Optional<V> get(K id, User user, Select... selects) {
    return runInTransaction(() -> delegate.get(id, user, selects));
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
