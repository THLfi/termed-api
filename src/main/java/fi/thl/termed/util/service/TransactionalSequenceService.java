package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DelegatingTransactionDefinition;

/**
 * Wraps operations to serializable database transactions to ensure atomicity
 */
public class TransactionalSequenceService<K extends Serializable> implements SequenceService<K> {

  private SequenceService<K> delegate;

  private PlatformTransactionManager manager;
  private TransactionDefinition definition;

  public TransactionalSequenceService(SequenceService<K> delegate, PlatformTransactionManager m) {
    this(delegate, m, new DelegatingTransactionDefinition(new DefaultTransactionDefinition()) {
      public int getIsolationLevel() {
        return TransactionDefinition.ISOLATION_SERIALIZABLE;
      }
    });
  }

  public TransactionalSequenceService(SequenceService<K> delegate,
      PlatformTransactionManager manager,
      TransactionDefinition definition) {
    this.delegate = delegate;
    this.manager = manager;
    this.definition = definition;
  }

  @Override
  public int getAndAdvance(K sequenceId, User user) {
    return runInTransaction(() -> delegate.getAndAdvance(sequenceId, user));
  }

  @Override
  public int getAndAdvance(K sequenceId, int count, User user) {
    return runInTransaction(() -> delegate.getAndAdvance(sequenceId, count, user));
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
