package fi.thl.termed.service.common;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;

public class TransactionalService<K extends Serializable, V> extends ForwardingService<K, V> {

  private static AtomicInteger serialNumber = new AtomicInteger(0);
  private Logger log = LoggerFactory.getLogger(getClass());

  private PlatformTransactionManager transactionManager;
  private TransactionDefinition transactionDefinition;

  public TransactionalService(Service<K, V> delegate,
                              PlatformTransactionManager transactionManager) {
    super(delegate);
    this.transactionManager = transactionManager;
    this.transactionDefinition = new DefaultTransactionDefinition();
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    log.trace("begin transaction {}", serialNumber.incrementAndGet());
    List<V> values = super.get(specification, currentUser);
    transactionManager.commit(tx);
    log.trace("commit transaction {}", serialNumber.get());
    return values;
  }

  @Override
  public Optional<V> get(K id, User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    log.trace("begin transaction {}", serialNumber.incrementAndGet());
    Optional<V> value = super.get(id, currentUser);
    transactionManager.commit(tx);
    log.trace("commit transaction {}", serialNumber.get());
    return value;
  }

  @Override
  public List<K> save(List<V> values, User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    log.trace("begin transaction {}", serialNumber.incrementAndGet());
    List<K> keys = super.save(values, currentUser);
    transactionManager.commit(tx);
    log.trace("commit transaction {}", serialNumber.get());
    return keys;
  }

  @Override
  public K save(V value, User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    log.trace("begin transaction {}", serialNumber.incrementAndGet());
    K key = super.save(value, currentUser);
    transactionManager.commit(tx);
    log.trace("commit transaction {}", serialNumber.get());
    return key;
  }

  @Override
  public void delete(K id, User currentUser) {
    TransactionStatus tx = transactionManager.getTransaction(transactionDefinition);
    log.trace("begin transaction {}", serialNumber.incrementAndGet());
    super.delete(id, currentUser);
    transactionManager.commit(tx);
    log.trace("commit transaction {}", serialNumber.get());
  }

}
