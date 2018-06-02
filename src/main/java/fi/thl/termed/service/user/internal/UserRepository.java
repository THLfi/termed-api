package fi.thl.termed.service.user.internal;

import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.collect.StreamUtils.toSetAndClose;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.dao.Dao2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class UserRepository extends AbstractRepository2<String, User> {

  private Dao2<String, User> userDao;
  private Dao2<UserGraphRole, Empty> userGraphRoleDao;

  public UserRepository(Dao2<String, User> userDao, Dao2<UserGraphRole, Empty> userGraphRoleDao) {
    this.userDao = userDao;
    this.userGraphRoleDao = userGraphRoleDao;
  }

  @Override
  public void insert(String username, User user, WriteOptions opts, User auth) {
    userDao.insert(username, user, auth);

    for (GraphRole graphRole : user.getGraphRoles()) {
      userGraphRoleDao.insert(new UserGraphRole(
          username, graphRole.getGraph(), graphRole.getRole()), Empty.INSTANCE, auth);
    }
  }

  @Override
  public void update(String username, User newUser, WriteOptions opts, User auth) {
    userDao.update(username, newUser, auth);

    Set<GraphRole> newRoles = ImmutableSet.copyOf(newUser.getGraphRoles());
    Set<GraphRole> oldRoles = toSetAndClose(userGraphRoleDao.getKeys(
        new UserGraphRolesByUsername(username), auth)
        .filter(userGraphRole -> userGraphRole.getUsername().equals(username))
        .map(userGraphRole -> new GraphRole(userGraphRole.getGraph(), userGraphRole.getRole())));

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
  public void delete(String username, WriteOptions opts, User auth) {
    userGraphRoleDao.delete(userGraphRoleDao.getKeys(
        new UserGraphRolesByUsername(username), auth), auth);
    userDao.delete(username, auth);
  }

  @Override
  public boolean exists(String username, User auth) {
    return userDao.exists(username, auth);
  }

  @Override
  public Stream<User> values(Query<String, User> query, User auth) {
    return userDao.getValues(query.getWhere(), auth).map(user -> populateValue(user, auth));
  }

  @Override
  public Stream<String> keys(Query<String, User> query, User user) {
    return userDao.getKeys(query.getWhere(), user);
  }

  @Override
  public Optional<User> get(String username, User auth, Select... selects) {
    return userDao.get(username, auth).map(user -> populateValue(user, auth));
  }

  private User populateValue(User user, User auth) {
    List<GraphRole> graphRoles = toListAndClose(
        userGraphRoleDao.getKeys(new UserGraphRolesByUsername(user.getUsername()), auth)
            .filter(value -> Objects.equals(user.getUsername(), value.getUsername()))
            .map(value -> new GraphRole(value.getGraph(), value.getRole())));

    return new User(user.getUsername(), user.getPassword(), user.getAppRole(), graphRoles);
  }

}
