package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WriteErrorHandlingService<K extends Serializable, V>
    extends ForwardingService<K, V> {

  private Consumer<Throwable> errorHandler;
  private Runnable finalHandler;

  public WriteErrorHandlingService(Service<K, V> delegate,
      Consumer<Throwable> errorHandler, Runnable finalHandler) {
    super(delegate);
    this.errorHandler = errorHandler;
    this.finalHandler = finalHandler;
  }

  @Override
  public void save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    processErrors(() -> {
      super.save(values, mode, opts, user);
      return null;
    });
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return processErrors(() -> super.save(value, mode, opts, user));
  }

  @Override
  public void delete(Stream<K> ids, WriteOptions opts, User user) {
    processErrors(() -> {
      super.delete(ids, opts, user);
      return null;
    });
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    processErrors(() -> {
      super.delete(id, opts, user);
      return null;
    });
  }

  @Override
  public void saveAndDelete(Stream<V> saves, Stream<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    processErrors(() -> {
      super.saveAndDelete(saves, deletes, mode, opts, user);
      return null;
    });
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
