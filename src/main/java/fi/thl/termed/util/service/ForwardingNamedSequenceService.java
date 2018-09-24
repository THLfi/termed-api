package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;

public class ForwardingNamedSequenceService<K extends Serializable> implements
    NamedSequenceService<K> {

  private NamedSequenceService<K> delegate;

  public ForwardingNamedSequenceService(NamedSequenceService<K> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Long get(K sequenceId, User user) {
    return delegate.get(sequenceId, user);
  }

  @Override
  public Long getAndAdvance(K sequenceId, User user) {
    return delegate.getAndAdvance(sequenceId, user);
  }

  @Override
  public Long getAndAdvance(K sequenceId, Long count, User user) {
    return delegate.getAndAdvance(sequenceId, count, user);
  }

  @Override
  public void set(K sequenceId, Long value, User user) {
    delegate.set(sequenceId, value, user);
  }

  @Override
  public void close() {
    delegate.close();
  }

}
