package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.security.access.AccessDeniedException;

public class WritePreAuthorizingService<K extends Serializable, V>
    extends ForwardingService<K, V> {

  private Predicate<User> savePredicate;
  private Predicate<User> deletePredicate;

  public WritePreAuthorizingService(Service<K, V> delegate,
      Predicate<User> savePredicate,
      Predicate<User> deletePredicate) {
    super(delegate);
    this.savePredicate = savePredicate;
    this.deletePredicate = deletePredicate;
  }

  @Override
  public void save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    super.save(values.filter(value -> {
      if (savePredicate.test(user)) {
        return true;
      } else {
        throw new AccessDeniedException("Access is denied");
      }
    }), mode, opts, user);
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    if (savePredicate.test(user)) {
      return super.save(value, mode, opts, user);
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

  @Override
  public void delete(Stream<K> ids, WriteOptions opts, User user) {
    super.delete(ids.filter(id -> {
      if (deletePredicate.test(user)) {
        return true;
      } else {
        throw new AccessDeniedException("Access is denied");
      }
    }), opts, user);
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    if (deletePredicate.test(user)) {
      super.delete(id, opts, user);
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

}
