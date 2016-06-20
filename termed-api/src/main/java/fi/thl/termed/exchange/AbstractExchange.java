package fi.thl.termed.exchange;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;

/**
 * Abstract Exchange (Exporter and Importer)
 */
public abstract class AbstractExchange<K extends Serializable, V, E>
    extends AbstractExporter<K, V, E> implements Exchange<K, V, E> {

  public AbstractExchange(Service<K, V> service) {
    super(service);
  }

  public void save(E value, Map<String, Object> args, User currentUser) {
    check(args);
    doImport(value, args, currentUser);
  }

  protected abstract void doImport(E value, Map<String, Object> args, User currentUser);

}
