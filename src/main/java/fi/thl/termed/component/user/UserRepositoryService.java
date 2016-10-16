package fi.thl.termed.component.user;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepositoryService;
import fi.thl.termed.util.specification.SpecificationQuery;

class UserRepositoryService extends AbstractRepositoryService<String, User> {

  private Dao<String, User> userDao;
  private Dao<UserSchemeRoleId, Empty> userSchemeRoleDao;

  public UserRepositoryService(Dao<String, User> userDao,
                               Dao<UserSchemeRoleId, Empty> userSchemeRoleDao) {
    this.userDao = userDao;
    this.userSchemeRoleDao = userSchemeRoleDao;
  }

  @Override
  protected String extractKey(User user) {
    return user.getUsername();
  }

  @Override
  protected void insert(String username, User user, User auth) {
    userDao.insert(username, user, auth);

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      userSchemeRoleDao.insert(new UserSchemeRoleId(
          username, schemeRole.getSchemeId(), schemeRole.getRole()), Empty.INSTANCE, auth);
    }
  }

  @Override
  protected void update(String username, User newUser, User oldUser, User auth) {
    userDao.update(username, newUser, auth);

    Set<SchemeRole> newRoles = ImmutableSet.copyOf(newUser.getSchemeRoles());
    Set<SchemeRole> oldRoles = ImmutableSet.copyOf(oldUser.getSchemeRoles());

    for (SchemeRole removedRole : Sets.difference(oldRoles, newRoles)) {
      userSchemeRoleDao.delete(new UserSchemeRoleId(
          username, removedRole.getSchemeId(), removedRole.getRole()), auth);
    }
    for (SchemeRole addedRole : Sets.difference(newRoles, oldRoles)) {
      userSchemeRoleDao.insert(new UserSchemeRoleId(
          username, addedRole.getSchemeId(), addedRole.getRole()), Empty.INSTANCE, auth);
    }
  }

  @Override
  protected void delete(String username, User user, User auth) {
    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      userSchemeRoleDao.delete(new UserSchemeRoleId(
          username, schemeRole.getSchemeId(), schemeRole.getRole()), auth);
    }

    userDao.delete(username, auth);
  }

  @Override
  public boolean exists(String username, User auth) {
    return userDao.exists(username, auth);
  }

  @Override
  public List<User> get(SpecificationQuery<String, User> specification, User auth) {
    return userDao.getValues(specification.getSpecification(), auth).stream()
        .map(user -> populateValue(user, auth))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<User> get(String username, User auth) {
    return userDao.get(username, auth).map(user -> populateValue(user, auth));
  }

  private User populateValue(User user, User auth) {
    user = new User(user);

    user.setSchemeRoles(Lists.transform(
        userSchemeRoleDao.getKeys(new UserSchemeRolesByUsername(user.getUsername()), auth),
        new ToSchemeRole(user.getUsername())));

    return user;
  }

  /**
   * Transforms user scheme role tuple to scheme role. Checks that username in the tuple matches the
   * expected username.
   */
  private class ToSchemeRole implements Function<UserSchemeRoleId, SchemeRole> {

    private String expectedUsername;

    public ToSchemeRole(String expectedUsername) {
      this.expectedUsername = expectedUsername;
    }

    @Override
    public SchemeRole apply(UserSchemeRoleId id) {
      Preconditions.checkArgument(Objects.equals(expectedUsername, id.getUsername()));
      return new SchemeRole(id.getSchemeId(), id.getRole());
    }
  }

}
