package fi.thl.termed.permission.resource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class ResourceTextAttributeValuePermissionEvaluator
    implements PermissionEvaluator<ResourceAttributeValueId> {

  private PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator;

  public ResourceTextAttributeValuePermissionEvaluator(
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {
    this.textAttributeIdPermissionEvaluator = textAttributeIdPermissionEvaluator;
  }

  @Override
  public boolean hasPermission(User user, ResourceAttributeValueId valueId, Permission permission) {
    TextAttributeId attributeId =
        new TextAttributeId(new ClassId(valueId.getResourceId()), valueId.getAttributeId());
    return textAttributeIdPermissionEvaluator.hasPermission(user, attributeId, permission);
  }

}
