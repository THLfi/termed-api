package fi.thl.termed.service.node.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Specification;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For filtering node service read operations. Useful to put in front of an index.
 */
public class ReadAuthorizedNodeService implements Service<NodeId, Node> {

  private Service<NodeId, Node> delegate;
  private PermissionEvaluator<NodeId> nodeEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  public ReadAuthorizedNodeService(
      Service<NodeId, Node> delegate,
      PermissionEvaluator<TypeId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator) {
    this.delegate = delegate;
    this.nodeEvaluator = (u, r, p) -> classEvaluator.hasPermission(u, new TypeId(r), p);
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
  }

  @Override
  public List<NodeId> save(List<Node> values, Map<String, Object> args, User user) {
    return delegate.save(values, args, user);
  }

  @Override
  public NodeId save(Node value, Map<String, Object> args, User user) {
    return delegate.save(value, args, user);
  }

  @Override
  public void delete(List<NodeId> ids, Map<String, Object> args, User user) {
    delegate.delete(ids, args, user);
  }

  @Override
  public void delete(NodeId id, Map<String, Object> args, User user) {
    delegate.delete(id, args, user);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves,
      Map<String, Object> args, User user) {
    return delegate.deleteAndSave(deletes, saves, args, user);
  }

  @Override
  public Stream<Node> get(Specification<NodeId, Node> spec, Map<String, Object> args, User user) {
    return filterValues(delegate.get(spec, args, user), user);
  }

  @Override
  public Stream<NodeId> getKeys(Specification<NodeId, Node> spec, Map<String, Object> args,
      User user) {
    return filterKeys(delegate.getKeys(spec, args, user), user);
  }

  @Override
  public long count(Specification<NodeId, Node> specification, Map<String, Object> args,
      User user) {
    return delegate.count(specification, args, user);
  }

  @Override
  public Stream<Node> get(List<NodeId> ids, Map<String, Object> args, User user) {
    return filterValues(delegate.get(filterKeys(ids, user), args, user), user);
  }

  @Override
  public Optional<Node> get(NodeId id, Map<String, Object> args, User user) {
    if (!nodeEvaluator.hasPermission(user, id, Permission.READ)) {
      return Optional.empty();
    }

    return delegate.get(id, args, user)
        .filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private List<NodeId> filterKeys(List<NodeId> keys, User user) {
    return filterKeys(keys.stream(), user).collect(Collectors.toList());
  }

  private Stream<NodeId> filterKeys(Stream<NodeId> keys, User user) {
    return keys.filter(id -> nodeEvaluator.hasPermission(user, id, Permission.READ));
  }

  private Stream<Node> filterValues(Stream<Node> values, User user) {
    return values.filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private class AttributePermissionFilter implements Function<Node, Node> {

    private User user;
    private Permission permission;

    AttributePermissionFilter(User user, Permission permission) {
      this.user = user;
      this.permission = permission;
    }

    @Override
    public Node apply(Node node) {
      TypeId typeId = new TypeId(node);

      node.setProperties(LinkedHashMultimap.create(Multimaps.filterKeys(
          node.getProperties(), new AcceptPropertyPredicate(typeId))));

      node.setReferences(LinkedHashMultimap.create(Multimaps.filterEntries(
          node.getReferences(), new AcceptReferenceEntryPredicate(typeId))));

      node.setReferrers(LinkedHashMultimap.create(Multimaps.filterEntries(
          node.getReferrers(), new AcceptReferrerEntryPredicate())));

      return node;
    }

    /**
     * Accept a node text attribute value if attribute is permitted
     */
    private class AcceptPropertyPredicate implements Predicate<String> {

      private TypeId typeId;

      AcceptPropertyPredicate(TypeId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(String attributeId) {
        TextAttributeId textAttrId = new TextAttributeId(typeId, attributeId);
        return textAttrEvaluator.hasPermission(user, textAttrId, permission);
      }
    }

    /**
     * Accepts a node reference entry in the reference multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferenceEntryPredicate implements Predicate<Map.Entry<String, NodeId>> {

      private TypeId typeId;

      AcceptReferenceEntryPredicate(TypeId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(Map.Entry<String, NodeId> entry) {
        String attributeId = entry.getKey();
        NodeId reference = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(typeId, attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
            nodeEvaluator.hasPermission(user, reference, permission);
      }
    }

    /**
     * Accepts a node referrer entry in the referrers multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferrerEntryPredicate implements Predicate<Map.Entry<String, NodeId>> {

      @Override
      public boolean apply(Map.Entry<String, NodeId> entry) {
        String attributeId = entry.getKey();
        NodeId referrer = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(referrer.getType(), attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
            nodeEvaluator.hasPermission(user, referrer, permission);
      }
    }

  }


}
