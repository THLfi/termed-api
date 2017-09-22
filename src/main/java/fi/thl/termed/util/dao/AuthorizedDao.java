package fi.thl.termed.util.dao;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizedDao<K extends Serializable, V> extends AbstractDao<K, V> {

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
  public void insert(Map<K, V> map, User user) {
    Map<K, V> filtered = new LinkedHashMap<>();

    map.forEach((key, value) -> {
      if (evaluator.hasPermission(user, key, INSERT)) {
        filtered.put(key, value);
      } else {
        reportFailedAuthorization(user, key, INSERT);
      }
    });

    delegate.insert(filtered);
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
  public void update(Map<K, V> map, User user) {
    Map<K, V> filtered = new LinkedHashMap<>();

    map.forEach((key, value) -> {
      if (evaluator.hasPermission(user, key, UPDATE)) {
        filtered.put(key, value);
      } else {
        reportFailedAuthorization(user, key, UPDATE);
      }
    });

    delegate.update(filtered);
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
  public void delete(List<K> keys, User user) {
    List<K> filtered = new ArrayList<>();

    keys.forEach(key -> {
      if (evaluator.hasPermission(user, key, DELETE)) {
        filtered.add(key);
      } else {
        reportFailedAuthorization(user, key, DELETE);
      }
    });

    delegate.delete(filtered);
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
    Map<K, V> filtered = new LinkedHashMap<>();

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
    if (reportLevel == ReportLevel.SILENT) {
      log.trace(formatErrorMessage(user, key, permission));
    } else if (reportLevel == ReportLevel.LOG) {
      log.warn(formatErrorMessage(user, key, permission));
    } else if (reportLevel == ReportLevel.THROW) {
      log.error(formatErrorMessage(user, key, permission));
      throw new AccessDeniedException("Access is denied");
    }
  }

  private String formatErrorMessage(User user, K key, Permission permission) {
    return String.format("Access denied: %s has no %s permission for %s",
        user.getUsername(), permission, key);
  }

  public enum ReportLevel {
    SILENT, LOG, THROW
  }

}
