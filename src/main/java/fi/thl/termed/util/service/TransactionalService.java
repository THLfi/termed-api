package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionalService<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

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
  public List<K> save(List<V> values, User user, Arg... args) {
    return runInTransaction(() -> delegate.save(values, user, args));
  }

  @Override
  public K save(V value, User user, Arg... args) {
    return runInTransaction(() -> delegate.save(value, user, args));
  }

  @Override
  public void delete(List<K> ids, User user, Arg... args) {
    runInTransaction(() -> {
      delegate.delete(ids, user, args);
      return null;
    });
  }

  @Override
  public void delete(K id, User user, Arg... args) {
    runInTransaction(() -> {
      delegate.delete(id, user, args);
      return null;
    });
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, User user, Arg... args) {
    return runInTransaction(() -> delegate.deleteAndSave(deletes, saves, user, args));
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, User user, Arg... args) {
    return runInTransaction(() -> delegate.get(specification, user, args));
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, User user, Arg... args) {
    return runInTransaction(() -> delegate.getKeys(specification, user, args));
  }

  @Override
  public long count(Specification<K, V> specification, User user, Arg... args) {
    return runInTransaction(() -> delegate.count(specification, user, args));
  }

  @Override
  public boolean exists(K id, User user, Arg... args) {
    return runInTransaction(() -> delegate.exists(id, user, args));
  }

  @Override
  public Stream<V> get(List<K> ids, User user, Arg... args) {
    return runInTransaction(() -> delegate.get(ids, user, args));
  }

  @Override
  public Optional<V> get(K id, User user, Arg... args) {
    return runInTransaction(() -> delegate.get(id, user, args));
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
