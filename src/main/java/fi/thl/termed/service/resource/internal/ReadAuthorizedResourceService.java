package fi.thl.termed.service.resource.internal;

import com.google.common.base.Predicate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.Multimaps.filterEntries;
import static com.google.common.collect.Multimaps.filterKeys;

/**
 * For filtering Resource service read operations. Useful to put in front of an index.
 */
public class ReadAuthorizedResourceService extends ForwardingService<ResourceId, Resource> {

  private PermissionEvaluator<ResourceId> resourceEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  public ReadAuthorizedResourceService(
      Service<ResourceId, Resource> delegate,
      PermissionEvaluator<ClassId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator) {
    super(delegate);
    this.resourceEvaluator = (u, r, p) -> classEvaluator.hasPermission(u, new ClassId(r), p);
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
  }

  @Override
  public List<Resource> get(Query<ResourceId, Resource> specification, User user) {
    return super.get(specification, user)
        .stream()
        .filter(r -> resourceEvaluator.hasPermission(user, new ResourceId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ))
        .collect(Collectors.toList());
  }

  @Override
  public List<ResourceId> getKeys(Query<ResourceId, Resource> specification, User user) {
    return super.getKeys(specification, user)
        .stream()
        .filter(id -> resourceEvaluator.hasPermission(user, id, Permission.READ))
        .collect(Collectors.toList());
  }

  @Override
  public List<Resource> get(List<ResourceId> ids, User user) {
    ids = ids.stream()
        .filter(id -> resourceEvaluator.hasPermission(user, id, Permission.READ))
        .collect(Collectors.toList());

    return super.get(ids, user)
        .stream()
        .filter(r -> resourceEvaluator.hasPermission(user, new ResourceId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Resource> get(ResourceId id, User user) {
    if (!resourceEvaluator.hasPermission(user, id, Permission.READ)) {
      return Optional.empty();
    }

    return super.get(id, user)
        .filter(r -> resourceEvaluator.hasPermission(user, new ResourceId(r), Permission.READ))
        .map(new AttributePermissionFilter(user, Permission.READ));
  }

  private class AttributePermissionFilter implements Function<Resource, Resource> {

    private User user;
    private Permission permission;

    public AttributePermissionFilter(User user, Permission permission) {
      this.user = user;
      this.permission = permission;
    }

    @Override
    public Resource apply(Resource resource) {
      ClassId typeId = new ClassId(resource);

      resource.setProperties(filterKeys(
          resource.getProperties(), new AcceptProperty(typeId)));

      resource.setReferences(filterEntries(
          resource.getReferences(), new AcceptReferenceEntryPredicate(typeId)));

      resource.setReferrers(filterEntries(
          resource.getReferrers(), new AcceptReferrerEntryPredicate()));

      return resource;
    }

    /**
     * Accept a resource text attribute value if attribute is permitted
     */
    private class AcceptProperty implements Predicate<String> {

      private ClassId typeId;

      public AcceptProperty(ClassId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(String attributeId) {
        TextAttributeId textAttrId = new TextAttributeId(typeId, attributeId);
        return textAttrEvaluator.hasPermission(user, textAttrId, permission);
      }
    }

    /**
     * Accepts a resource reference entry in the reference multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferenceEntryPredicate
        implements Predicate<Map.Entry<String, ResourceId>> {

      private ClassId typeId;

      public AcceptReferenceEntryPredicate(ClassId typeId) {
        this.typeId = typeId;
      }

      @Override
      public boolean apply(Map.Entry<String, ResourceId> entry) {
        String attributeId = entry.getKey();
        ResourceId reference = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(typeId, attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
               resourceEvaluator.hasPermission(user, reference, permission);
      }
    }

    /**
     * Accepts a resource referrer entry in the referrers multimap if 1) reference attribute is
     * permitted and 2) value is permitted
     */
    private class AcceptReferrerEntryPredicate implements Predicate<Map.Entry<String, ResourceId>> {

      @Override
      public boolean apply(Map.Entry<String, ResourceId> entry) {
        String attributeId = entry.getKey();
        ResourceId referrer = entry.getValue();

        ReferenceAttributeId refAttrId = new ReferenceAttributeId(referrer.getType(), attributeId);

        return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
               resourceEvaluator.hasPermission(user, referrer, permission);
      }
    }

  }


}
