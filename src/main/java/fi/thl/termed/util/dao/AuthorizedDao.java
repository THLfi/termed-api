package fi.thl.termed.util.dao;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.specification.Specification;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;

public class AuthorizedDao<K extends Serializable, V> extends AbstractDao<K, V> {

  public enum ReportLevel {
    LOG, THROW
  }

  private Logger log = LoggerFactory.getLogger(getClass());

  private SystemDao<K, V> delegate;
  private PermissionEvaluator<K> evaluator;

  private ReportLevel reportLevel;

  public AuthorizedDao(SystemDao<K, V> delegate, PermissionEvaluator<K> evaluator) {
    this(delegate, evaluator, ReportLevel.LOG);
  }

  public AuthorizedDao(SystemDao<K, V> delegate, PermissionEvaluator<K> evaluator,
                       ReportLevel reportLevel) {
    this.delegate = delegate;
    this.evaluator = evaluator;
    this.reportLevel = reportLevel;
  }

  @Override
  public void insert(K key, V val, User user) {
    if (evaluator.hasPermission(user, key, INSERT)) {
      delegate.insert(key, val);
    } else {
      reportFailedAuthorization(user, key, INSERT);
    }
  }

  @Override
  public void update(K key, V val, User user) {
    if (evaluator.hasPermission(user, key, UPDATE)) {
      delegate.update(key, val);
    } else {
      reportFailedAuthorization(user, key, UPDATE);
    }
  }

  @Override
  public void delete(K key, User user) {
    if (evaluator.hasPermission(user, key, DELETE)) {
      delegate.delete(key);
    } else {
      reportFailedAuthorization(user, key, DELETE);
    }
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification, User user) {
    Map<K, V> filtered = Maps.newLinkedHashMap();

    for (Map.Entry<K, V> entry : delegate.getMap(specification).entrySet()) {
      K key = entry.getKey();
      V value = entry.getValue();

      if (evaluator.hasPermission(user, key, READ)) {
        filtered.put(key, value);
      } else {
        reportFailedAuthorization(user, key, READ);
      }
    }

    return filtered;
  }

  @Override
  public Optional<V> get(K key, User user) {
    if (evaluator.hasPermission(user, key, READ)) {
      return delegate.get(key);
    }
    reportFailedAuthorization(user, key, READ);
    return Optional.empty();
  }

  @Override
  public boolean exists(K key, User user) {
    if (evaluator.hasPermission(user, key, READ)) {
      return delegate.exists(key);
    }
    reportFailedAuthorization(user, key, READ);
    return false;
  }

  private void reportFailedAuthorization(User user, K key, Permission permission) {
    if (reportLevel == ReportLevel.LOG || reportLevel == ReportLevel.THROW) {
      log.warn("Access denied: {} has no {} permission for {}",
               user.getUsername(), permission, key);
    }
    if (reportLevel == ReportLevel.THROW) {
      throw new AccessDeniedException("Access is denied");
    }
  }

}
