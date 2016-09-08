package fi.thl.termed.permission.resource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class ResourceReferenceAttributeValuePermissionEvaluator
    implements PermissionEvaluator<ResourceAttributeValueId> {

  private PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator;

  public ResourceReferenceAttributeValuePermissionEvaluator(
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {
    this.referenceAttributeIdPermissionEvaluator = referenceAttributeIdPermissionEvaluator;
  }

  @Override
  public boolean hasPermission(User user, ResourceAttributeValueId valueId, Permission permission) {
    ReferenceAttributeId attributeId =
        new ReferenceAttributeId(new ClassId(valueId.getResourceId()), valueId.getAttributeId());
    return referenceAttributeIdPermissionEvaluator.hasPermission(user, attributeId, permission);
  }

}
