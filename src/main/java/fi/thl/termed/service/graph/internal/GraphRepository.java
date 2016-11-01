package fi.thl.termed.service.graph.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import fi.thl.termed.domain.transform.GraphRoleModelToDto;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;

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
  public void insert(GraphId id, Graph graph, User user) {
    graphDao.insert(id, graph, user);
    insertRoles(id, graph.getRoles(), user);
    insertPermissions(id, graph.getPermissions(), user);
    insertProperties(id, graph.getProperties(), user);
  }

  private void insertRoles(GraphId graphId, List<String> roles, User user) {
    graphRoleDao.insert(new GraphRoleDtoToModel(graphId).apply(roles), user);
  }

  private void insertPermissions(GraphId graphId, Multimap<String, Permission> permissions,
                                 User user) {
    graphPermissionDao.insert(
        new RolePermissionsDtoToModel<>(graphId, graphId).apply(permissions), user);
  }

  private void insertProperties(GraphId graphId, Multimap<String, LangValue> properties,
                                User user) {
    graphPropertyDao.insert(new PropertyValueDtoToModel<>(graphId).apply(properties), user);
  }

  @Override
  public void update(GraphId id, Graph newGraph, Graph oldGraph, User user) {
    graphDao.update(id, newGraph, user);
    updateRoles(id, newGraph.getRoles(), oldGraph.getRoles(), user);
    updatePermissions(id, newGraph.getPermissions(), oldGraph.getPermissions(), user);
    updateProperties(id, newGraph.getProperties(), oldGraph.getProperties(), user);
  }

  private void updateRoles(GraphId graph, List<String> newRoles, List<String> oldRoles,
                           User user) {
    Map<GraphRole, Empty> newRolesMap = new GraphRoleDtoToModel(graph).apply(newRoles);
    Map<GraphRole, Empty> oldRolesMap = new GraphRoleDtoToModel(graph).apply(oldRoles);

    MapDifference<GraphRole, Empty> diff = Maps.difference(newRolesMap, oldRolesMap);

    graphRoleDao.insert(diff.entriesOnlyOnLeft(), user);
    graphRoleDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updatePermissions(GraphId graphId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<GraphId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(graphId, graphId).apply(newPermissions);
    Map<ObjectRolePermission<GraphId>, GrantedPermission> oldPermissionMap =
        new RolePermissionsDtoToModel<>(graphId, graphId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<GraphId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    graphPermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    graphPermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(GraphId graphId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<GraphId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(graphId).apply(newPropertyMultimap);
    Map<PropertyValueId<GraphId>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(graphId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<GraphId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    graphPropertyDao.insert(diff.entriesOnlyOnLeft(), user);
    graphPropertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    graphPropertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(GraphId id, Graph graph, User user) {
    deleteRoles(id, graph.getRoles(), user);
    deletePermissions(id, graph.getPermissions(), user);
    deleteProperties(id, graph.getProperties(), user);
    graphDao.delete(id, user);
  }

  private void deleteRoles(GraphId id, List<String> roles, User user) {
    graphRoleDao.delete(ImmutableList.copyOf(
        new GraphRoleDtoToModel(id).apply(roles).keySet()), user);
  }

  private void deletePermissions(GraphId id, Multimap<String, Permission> permissions, User user) {
    graphPermissionDao.delete(ImmutableList.copyOf(
        new RolePermissionsDtoToModel<>(id, id).apply(permissions).keySet()), user);
  }

  private void deleteProperties(GraphId id, Multimap<String, LangValue> properties, User user) {
    graphPropertyDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(GraphId id, User user) {
    return graphDao.exists(id, user);
  }

  @Override
  public List<Graph> get(Query<GraphId, Graph> specification, User user) {
    return graphDao.getValues(specification.getSpecification(), user).stream()
        .map(graph -> populateValue(graph, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<GraphId> getKeys(Query<GraphId, Graph> specification, User user) {
    return graphDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Graph> get(GraphId id, User user) {
    return graphDao.get(id, user).map(graph -> populateValue(graph, user));
  }

  private Graph populateValue(Graph graph, User user) {
    graph = new Graph(graph);

    graph.setRoles(new GraphRoleModelToDto().apply(graphRoleDao.getMap(
        new GraphRolesByGraphId(graph.getId()), user)));

    graph.setPermissions(new RolePermissionsModelToDto<GraphId>().apply(
        graphPermissionDao.getMap(new GraphPermissionsByGraphId(new GraphId(graph)), user)));

    graph.setProperties(
        new PropertyValueModelToDto<GraphId>().apply(graphPropertyDao.getMap(
            new GraphPropertiesByGraphId(new GraphId(graph)), user)));

    return graph;
  }

}
