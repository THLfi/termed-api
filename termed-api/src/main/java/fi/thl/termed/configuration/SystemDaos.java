package fi.thl.termed.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.dao.jdbc.JdbcClassDao;
import fi.thl.termed.dao.jdbc.JdbcClassPermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcClassPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceDao;
import fi.thl.termed.dao.jdbc.JdbcResourceReferenceAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceTextAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcSchemeDao;
import fi.thl.termed.dao.jdbc.JdbcSchemePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcSchemePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcSchemeRoleDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcUserDao;
import fi.thl.termed.dao.jdbc.JdbcUserSchemeRoleDao;
import fi.thl.termed.dao.util.CachedSystemDao;
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
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.StrictLangValue;

/**
 * Configures system DAOs which are at the lowest level of the application. A system DAO typically
 * operates on single database table and can be cached.
 */
@Configuration
public class SystemDaos {

  @Bean
  public SystemDao<String, User> userSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcUserDao(dataSource));
  }

  @Bean
  public SystemDao<UserSchemeRoleId, Empty> userSchemeRoleSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcUserSchemeRoleDao(dataSource));
  }

  @Bean
  public SystemDao<String, Property> propertySystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcPropertyDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<String>, LangValue> propertyPropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcPropertyPropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<UUID, Scheme> schemeSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemeDao(dataSource));
  }

  @Bean
  public SystemDao<SchemeRole, Empty> schemeRoleSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemeRoleDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<UUID>, Empty> schemePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<UUID>, LangValue> schemePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ClassId, Class> classSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ClassId>, Empty> classPermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassPermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ClassId>, LangValue> classPropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassPropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ReferenceAttributeId>, Empty> referenceAttributePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<TextAttributeId>, Empty> textAttributePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceId, Resource> resourceSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceReferenceAttributeValueDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceTextAttributeValueDao(dataSource));
  }

}
