package fi.thl.termed.service.resource.internal;

import com.google.common.base.Predicate;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;

import static com.google.common.collect.Multimaps.filterEntries;
import static com.google.common.collect.Multimaps.filterKeys;

public class ResourceAttributePermissionFilter implements Function<Resource, Resource> {

  private PermissionEvaluator<ClassId> classEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttrEvaluator;
  private PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator;

  private User user;
  private Permission permission;

  public ResourceAttributePermissionFilter(
      PermissionEvaluator<ClassId> classEvaluator,
      PermissionEvaluator<TextAttributeId> textAttrEvaluator,
      PermissionEvaluator<ReferenceAttributeId> refAttrEvaluator,
      User user, Permission permission) {
    this.classEvaluator = classEvaluator;
    this.textAttrEvaluator = textAttrEvaluator;
    this.refAttrEvaluator = refAttrEvaluator;
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
  private class AcceptReferenceEntryPredicate implements Predicate<Map.Entry<String, Resource>> {

    private ClassId typeId;

    public AcceptReferenceEntryPredicate(ClassId typeId) {
      this.typeId = typeId;
    }

    @Override
    public boolean apply(Map.Entry<String, Resource> entry) {
      String attributeId = entry.getKey();
      Resource reference = entry.getValue();

      ReferenceAttributeId refAttrId = new ReferenceAttributeId(typeId, attributeId);
      ClassId refTypeId = new ClassId(reference);

      return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
             classEvaluator.hasPermission(user, refTypeId, permission);
    }
  }

  /**
   * Accepts a resource referrer entry in the referrers multimap if 1) reference attribute is
   * permitted and 2) value is permitted
   */
  private class AcceptReferrerEntryPredicate implements Predicate<Map.Entry<String, Resource>> {

    @Override
    public boolean apply(Map.Entry<String, Resource> entry) {
      String attributeId = entry.getKey();
      Resource referrer = entry.getValue();

      ClassId refTypeId = new ClassId(referrer);
      ReferenceAttributeId refAttrId = new ReferenceAttributeId(refTypeId, attributeId);

      return refAttrEvaluator.hasPermission(user, refAttrId, permission) &&
             classEvaluator.hasPermission(user, refTypeId, permission);
    }
  }

}
