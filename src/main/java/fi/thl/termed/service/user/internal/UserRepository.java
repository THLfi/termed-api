package fi.thl.termed.service.user.internal;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Specification;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class UserRepository extends AbstractRepository<String, User> {

  private Dao<String, User> userDao;
  private Dao<UserGraphRole, Empty> userGraphRoleDao;

  public UserRepository(Dao<String, User> userDao, Dao<UserGraphRole, Empty> userGraphRoleDao) {
    this.userDao = userDao;
    this.userGraphRoleDao = userGraphRoleDao;
  }

  @Override
  public void insert(String username, User user, User auth) {
    userDao.insert(username, user, auth);

    for (GraphRole graphRole : user.getGraphRoles()) {
      userGraphRoleDao.insert(new UserGraphRole(
          username, graphRole.getGraph(), graphRole.getRole()), Empty.INSTANCE, auth);
    }
  }

  @Override
  public void update(String username, User newUser, User oldUser, User auth) {
    userDao.update(username, newUser, auth);

    Set<GraphRole> newRoles = ImmutableSet.copyOf(newUser.getGraphRoles());
    Set<GraphRole> oldRoles = ImmutableSet.copyOf(oldUser.getGraphRoles());

    for (GraphRole removedRole : Sets.difference(oldRoles, newRoles)) {
      userGraphRoleDao.delete(new UserGraphRole(
          username, removedRole.getGraph(), removedRole.getRole()), auth);
    }
    for (GraphRole addedRole : Sets.difference(newRoles, oldRoles)) {
      userGraphRoleDao.insert(new UserGraphRole(
          username, addedRole.getGraph(), addedRole.getRole()), Empty.INSTANCE, auth);
    }
  }

  @Override
  public void delete(String username, User user, User auth) {
    for (GraphRole graphRole : user.getGraphRoles()) {
      userGraphRoleDao.delete(new UserGraphRole(
          username, graphRole.getGraph(), graphRole.getRole()), auth);
    }

    userDao.delete(username, auth);
  }

  @Override
  public boolean exists(String username, User auth) {
    return userDao.exists(username, auth);
  }

  @Override
  public Stream<User> get(Specification<String, User> specification, User auth) {
    return userDao.getValues(specification, auth).stream()
        .map(user -> populateValue(user, auth));
  }

  @Override
  public Stream<String> getKeys(Specification<String, User> specification, User user) {
    return userDao.getKeys(specification, user).stream();
  }

  @Override
  public Optional<User> get(String username, User auth) {
    return userDao.get(username, auth).map(user -> populateValue(user, auth));
  }

  private User populateValue(User user, User auth) {
    user = new User(user);

    user.setGraphRoles(Lists.transform(
        userGraphRoleDao.getKeys(new UserGraphRolesByUsername(user.getUsername()), auth),
        new ToGraphRole(user.getUsername())));

    return user;
  }

  /**
   * Transforms user graph role tuple to graph role. Checks that username in the tuple matches the
   * expected username.
   */
  private class ToGraphRole implements Function<UserGraphRole, GraphRole> {

    private String expectedUsername;

    ToGraphRole(String expectedUsername) {
      this.expectedUsername = expectedUsername;
    }

    @Override
    public GraphRole apply(UserGraphRole id) {
      Preconditions.checkArgument(Objects.equals(expectedUsername, id.getUsername()));
      return new GraphRole(id.getGraph(), id.getRole());
    }
  }

}
