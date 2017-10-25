package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Adds simple "one at a time" synchronization to sequence service.
 */
public class SynchronizedNamedSequenceService<K extends Serializable> implements
    NamedSequenceService<K> {

  private final Lock lock;
  private NamedSequenceService<K> delegate;

  public SynchronizedNamedSequenceService(NamedSequenceService<K> delegate) {
    this.delegate = delegate;
    this.lock = new ReentrantLock();
  }

  @Override
  public Long getAndAdvance(K sequenceId, User user) {
    return runSynchronized(() -> delegate.getAndAdvance(sequenceId, user));
  }

  @Override
  public Long getAndAdvance(K sequenceId, Long count, User user) {
    return runSynchronized(() -> delegate.getAndAdvance(sequenceId, count, user));
  }

  private <E> E runSynchronized(Supplier<E> supplier) {
    lock.lock();
    try {
      return supplier.get();
    } finally {
      lock.unlock();
    }
  }

}
