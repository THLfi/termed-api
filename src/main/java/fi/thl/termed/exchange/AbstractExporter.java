package fi.thl.termed.exchange;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.SpecificationQuery;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Abstract Exporter that is backed by a Service.
 */
public abstract class AbstractExporter<K extends Serializable, V, E> implements Exporter<K, V, E> {

  protected Service<K, V> service;

  public AbstractExporter(Service<K, V> service) {
    this.service = service;
  }

  @Override
  public E get(SpecificationQuery<K, V> specification, Map<String, Object> args, User currentUser) {
    check(args);
    return doExport(service.get(specification, currentUser), args, currentUser);
  }

  @Override
  public E get(K id, Map<String, Object> args, User currentUser) {
    check(args);

    Optional<V> optional = service.get(id, currentUser);

    return doExport(optional.isPresent() ? Collections.singletonList(optional.get())
                                         : Collections.<V>emptyList(), args, currentUser);
  }

  protected void check(Map<String, Object> args) {
    checkArgument(Objects.equal(Maps.transformValues(args, new Function<Object, Class>() {
      public Class apply(Object argValue) {
        return argValue != null ? argValue.getClass() : null;
      }
    }), requiredArgs()));
  }

  protected abstract Map<String, Class> requiredArgs();

  protected abstract E doExport(List<V> values, Map<String, Object> args, User currentUser);

}
