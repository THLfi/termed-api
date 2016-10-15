package fi.thl.termed.util.dao;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.permission.AppRolePermissionEvaluator;
import fi.thl.termed.util.permission.PermitAllPermissionEvaluator;
import fi.thl.termed.util.specification.Specification;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;

public class SecureDao<K extends Serializable, V> extends AbstractDao<K, V> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private SystemDao<K, V> systemDao;
  private PermissionEvaluator<K> keyEvaluator;
  private PermissionEvaluator<V> valEvaluator;
  private PermissionEvaluator<Specification<K, V>> specificationEvaluator;
  private boolean silent;

  public SecureDao(SystemDao<K, V> systemDao, Multimap<AppRole, Permission> appRolePermissions) {
    this(systemDao,
         new AppRolePermissionEvaluator<K>(appRolePermissions),
         new AppRolePermissionEvaluator<V>(appRolePermissions),
         new AppRolePermissionEvaluator<Specification<K, V>>(appRolePermissions));
  }

  public SecureDao(SystemDao<K, V> systemDao,
                   PermissionEvaluator<K> keyEvaluator,
                   PermissionEvaluator<V> valEvaluator,
                   PermissionEvaluator<Specification<K, V>> specificationEvaluator) {
    this(systemDao, keyEvaluator, valEvaluator, specificationEvaluator, false);
  }

  public SecureDao(SystemDao<K, V> systemDao, PermissionEvaluator<K> keyEvaluator, boolean silent) {
    this(systemDao, keyEvaluator,
         new PermitAllPermissionEvaluator<V>(),
         new PermitAllPermissionEvaluator<Specification<K, V>>(),
         silent);
  }

  public SecureDao(SystemDao<K, V> systemDao,
                   PermissionEvaluator<K> keyEvaluator,
                   PermissionEvaluator<V> valEvaluator,
                   PermissionEvaluator<Specification<K, V>> specificationEvaluator,
                   boolean silent) {
    this.systemDao = systemDao;
    this.keyEvaluator = keyEvaluator;
    this.valEvaluator = valEvaluator;
    this.specificationEvaluator = specificationEvaluator;
    this.silent = silent;
  }

  @Override
  public void insert(K key, V val, User user) {
    if (keyEvaluator.hasPermission(user, key, INSERT) &&
        valEvaluator.hasPermission(user, val, INSERT)) {
      systemDao.insert(key, val);
    } else {
      reportFailedPreAuthorization(user, key, INSERT);
    }
  }

  @Override
  public void update(K key, V val, User user) {
    if (keyEvaluator.hasPermission(user, key, UPDATE) &&
        valEvaluator.hasPermission(user, val, UPDATE)) {
      systemDao.update(key, val);
    } else {
      reportFailedPreAuthorization(user, key, UPDATE);
    }
  }

  @Override
  public void delete(K key, User user) {
    if (keyEvaluator.hasPermission(user, key, DELETE)) {
      systemDao.delete(key);
    } else {
      reportFailedPreAuthorization(user, key, DELETE);
    }
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification, User user) {
    if (specificationEvaluator.hasPermission(user, specification, READ)) {
      Map<K, V> filtered = Maps.newLinkedHashMap();

      for (Map.Entry<K, V> entry : systemDao.getMap(specification).entrySet()) {
        K key = entry.getKey();
        V val = entry.getValue();

        if (keyEvaluator.hasPermission(user, key, READ) &&
            valEvaluator.hasPermission(user, val, READ)) {
          filtered.put(key, val);
        } else {
          reportFailedPostAuthorization(user, key, READ);
        }
      }

      return filtered;
    }

    reportFailedPreAuthorization(user, specification, READ);
    return Collections.emptyMap();
  }

  @Override
  public Optional<V> get(K key, User user) {
    if (keyEvaluator.hasPermission(user, key, READ)) {
      Optional<V> value = systemDao.get(key);

      if (value.isPresent()) {
        if (valEvaluator.hasPermission(user, value.get(), READ)) {
          return value;
        } else {
          reportFailedPostAuthorization(user, value, READ);
        }
      }
    } else {
      reportFailedPreAuthorization(user, key, READ);
    }

    return Optional.absent();
  }

  @Override
  public boolean exists(K key, User user) {
    if (keyEvaluator.hasPermission(user, key, READ)) {
      return systemDao.exists(key);
    }
    reportFailedPreAuthorization(user, key, READ);
    return false;
  }

  private void reportFailedPreAuthorization(User user, Object item, Permission permission) {
    log.warn("Pre-authorization error: Access denied for {} {} {}",
             user.getUsername(), permission, item);
    if (!silent) {
      throw new AccessDeniedException("Access is denied");
    }
  }

  private void reportFailedPostAuthorization(User user, Object item, Permission permission) {
    log.warn("Post-authorization error: Access is denied for {} {} {}",
             user.getUsername(), permission, item);
    if (!silent) {
      throw new AccessDeniedException("Access is denied");
    }
  }

}
