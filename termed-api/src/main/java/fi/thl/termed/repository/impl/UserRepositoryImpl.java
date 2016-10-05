package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.UserSchemeRolesByUsername;
import fi.thl.termed.util.FunctionUtils;

public class UserRepositoryImpl extends AbstractRepository<String, User> {

  private Dao<String, User> userDao;
  private Dao<UserSchemeRoleId, Empty> userSchemeRoleDao;

  public UserRepositoryImpl(Dao<String, User> userDao,
                            Dao<UserSchemeRoleId, Empty> userSchemeRoleDao) {
    this.userDao = userDao;
    this.userSchemeRoleDao = userSchemeRoleDao;
  }

  @Override
  public void save(User user, User auth) {
    save(user.getUsername(), user, auth);
  }

  @Override
  protected void insert(String username, User user, User auth) {
    userDao.insert(username, user, auth);

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      userSchemeRoleDao.insert(new UserSchemeRoleId(
          username, schemeRole.getSchemeId(), schemeRole.getRole()), null, auth);
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
          username, addedRole.getSchemeId(), addedRole.getRole()), null, auth);
    }
  }

  @Override
  public void delete(String username, User auth) {
    delete(username, get(username, auth).get(), auth);
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
    return Lists.transform(userDao.getValues(specification.getSpecification(), auth),
                           FunctionUtils.pipe(new CreateCopy(), new AddSchemeRoles(auth)));
  }

  @Override
  public Optional<User> get(String username, User auth) {
    Optional<User> o = userDao.get(username, auth);
    return o.isPresent() ? Optional.of(new AddSchemeRoles(auth).apply(new User(o.get())))
                         : Optional.<User>absent();
  }

  private class CreateCopy implements Function<User, User> {

    public User apply(User user) {
      return new User(user);
    }

  }

  /**
   * Adds scheme roles to user object.
   */
  private class AddSchemeRoles implements Function<User, User> {

    private User auth;

    public AddSchemeRoles(User auth) {
      this.auth = auth;
    }

    @Override
    public User apply(User user) {
      user.setSchemeRoles(Lists.transform(
          userSchemeRoleDao.getKeys(new UserSchemeRolesByUsername(user.getUsername()), auth),
          new ToSchemeRole(user.getUsername())));
      return user;
    }
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
      Preconditions.checkArgument(Objects.equal(expectedUsername, id.getUsername()));
      return new SchemeRole(id.getSchemeId(), id.getRole());
    }
  }

}
