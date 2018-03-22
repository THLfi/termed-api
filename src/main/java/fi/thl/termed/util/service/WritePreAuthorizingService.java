package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiPredicate;
import org.springframework.security.access.AccessDeniedException;

public class WritePreAuthorizingService<K extends Serializable, V> extends ForwardingService<K, V> {

  private Service<K, V> delegate;

  private BiPredicate<V, User> savePredicate;
  private BiPredicate<K, User> deletePredicate;

  public WritePreAuthorizingService(Service<K, V> delegate,
      BiPredicate<V, User> savePredicate,
      BiPredicate<K, User> deletePredicate) {
    super(delegate);
    this.delegate = delegate;
    this.savePredicate = savePredicate;
    this.deletePredicate = deletePredicate;
  }

  @Override
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    if (values.stream().allMatch(v -> savePredicate.test(v, user))) {
      return delegate.save(values, mode, opts, user);
    }
    throw new AccessDeniedException("Access is denied");
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    if (savePredicate.test(value, user)) {
      return delegate.save(value, mode, opts, user);
    }
    throw new AccessDeniedException("Access is denied");
  }

  @Override
  public void delete(List<K> ids, WriteOptions opts, User user) {
    if (ids.stream().allMatch(id -> deletePredicate.test(id, user))) {
      delegate.delete(ids, opts, user);
    }
    throw new AccessDeniedException("Access is denied");
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    if (deletePredicate.test(id, user)) {
      delegate.delete(id, opts, user);
    }
    throw new AccessDeniedException("Access is denied");
  }

  @Override
  public List<K> saveAndDelete(List<V> saves, List<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    if (saves.stream().allMatch(s -> savePredicate.test(s, user)) &&
        deletes.stream().allMatch(d -> deletePredicate.test(d, user))) {
      return delegate.saveAndDelete(saves, deletes, mode, opts, user);
    }
    throw new AccessDeniedException("Access is denied");
  }

}
