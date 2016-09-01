package fi.thl.termed.service.common;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

public class TransactionalService<K extends Serializable, V> extends ForwardingService<K, V> {

  private PlatformTransactionManager transactionManager;
  private TransactionDefinition transactionDefinition;

  public TransactionalService(Service<K, V> delegate,
                              PlatformTransactionManager transactionManager) {
    super(delegate);
    this.transactionManager = transactionManager;
    this.transactionDefinition = new DefaultTransactionDefinition();
  }

  @Override
  public List<V> get(final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    List<V> values = super.get(currentUser);
    transactionManager.commit(tx);
    return values;
  }

  @Override
  public List<V> get(final Specification<K, V> specification, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    List<V> values = super.get(specification, currentUser);
    transactionManager.commit(tx);
    return values;
  }

  @Override
  public List<V> get(final Query query, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    List<V> values = super.get(query, currentUser);
    transactionManager.commit(tx);
    return values;
  }

  @Override
  public V get(final K id, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    V value = super.get(id, currentUser);
    transactionManager.commit(tx);
    return value;
  }

  @Override
  public void save(final List<V> values, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    super.save(values, currentUser);
    transactionManager.commit(tx);
  }

  @Override
  public void save(final V value, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    super.save(value, currentUser);
    transactionManager.commit(tx);
  }

  @Override
  public void delete(final K id, final User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    super.delete(id, currentUser);
    transactionManager.commit(tx);
  }

}
