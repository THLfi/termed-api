package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WriteErrorHandlingService2<K extends Serializable, V>
    extends ForwardingService2<K, V> {

  private Consumer<Throwable> errorHandler;
  private Runnable finalHandler;

  public WriteErrorHandlingService2(Service2<K, V> delegate,
      Consumer<Throwable> errorHandler, Runnable finalHandler) {
    super(delegate);
    this.errorHandler = errorHandler;
    this.finalHandler = finalHandler;
  }

  @Override
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    return processErrors(() -> super.save(values, mode, opts, user));
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
