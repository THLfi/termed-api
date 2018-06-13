package fi.thl.termed.util.spring.transaction;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public final class TransactionUtils {

  private TransactionUtils() {
  }

  public static <E> E runInTransaction(PlatformTransactionManager manager, Supplier<E> operation) {
    return runInTransaction(manager, new DefaultTransactionDefinition(), operation, (e) -> {
    });
  }

  public static <E> E runInTransaction(PlatformTransactionManager manager, Supplier<E> operation,
      Consumer<Throwable> onError) {
    return runInTransaction(manager, new DefaultTransactionDefinition(), operation, onError);
  }

  public static <E> E runInTransaction(PlatformTransactionManager manager,
      TransactionDefinition definition, Supplier<E> operation, Consumer<Throwable> onError) {

    TransactionStatus tx = manager.getTransaction(definition);
    E results;
    try {
      results = operation.get();
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      onError.accept(e);
      throw e;
    }
    manager.commit(tx);
    return results;
  }

}
