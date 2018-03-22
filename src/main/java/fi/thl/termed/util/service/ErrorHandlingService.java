package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ErrorHandlingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;
  private Consumer<Throwable> errorHandler;
  private Runnable finalHandler;

  public ErrorHandlingService(Service<K, V> delegate,
      Consumer<Throwable> errorHandler, Runnable finalHandler) {
    this.delegate = delegate;
    this.errorHandler = errorHandler;
    this.finalHandler = finalHandler;
  }

  @Override
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    return processErrors(() -> delegate.save(values, mode, opts, user));
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return processErrors(() -> delegate.save(value, mode, opts, user));
  }

  @Override
  public void delete(List<K> ids, WriteOptions opts, User user) {
    processErrors(() -> {
      delegate.delete(ids, opts, user);
      return null;
    });
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    processErrors(() -> {
      delegate.delete(id, opts, user);
      return null;
    });
  }

  @Override
  public List<K> saveAndDelete(List<V> saves, List<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    return processErrors(() -> delegate.saveAndDelete(saves, deletes, mode, opts, user));
  }

  @Override
  public List<V> getValues(User user) {
    return processErrors(() -> delegate.getValues(user));
  }

  @Override
  public List<V> getValues(Specification<K, V> spec, User user) {
    return processErrors(() -> delegate.getValues(spec, user));
  }

  @Override
  public List<V> getValues(Query<K, V> query, User user) {
    return processErrors(() -> delegate.getValues(query, user));
  }

  @Override
  public Stream<V> getValueStream(User user) {
    return processErrors(() -> delegate.getValueStream(user));
  }

  @Override
  public Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return processErrors(() -> delegate.getValueStream(spec, user));
  }

  @Override
  public Stream<V> getValueStream(Query<K, V> query, User user) {
    return processErrors(() -> delegate.getValueStream(query, user));
  }

  @Override
  public List<K> getKeys(User user) {
    return processErrors(() -> delegate.getKeys(user));
  }

  @Override
  public List<K> getKeys(Specification<K, V> spec, User user) {
    return processErrors(() -> delegate.getKeys(spec, user));
  }

  @Override
  public List<K> getKeys(Query<K, V> query, User user) {
    return processErrors(() -> delegate.getKeys(query, user));
  }

  @Override
  public Stream<K> getKeyStream(User user) {
    return processErrors(() -> delegate.getKeyStream(user));
  }

  @Override
  public Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return processErrors(() -> delegate.getKeyStream(spec, user));
  }

  @Override
  public Stream<K> getKeyStream(Query<K, V> query, User user) {
    return processErrors(() -> delegate.getKeyStream(query, user));
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    return processErrors(() -> delegate.count(spec, user));
  }

  @Override
  public boolean exists(K id, User user) {
    return processErrors(() -> delegate.exists(id, user));
  }

  @Override
  public Optional<V> get(K id, User user, Select... selects) {
    return processErrors(() -> delegate.get(id, user, selects));
  }

  private <E> E processErrors(Supplier<E> supplier) {
    E results;

    try {
      results = supplier.get();
    } catch (RuntimeException | Error e) {
      errorHandler.accept(e);
      throw e;
    } finally {
      finalHandler.run();
    }

    return results;
  }

}
