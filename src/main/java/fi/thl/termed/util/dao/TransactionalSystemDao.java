package fi.thl.termed.util.dao;

import fi.thl.termed.util.collect.Tuple2;
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

public class TransactionalSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private SystemDao<K, V> delegate;

  private PlatformTransactionManager manager;
  private TransactionDefinition definition;

  public TransactionalSystemDao(SystemDao<K, V> delegate, PlatformTransactionManager manager) {
    this(delegate, manager, new DefaultTransactionDefinition());
  }

  public TransactionalSystemDao(SystemDao<K, V> delegate, PlatformTransactionManager manager,
      TransactionDefinition definition) {
    this.delegate = delegate;
    this.manager = manager;
    this.definition = definition;
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    runInTransaction(() -> {
      delegate.insert(entries);
      return null;
    });
  }

  @Override
  public void insert(K key, V value) {
    runInTransaction(() -> {
      delegate.insert(key, value);
      return null;
    });
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries) {
    runInTransaction(() -> {
      delegate.update(entries);
      return null;
    });
  }

  @Override
  public void update(K key, V value) {
    runInTransaction(() -> {
      delegate.update(key, value);
      return null;
    });
  }

  @Override
  public void delete(Stream<K> ids) {
    runInTransaction(() -> {
      delegate.delete(ids);
      return null;
    });
  }

  @Override
  public void delete(K key) {
    runInTransaction(() -> {
      delegate.delete(key);
      return null;
    });
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification) {
    return readStreamInTransaction(() -> delegate.entries(specification));
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification) {
    return readStreamInTransaction(() -> delegate.keys(specification));
  }

  @Override
  public Stream<V> values(Specification<K, V> specification) {
    return readStreamInTransaction(() -> delegate.values(specification));
  }

  @Override
  public boolean exists(K id) {
    return runInTransaction(() -> delegate.exists(id));
  }

  @Override
  public Optional<V> get(K id) {
    return runInTransaction(() -> delegate.get(id));
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

  private <E> Stream<E> readStreamInTransaction(Supplier<Stream<E>> supplier) {
    Stream<E> stream;
    log.trace("Opening stream read transaction");
    TransactionStatus tx = manager.getTransaction(definition);
    try {
      stream = supplier.get();
    } catch (RuntimeException | Error e) {
      log.trace("Stream initialization failed, rolling back transaction");
      manager.rollback(tx);
      throw e;
    }
    return stream.onClose(() -> {
      log.trace("Committing stream read transaction");
      manager.commit(tx);
    });
  }

}
