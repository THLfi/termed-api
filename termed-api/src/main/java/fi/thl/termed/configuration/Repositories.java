package fi.thl.termed.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.ObjectRolePermission;
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
import fi.thl.termed.repository.Repository;
import fi.thl.termed.repository.impl.AbstractRepository;
import fi.thl.termed.repository.impl.ClassRepositoryImpl;
import fi.thl.termed.repository.impl.PropertyRepositoryImpl;
import fi.thl.termed.repository.impl.ReferenceAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.ResourceRepositoryImpl;
import fi.thl.termed.repository.impl.SchemeRepositoryImpl;
import fi.thl.termed.repository.impl.TextAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.UserRepositoryImpl;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.StrictLangValue;

/**
 * Configures Repositories. A Repository is intended to abstract persisting of complex objects.
 */
@Configuration
public class Repositories {

  @Bean
  public Repository<String, User> userRepository(
      Dao<String, User> userDao,
      Dao<UserSchemeRoleId, Empty> userSchemeRoleDao) {
    return new UserRepositoryImpl(userDao, userSchemeRoleDao);
  }

  @Bean
  public Repository<String, Property> propertyRepository(
      Dao<String, Property> propertyDao,
      Dao<PropertyValueId<String>, LangValue> propertyPropertyValueDao) {
    return new PropertyRepositoryImpl(propertyDao,
                                      propertyPropertyValueDao);
  }

  @Bean
  public Repository<UUID, Scheme> schemeRepository(
      Dao<UUID, Scheme> schemeDao,
      Dao<SchemeRole, Empty> schemeRoleDao,
      Dao<ObjectRolePermission<UUID>, Empty> schemePermissionDao,
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      AbstractRepository<ClassId, Class> classRepository) {
    return new SchemeRepositoryImpl(schemeDao,
                                    schemeRoleDao,
                                    schemePermissionDao,
                                    schemePropertyValueDao,
                                    classRepository);
  }

  @Bean
  public AbstractRepository<ClassId, Class> classRepository(
      Dao<ClassId, Class> classDao,
      Dao<ObjectRolePermission<ClassId>, Empty> classPermissionDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    return new ClassRepositoryImpl(classDao,
                                   classPermissionDao,
                                   classPropertyValueDao,
                                   textAttributeRepository,
                                   referenceAttributeRepository);
  }

  @Bean
  public AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, Empty> textAttributePermissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao) {
    return new TextAttributeRepositoryImpl(textAttributeDao,
                                           textAttributePermissionDao,
                                           textAttributePropertyValueDao);
  }

  @Bean
  public AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, Empty> referenceAttributePermissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao) {
    return new ReferenceAttributeRepositoryImpl(referenceAttributeDao,
                                                referenceAttributePermissionDao,
                                                referenceAttributePropertyValueDao);
  }

  @Bean
  public Repository<ResourceId, Resource> resourceRepository(
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao,
      Dao<UUID, Scheme> schemeDao,
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      Dao<ClassId, Class> classDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao) {
    return new ResourceRepositoryImpl(resourceDao,
                                      textAttributeValueDao,
                                      referenceAttributeValueDao,
                                      schemeDao,
                                      schemePropertyValueDao,
                                      classDao,
                                      classPropertyValueDao);
  }

}
