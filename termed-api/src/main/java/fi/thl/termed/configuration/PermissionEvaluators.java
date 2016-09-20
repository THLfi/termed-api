package fi.thl.termed.configuration;

import com.google.common.collect.ImmutableMultimap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.common.AppRolePermissionEvaluator;
import fi.thl.termed.permission.common.DaoPermissionEvaluator;
import fi.thl.termed.permission.common.DisjunctionPermissionEvaluator;
import fi.thl.termed.permission.common.TypeBasedDelegatingSpecificationEvaluator;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.resource.ResourceReferences;
import fi.thl.termed.spesification.resource.ResourceReferrers;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.resource.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.spesification.util.AndSpecification;
import fi.thl.termed.spesification.util.OrSpecification;

/**
 * Configures permission evaluators which are the base for authorizing object access.
 */
@Configuration
public class PermissionEvaluators {

  @Bean
  public PermissionEvaluator<UUID> schemeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionSystemDao) {

    PermissionEvaluator<UUID> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<UUID>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<UUID> schemeIdPermissionEvaluator =
        new DaoPermissionEvaluator<UUID>(schemePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<UUID>(
        appRolePermissionEvaluator, schemeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<ClassId> classIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<ClassId>, Void> classPermissionSystemDao) {

    PermissionEvaluator<ClassId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<ClassId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<ClassId> classIdPermissionEvaluator =
        new DaoPermissionEvaluator<ClassId>(classPermissionSystemDao);

    return new DisjunctionPermissionEvaluator<ClassId>(
        appRolePermissionEvaluator, classIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionSystemDao) {

    PermissionEvaluator<TextAttributeId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<TextAttributeId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator =
        new DaoPermissionEvaluator<TextAttributeId>(textAttributePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<TextAttributeId>(
        appRolePermissionEvaluator, textAttributeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionSystemDao) {

    PermissionEvaluator<ReferenceAttributeId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<ReferenceAttributeId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<ReferenceAttributeId> textAttributeIdPermissionEvaluator =
        new DaoPermissionEvaluator<ReferenceAttributeId>(
            referenceAttributePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<ReferenceAttributeId>(
        appRolePermissionEvaluator, textAttributeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<Specification<ResourceId, Resource>> resourceSpecificationPermissionEvaluator(
      final PermissionEvaluator<ClassId> classIdPermissionEvaluator,
      final PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator,
      final PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<Specification<ResourceId, Resource>> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<Specification<ResourceId, Resource>>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    final TypeBasedDelegatingSpecificationEvaluator<ResourceId, Resource> specificationEvaluator =
        new TypeBasedDelegatingSpecificationEvaluator<ResourceId, Resource>();

    // register evaluators for OR and AND type of composite specifications
    specificationEvaluator.registerEvaluatorByRawType(
        OrSpecification.class, new PermissionEvaluator<OrSpecification<ResourceId, Resource>>() {
          public boolean hasPermission(
              User u, OrSpecification<ResourceId, Resource> spec, Permission p) {
            for (Specification<ResourceId, Resource> s : spec.getSpecifications()) {
              if (!specificationEvaluator.hasPermission(u, s, p)) {
                return false;
              }
            }
            return true;
          }
        });
    specificationEvaluator.registerEvaluatorByRawType(
        AndSpecification.class, new PermissionEvaluator<AndSpecification<ResourceId, Resource>>() {
          public boolean hasPermission(
              User u, AndSpecification<ResourceId, Resource> spec, Permission p) {
            for (Specification<ResourceId, Resource> s : spec.getSpecifications()) {
              if (!specificationEvaluator.hasPermission(u, s, p)) {
                return false;
              }
            }
            return true;
          }
        });

    // register evaluators for individual resource specifications
    specificationEvaluator.registerEvaluator(
        ResourcesByClassId.class,
        new PermissionEvaluator<ResourcesByClassId>() {
          public boolean hasPermission(User u, ResourcesByClassId spec, Permission p) {
            return classIdPermissionEvaluator.hasPermission(u, spec.getClassId(), p);
          }
        });
    specificationEvaluator.registerEvaluator(
        ResourcesByTextAttributeValuePrefix.class,
        new PermissionEvaluator<ResourcesByTextAttributeValuePrefix>() {
          public boolean hasPermission(
              User u, ResourcesByTextAttributeValuePrefix spec, Permission p) {
            return textAttributeIdPermissionEvaluator.hasPermission(u, spec.getAttributeId(), p);
          }
        });
    specificationEvaluator.registerEvaluator(
        ResourceReferences.class, new PermissionEvaluator<ResourceReferences>() {
          public boolean hasPermission(User u, ResourceReferences spec, Permission p) {
            return referenceAttributeIdPermissionEvaluator.hasPermission(u, spec.getAttrId(), p) &&
                   classIdPermissionEvaluator.hasPermission(u, spec.getRangeId(), p);
          }
        }
    );
    specificationEvaluator.registerEvaluator(
        ResourceReferrers.class, new PermissionEvaluator<ResourceReferrers>() {
          public boolean hasPermission(User u, ResourceReferrers spec, Permission p) {
            return referenceAttributeIdPermissionEvaluator.hasPermission(u, spec.getAttrId(), p) &&
                   classIdPermissionEvaluator.hasPermission(u, new ClassId(spec.getObjectId()), p);
          }
        }
    );

    return new DisjunctionPermissionEvaluator<Specification<ResourceId, Resource>>(
        appRolePermissionEvaluator, specificationEvaluator);
  }

}
