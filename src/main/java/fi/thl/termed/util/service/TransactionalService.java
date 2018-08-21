package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionalService<K extends Serializable, V> implements Service<K, V> {

  private Logger log = LoggerFactory.getLogger(getClass());

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
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    return runInTransaction(() -> delegate.save(values, mode, opts, user));
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return runInTransaction(() -> delegate.save(value, mode, opts, user));
  }

  @Override
  public void delete(Stream<K> ids, WriteOptions opts, User user) {
    runInTransaction(() -> {
      delegate.delete(ids, opts, user);
      return null;
    });
  }

  @Override
  public void delete(K key, WriteOptions opts, User user) {
    runInTransaction(() -> {
      delegate.delete(key, opts, user);
      return null;
    });
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return delegate.values(query, user);
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return delegate.keys(query, user);
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
    log.trace("Opening transaction");
    TransactionStatus tx = manager.getTransaction(definition);
    E results;

    try {
      results = supplier.get();
    } catch (RuntimeException | Error e) {
      log.trace("Rolling back transaction");
      manager.rollback(tx);
      throw e;
    }

    log.trace("Committing transaction");
    manager.commit(tx);
    return results;
  }

}
