package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteSynchronizedService<K extends Serializable, V> implements Service<K, V> {

  private final Logger log;

  private final Service<K, V> delegate;
  private final Lock readLock;
  private final Lock writeLock;

  private final AtomicInteger writeLockNumber = new AtomicInteger();
  private final AtomicInteger readLockNumber = new AtomicInteger();

  private final int readLockTimeoutInSeconds;
  private final int writeLockTimeoutInSeconds;

  public ReadWriteSynchronizedService(Service<K, V> delegate) {
    this.delegate = delegate;
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    this.readLock = readWriteLock.readLock();
    this.writeLock = readWriteLock.writeLock();
    this.log = LoggerFactory.getLogger(getClass());
    this.readLockTimeoutInSeconds = 10;
    this.writeLockTimeoutInSeconds = 10;
  }

  @Override
  public void save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    writeLock(() -> delegate.save(values, mode, opts, user));
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return writeLock(() -> delegate.save(value, mode, opts, user));
  }

  @Override
  public void delete(Stream<K> keys, WriteOptions opts, User user) {
    writeLock(() -> delegate.delete(keys, opts, user));
  }

  @Override
  public void delete(K key, WriteOptions opts, User user) {
    writeLock(() -> delegate.delete(key, opts, user));
  }

  @Override
  public void saveAndDelete(Stream<V> saves, Stream<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    writeLock(() -> delegate.saveAndDelete(saves, deletes, mode, opts, user));
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return readLockStream(() -> delegate.keys(query, user));
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return readLockStream(() -> delegate.values(query, user));
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    return readLock(() -> delegate.count(spec, user));
  }

  @Override
  public boolean exists(K key, User user) {
    return readLock(() -> delegate.exists(key, user));
  }

  @Override
  public Optional<V> get(K key, User user, Select... selects) {
    return readLock(() -> delegate.get(key, user, selects));
  }

  private void writeLock(Runnable runnable) {
    writeLock(() -> {
      runnable.run();
      return null;
    });
  }

  private <E> E writeLock(Supplier<E> runnable) {
    try {
      if (!writeLock.tryLock(writeLockTimeoutInSeconds, TimeUnit.SECONDS)) {
        throw new RuntimeException("Failed to acquire write lock");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Failed to acquire write lock", e);
    }

    int number = writeLockNumber.getAndIncrement();
    log.trace("Acquire write lock {}", number);

    try {
      return runnable.get();
    } finally {
      log.trace("Release write lock {}", number);
      writeLock.unlock();
    }
  }

  private <E> E readLock(Supplier<E> supplier) {
    try {
      if (!readLock.tryLock(readLockTimeoutInSeconds, TimeUnit.SECONDS)) {
        throw new RuntimeException("Failed to acquire read lock");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Failed to acquire read lock", e);
    }

    int number = readLockNumber.getAndIncrement();
    log.trace("Acquire read lock {}", number);

    try {
      return supplier.get();
    } finally {
      log.trace("Release read lock {}", number);
      readLock.unlock();
    }
  }

  private <E> Stream<E> readLockStream(Supplier<Stream<E>> supplier) {
    try {
      if (!readLock.tryLock(readLockTimeoutInSeconds, TimeUnit.SECONDS)) {
        throw new RuntimeException("Failed to acquire read lock");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Failed to acquire read lock", e);
    }

    int number = readLockNumber.getAndIncrement();
    log.trace("Acquire read lock {}", number);

    try {
      return supplier.get().onClose(() -> {
        log.trace("Release read lock {}", number);
        readLock.unlock();
      });
    } catch (RuntimeException | Error e) {
      log.error("Release read lock " + number, e);
      readLock.unlock();
      throw e;
    }
  }

}
