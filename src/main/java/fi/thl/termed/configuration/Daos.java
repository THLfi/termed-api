package fi.thl.termed.configuration;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.dao.util.SecureDao;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.domain.function.ObjectRolePermissionToObjectId;
import fi.thl.termed.domain.function.PropertyValueIdToSubjectId;
import fi.thl.termed.domain.function.ResourceAttributeValueIdToReferenceAttributeId;
import fi.thl.termed.domain.function.ResourceAttributeValueIdToTextAttributeId;
import fi.thl.termed.domain.function.ResourceIdToClassId;
import fi.thl.termed.domain.function.ResourceToClassId;
import fi.thl.termed.domain.function.SchemeRoleToSchemeId;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.common.MappingPermissionEvaluator;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.StrictLangValue;

/**
 * Configures DAOs, typically with some sort of permission evaluating strategy.
 */
@Configuration
public class Daos {

  @Bean
  public Dao<String, User> userDao(SystemDao<String, User> userSystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<String, User>(userSystemDao, rolePermissions);
  }

  @Bean
  public Dao<UserSchemeRoleId, Empty> userSchemeRoleDao(
      SystemDao<UserSchemeRoleId, Empty> userSchemeRoleSystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<UserSchemeRoleId, Empty>(userSchemeRoleSystemDao, rolePermissions);
  }

  @Bean
  public Dao<String, Property> propertyDao(SystemDao<String, Property> propertySystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.USER, Permission.READ)
        .putAll(AppRole.ADMIN, Permission.READ)
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<String, Property>(propertySystemDao, rolePermissions);
  }

  @Bean
  public Dao<PropertyValueId<String>, LangValue> propertyPropertyValueDao(
      SystemDao<PropertyValueId<String>, LangValue> propertyPropertySystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.USER, Permission.READ)
        .putAll(AppRole.ADMIN, Permission.READ)
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<PropertyValueId<String>, LangValue>(propertyPropertySystemDao,
                                                             rolePermissions);
  }

  @Bean
  public Dao<UUID, Scheme> schemeDao(SystemDao<UUID, Scheme> schemeSystemDao,
                                     PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {
    return new SecureDao<UUID, Scheme>(schemeSystemDao, schemeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<SchemeRole, Empty> schemeRoleDao(SystemDao<SchemeRole, Empty> schemeRoleSystemDao,
                                              PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<SchemeRole> evaluator =
        new MappingPermissionEvaluator<SchemeRole, UUID>(
            new SchemeRoleToSchemeId(), schemeIdPermissionEvaluator);

    return new SecureDao<SchemeRole, Empty>(schemeRoleSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionDao(
      SystemDao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionSystemDao,
      PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<UUID>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<UUID>, UUID>(
            new ObjectRolePermissionToObjectId<UUID>(), schemeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<UUID>, GrantedPermission>(
        schemePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao(
      SystemDao<PropertyValueId<UUID>, LangValue> schemePropertyValueSystemDao,
      PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<UUID>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<UUID>, UUID>(
            new PropertyValueIdToSubjectId<UUID>(), schemeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<UUID>, LangValue>(
        schemePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ClassId, Class> classDao(SystemDao<ClassId, Class> classSystemDao,
                                      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {
    return new SecureDao<ClassId, Class>(classSystemDao, classIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionDao(
      SystemDao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<ClassId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<ClassId>, ClassId>(
            new ObjectRolePermissionToObjectId<ClassId>(), classIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<ClassId>, GrantedPermission>(
        classPermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao(
      SystemDao<PropertyValueId<ClassId>, LangValue> classPropertyValueSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<ClassId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<ClassId>, ClassId>(
            new PropertyValueIdToSubjectId<ClassId>(), classIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<ClassId>, LangValue>(
        classPropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao(
      SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {
    return new SecureDao<ReferenceAttributeId, ReferenceAttribute>(
        referenceAttributeSystemDao, referenceAttributeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionDao(
      SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<ReferenceAttributeId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<ReferenceAttributeId>, ReferenceAttributeId>(
            new ObjectRolePermissionToObjectId<ReferenceAttributeId>(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission>(
        referenceAttributePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao(
      SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<ReferenceAttributeId>, ReferenceAttributeId>(
            new PropertyValueIdToSubjectId<ReferenceAttributeId>(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<ReferenceAttributeId>, LangValue>(
        referenceAttributePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<TextAttributeId, TextAttribute> textAttributeDao(
      SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {
    return new SecureDao<TextAttributeId, TextAttribute>(
        textAttributeSystemDao, textAttributeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionDao(
      SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<TextAttributeId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<TextAttributeId>, TextAttributeId>(
            new ObjectRolePermissionToObjectId<TextAttributeId>(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<TextAttributeId>, GrantedPermission>(
        textAttributePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao(
      SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<TextAttributeId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<TextAttributeId>, TextAttributeId>(
            new PropertyValueIdToSubjectId<TextAttributeId>(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<TextAttributeId>, LangValue>(
        textAttributePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ResourceId, Resource> resourceDao(
      SystemDao<ResourceId, Resource> resourceSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator,
      PermissionEvaluator<Specification<ResourceId, Resource>> resourceSpecificationPermissionEvaluator) {

    PermissionEvaluator<ResourceId> keyEvaluator =
        new MappingPermissionEvaluator<ResourceId, ClassId>(
            new ResourceIdToClassId(), classIdPermissionEvaluator);
    PermissionEvaluator<Resource> valEvaluator =
        new MappingPermissionEvaluator<Resource, ClassId>(
            new ResourceToClassId(), classIdPermissionEvaluator);

    return new SecureDao<ResourceId, Resource>(
        resourceSystemDao, keyEvaluator, valEvaluator,
        resourceSpecificationPermissionEvaluator, true);
  }

  @Bean
  public Dao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueDao(
      SystemDao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ResourceAttributeValueId> evaluator =
        new MappingPermissionEvaluator<ResourceAttributeValueId, ReferenceAttributeId>(
            new ResourceAttributeValueIdToReferenceAttributeId(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<ResourceAttributeValueId, ResourceId>(
        resourceReferenceAttributeValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueDao(
      SystemDao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ResourceAttributeValueId> evaluator =
        new MappingPermissionEvaluator<ResourceAttributeValueId, TextAttributeId>(
            new ResourceAttributeValueIdToTextAttributeId(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<ResourceAttributeValueId, StrictLangValue>(
        resourceTextAttributeValueSystemDao, evaluator, true);
  }

}
