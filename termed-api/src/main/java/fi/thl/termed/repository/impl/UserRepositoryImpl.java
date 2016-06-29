package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.UserSchemeRolesByUsername;

public class UserRepositoryImpl extends AbstractRepository<String, User> {

  private Dao<String, User> userDao;
  private Dao<UserSchemeRoleId, Void> userSchemeRoleDao;
  private Function<User, User> addSchemeRoles;

  public UserRepositoryImpl(Dao<String, User> userDao,
                            Dao<UserSchemeRoleId, Void> userSchemeRoleDao) {
    this.userDao = userDao;
    this.userSchemeRoleDao = userSchemeRoleDao;
    this.addSchemeRoles = new AddSchemeRoles();
  }

  @Override
  public void save(User user) {
    save(user.getUsername(), user);
  }

  @Override
  protected void insert(String username, User user) {
    userDao.insert(username, user);

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      userSchemeRoleDao.insert(new UserSchemeRoleId(
          username, schemeRole.getSchemeId(), schemeRole.getRole()), null);
    }
  }

  @Override
  protected void update(String username, User newUser, User oldUser) {
    userDao.update(username, newUser);

    Set<SchemeRole> newRoles = ImmutableSet.copyOf(newUser.getSchemeRoles());
    Set<SchemeRole> oldRoles = ImmutableSet.copyOf(oldUser.getSchemeRoles());

    for (SchemeRole removedRole : Sets.difference(oldRoles, newRoles)) {
      userSchemeRoleDao.delete(new UserSchemeRoleId(
          username, removedRole.getSchemeId(), removedRole.getRole()));
    }
    for (SchemeRole addedRole : Sets.difference(newRoles, oldRoles)) {
      userSchemeRoleDao.insert(new UserSchemeRoleId(
          username, addedRole.getSchemeId(), addedRole.getRole()), null);
    }
  }

  @Override
  protected void delete(String username, User user) {
    delete(username);
  }

  @Override
  public void delete(String username) {
    userDao.delete(username);
  }

  @Override
  public boolean exists(String username) {
    return userDao.exists(username);
  }

  @Override
  public List<User> get() {
    return Lists.transform(userDao.getValues(), addSchemeRoles);
  }

  @Override
  public List<User> get(Specification<String, User> specification) {
    return Lists.transform(userDao.getValues(specification), addSchemeRoles);
  }

  @Override
  public User get(String username) {
    return addSchemeRoles.apply(userDao.get(username));
  }

  /**
   * Adds scheme roles to user object.
   */
  private class AddSchemeRoles implements Function<User, User> {

    @Override
    public User apply(User user) {
      user.setSchemeRoles(Lists.transform(
          userSchemeRoleDao.getKeys(new UserSchemeRolesByUsername(user.getUsername())),
          new ToSchemeRole(user.getUsername())));
      return user;
    }
  }

  /**
   * Transforms user scheme role tuple to scheme role. Checks that username in the tuple matches to
   * expected username.
   */
  private class ToSchemeRole implements Function<UserSchemeRoleId, SchemeRole> {

    private String expectedUsername;

    public ToSchemeRole(String expectedUsername) {
      this.expectedUsername = expectedUsername;
    }

    @Override
    public SchemeRole apply(UserSchemeRoleId id) {
      Preconditions.checkArgument(Objects.equal(expectedUsername, id.getUsername()));
      return new SchemeRole(id.getSchemeId(), id.getRole());
    }
  }

}
