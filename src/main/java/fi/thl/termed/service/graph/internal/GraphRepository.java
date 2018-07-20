package fi.thl.termed.service.graph.internal;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static fi.thl.termed.util.collect.MultimapUtils.toImmutableMultimap;
import static fi.thl.termed.util.collect.Tuple.entriesAsTuples;
import static fi.thl.termed.util.collect.Tuple.tuplesToMap;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.GraphRoleDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class GraphRepository extends AbstractRepository<GraphId, Graph> {

  private Dao<GraphId, Graph> graphDao;
  private Dao<GraphRole, Empty> graphRoleDao;
  private Dao<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionDao;
  private Dao<PropertyValueId<GraphId>, LangValue> graphPropertyDao;

  public GraphRepository(Dao<GraphId, Graph> graphDao,
      Dao<GraphRole, Empty> graphRoleDao,
      Dao<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionDao,
      Dao<PropertyValueId<GraphId>, LangValue> graphPropertyDao) {
    this.graphDao = graphDao;
    this.graphRoleDao = graphRoleDao;
    this.graphPermissionDao = graphPermissionDao;
    this.graphPropertyDao = graphPropertyDao;
  }

  @Override
  public void insert(GraphId id, Graph graph, WriteOptions opts, User user) {
    graphDao.insert(id, graph, user);
    insertRoles(id, graph.getRoles(), user);
    insertPermissions(id, graph.getPermissions(), user);
    insertProperties(id, graph.getProperties(), user);
  }

  private void insertRoles(GraphId graphId, List<String> roles, User user) {
    graphRoleDao.insert(
        new GraphRoleDtoToModel(graphId).apply(roles), user);
  }

  private void insertPermissions(GraphId graphId, Multimap<String, Permission> permissions,
      User user) {
    graphPermissionDao.insert(
        new RolePermissionsDtoToModel<>(graphId, graphId).apply(permissions), user);
  }

  private void insertProperties(GraphId graphId, Multimap<String, LangValue> properties,
      User user) {
    graphPropertyDao.insert(
        new PropertyValueDtoToModel<>(graphId).apply(properties), user);
  }

  @Override
  public void update(GraphId id, Graph graph, WriteOptions opts, User user) {
    graphDao.update(id, graph, user);
    updateRoles(id, graph.getRoles(), user);
    updatePermissions(id, graph.getPermissions(), user);
    updateProperties(id, graph.getProperties(), user);
  }

  private void updateRoles(GraphId graphId, List<String> roles, User user) {
    Map<GraphRole, Empty> newRolesMap = tuplesToMap(
        new GraphRoleDtoToModel(graphId).apply(roles));
    Map<GraphRole, Empty> oldRolesMap = tuplesToMap(graphRoleDao.getEntries(
        new GraphRolesByGraphId(graphId), user));

    MapDifference<GraphRole, Empty> diff = Maps.difference(newRolesMap, oldRolesMap);

    graphRoleDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    graphRoleDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  private void updatePermissions(GraphId graphId, Multimap<String, Permission> permissions,
      User u) {

    Map<ObjectRolePermission<GraphId>, GrantedPermission> newPermissionMap =
        tuplesToMap(new RolePermissionsDtoToModel<>(graphId, graphId).apply(permissions));
    Map<ObjectRolePermission<GraphId>, GrantedPermission> oldPermissionMap =
        tuplesToMap(graphPermissionDao.getEntries(new GraphPermissionsByGraphId(graphId), u));

    MapDifference<ObjectRolePermission<GraphId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    graphPermissionDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), u);
    graphPermissionDao.delete(diff.entriesOnlyOnRight().keySet().stream(), u);
  }

  private void updateProperties(GraphId graphId, Multimap<String, LangValue> propertyMultimap,
      User user) {

    Map<PropertyValueId<GraphId>, LangValue> newProperties =
        tuplesToMap(new PropertyValueDtoToModel<>(graphId).apply(propertyMultimap));
    Map<PropertyValueId<GraphId>, LangValue> oldProperties =
        tuplesToMap(graphPropertyDao.getEntries(new GraphPropertiesByGraphId(graphId), user));

    MapDifference<PropertyValueId<GraphId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    graphPropertyDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    graphPropertyDao.update(entriesAsTuples(leftValues(diff.entriesDiffering())), user);
    graphPropertyDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  @Override
  public void delete(GraphId id, WriteOptions opts, User user) {
    deleteRoles(id, user);
    deletePermissions(id, user);
    deleteProperties(id, user);
    graphDao.delete(id, user);
  }

  private void deleteRoles(GraphId id, User user) {
    graphRoleDao.delete(graphRoleDao.getKeys(
        new GraphRolesByGraphId(id), user), user);
  }

  private void deletePermissions(GraphId id, User user) {
    graphPermissionDao.delete(graphPermissionDao.getKeys(
        new GraphPermissionsByGraphId(id), user), user);
  }

  private void deleteProperties(GraphId id, User user) {
    graphPropertyDao.delete(graphPropertyDao.getKeys(
        new GraphPropertiesByGraphId(id), user), user);
  }

  @Override
  public boolean exists(GraphId id, User user) {
    return graphDao.exists(id, user);
  }

  @Override
  public Stream<Graph> values(Query<GraphId, Graph> query, User user) {
    return graphDao.getValues(query.getWhere(), user).map(graph -> populateValue(graph, user));
  }

  @Override
  public Stream<GraphId> keys(Query<GraphId, Graph> query, User user) {
    return graphDao.getKeys(query.getWhere(), user);
  }

  @Override
  public Optional<Graph> get(GraphId id, User user, Select... selects) {
    return graphDao.get(id, user).map(graph -> populateValue(graph, user));
  }

  private Graph populateValue(Graph graph, User user) {
    GraphId id = graph.identifier();

    try (
        Stream<GraphRole> roleStream =
            graphRoleDao.getKeys(new GraphRolesByGraphId(id), user);
        Stream<ObjectRolePermission<GraphId>> permissionStream = graphPermissionDao
            .getKeys(new GraphPermissionsByGraphId(id), user);
        Stream<Tuple2<PropertyValueId<GraphId>, LangValue>> propertyStream = graphPropertyDao
            .getEntries(new GraphPropertiesByGraphId(id), user)) {

      return Graph.builderFromCopyOf(graph)
          .roles(roleStream.map(GraphRole::getRole).collect(toImmutableList()))
          .permissions(permissionStream.collect(toImmutableMultimap(
              ObjectRolePermission::getRole,
              ObjectRolePermission::getPermission)))
          .properties(propertyStream.collect(toImmutableMultimap(
              e -> e._1.getPropertyId(),
              e -> e._2)))
          .build();
    }
  }

}
