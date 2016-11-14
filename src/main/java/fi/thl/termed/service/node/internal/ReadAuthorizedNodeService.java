package fi.thl.termed.service.node.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimaps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;

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
  public List<NodeId> save(List<Node> values, User user) {
    return delegate.save(values, user);
  }

  @Override
  public NodeId save(Node value, User user) {
    return delegate.save(value, user);
  }

  @Override
  public void delete(List<NodeId> ids, User user) {
    delegate.delete(ids, user);
  }

  @Override
  public void delete(NodeId id, User user) {
    delegate.delete(id, user);
  }

  @Override
  public Results<Node> get(Query<NodeId, Node> query, User user) {
    Results<Node> results = delegate.get(query, user);
    return new Results<>(filterValues(results.getValues(), user), results.getSize());
  }

  @Override
  public Results<NodeId> getKeys(Query<NodeId, Node> query, User user) {
    Results<NodeId> results = delegate.getKeys(query, user);
    return new Results<>(filterKeys(results.getValues(), user), results.getSize());
  }

  @Override
  public List<Node> get(List<NodeId> ids, User user) {
    return filterValues(delegate.get(filterKeys(ids, user), user), user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user) {
    if (!nodeEvaluator.hasPermission(user, id, Permission.READ)) {
      return Optional.empty();
    }

    return delegate.get(id, user)
        .filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private List<NodeId> filterKeys(List<NodeId> keys, User user) {
    return keys.stream()
        .filter(id -> nodeEvaluator.hasPermission(user, id, Permission.READ))
        .collect(Collectors.toList());
  }

  private List<Node> filterValues(List<Node> values, User user) {
    return values.stream()
        .filter(r -> nodeEvaluator.hasPermission(user, new NodeId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ))
        .collect(Collectors.toList());
  }

  private class AttributePermissionFilter implements Function<Node, Node> {

    private User user;
    private Permission permission;

    public AttributePermissionFilter(User user, Permission permission) {
      this.user = user;
      this.permission = permission;
    }

    @Override
    public Node apply(Node node) {
      TypeId typeId = new TypeId(node);

      node.setProperties(Multimaps.filterKeys(
          node.getProperties(), new AcceptProperty(typeId)));

      node.setReferences(Multimaps.filterEntries(
          node.getReferences(), new AcceptReferenceEntryPredicate(typeId)));

      node.setReferrers(Multimaps.filterEntries(
          node.getReferrers(), new AcceptReferrerEntryPredicate()));

      return node;
    }

    /**
     * Accept a node text attribute value if attribute is permitted
     */
    private class AcceptProperty implements Predicate<String> {

      private TypeId typeId;

      public AcceptProperty(TypeId typeId) {
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
    private class AcceptReferenceEntryPredicate
        implements Predicate<Map.Entry<String, NodeId>> {

      private TypeId typeId;

      public AcceptReferenceEntryPredicate(TypeId typeId) {
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
