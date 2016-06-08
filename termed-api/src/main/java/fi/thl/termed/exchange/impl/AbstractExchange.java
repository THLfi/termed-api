package fi.thl.termed.exchange.impl;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.service.Service;

public abstract class AbstractExchange<K extends Serializable, V, E>
    extends AbstractExporter<K, V, E> implements Exchange<K, V, E> {

  public AbstractExchange(Service<K, V> service) {
    super(service);
  }

  public void save(E value, Map<String, Object> args, User currentUser) {
    Preconditions.checkArgument(ArgsValidator.validate(args, requiredArgs()));
    doImport(value, args, currentUser);
  }

  protected abstract void doImport(E value, Map<String, Object> args, User currentUser);

}
