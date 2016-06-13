package fi.thl.termed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.dao.jdbc.JdbcClassDao;
import fi.thl.termed.dao.jdbc.JdbcClassPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceDao;
import fi.thl.termed.dao.jdbc.JdbcResourceReferenceAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceTextAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcSchemeDao;
import fi.thl.termed.dao.jdbc.JdbcSchemePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributePropertyValueDao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.exchange.Exporter;
import fi.thl.termed.exchange.impl.RdfModelExchange;
import fi.thl.termed.exchange.impl.ResourceContextJsTreeExporter;
import fi.thl.termed.exchange.impl.ResourceTreeExporter;
import fi.thl.termed.index.Index;
import fi.thl.termed.index.lucene.LuceneIndex;
import fi.thl.termed.index.lucene.ResourceDocumentConverter;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.repository.impl.AbstractRepository;
import fi.thl.termed.repository.impl.ClassRepositoryImpl;
import fi.thl.termed.repository.impl.PropertyRepositoryImpl;
import fi.thl.termed.repository.impl.ReferenceAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.ResourceRepositoryImpl;
import fi.thl.termed.repository.impl.SchemeRepositoryImpl;
import fi.thl.termed.repository.impl.TextAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.UserRepositoryImpl;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.impl.PropertyServiceImpl;
import fi.thl.termed.service.impl.ResourceServiceImpl;
import fi.thl.termed.service.impl.SchemeServiceImpl;
import fi.thl.termed.service.impl.UserServiceImpl;
import fi.thl.termed.util.DateTypeAdapter;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapTypeAdapterFactory;
import fi.thl.termed.util.StrictLangValue;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.web.PropertyController;
import fi.thl.termed.web.RdfController;
import fi.thl.termed.web.ResourceContextJsTreeController;
import fi.thl.termed.web.ResourceController;
import fi.thl.termed.web.ResourceTreeController;
import fi.thl.termed.web.SchemeController;
import fi.thl.termed.web.UserController;

@Configuration
public class ApplicationBeans {

  // Utils

  @Bean
  public Gson gson() {
    return new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(Date.class, new DateTypeAdapter().nullSafe())
        .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
        .create();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Controllers

  @Bean
  public ResourceController resourceController(Service<ResourceId, Resource> resourceService) {
    return new ResourceController(resourceService);
  }

  @Bean
  public SchemeController schemeController(Service<UUID, Scheme> schemeService) {
    return new SchemeController(schemeService);
  }

  @Bean
  public ResourceTreeController resourceTreeController(
      Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter) {
    return new ResourceTreeController(resourceTreeExporter);
  }

  @Bean
  public ResourceContextJsTreeController resourceContextJsTreeController(
      Exporter<ResourceId, Resource, List<JsTree>> resourceContextJsTreeExporter) {
    return new ResourceContextJsTreeController(resourceContextJsTreeExporter);
  }

  @Bean
  public RdfController rdfController(Exchange<ResourceId, Resource, RdfModel> rdfExchange) {
    return new RdfController(rdfExchange);
  }

  @Bean
  public PropertyController propertyController(Service<String, Property> propertyService) {
    return new PropertyController(propertyService);
  }

  @Bean
  public UserController userController(Service<String, User> userService,
                                       PasswordEncoder passwordEncoder) {
    return new UserController(userService, passwordEncoder);
  }

  // Exporters

  @Bean
  public Exchange<ResourceId, Resource, RdfModel> rdfExchange(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService) {
    return new RdfModelExchange(resourceService, schemeService);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<JsTree>> resourceContextJsTreeExporter(
      Service<ResourceId, Resource> resourceService) {
    return new ResourceContextJsTreeExporter(resourceService);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter(
      Service<ResourceId, Resource> resourceService) {
    return new ResourceTreeExporter(resourceService);
  }

  // Services

  @Bean
  public Service<String, User> userService(
      Repository<String, User> userRepository) {
    return new UserServiceImpl(userRepository);
  }

  @Bean
  public Service<String, Property> propertyService(
      Repository<String, Property> propertyRepository) {
    return new PropertyServiceImpl(propertyRepository);
  }

  @Bean
  public Service<UUID, Scheme> schemeService(
      Repository<UUID, Scheme> schemeRepository,
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      Dao<ResourceId, Resource> resourceDao) {
    return new SchemeServiceImpl(schemeRepository,
                                 resourceRepository,
                                 resourceIndex,
                                 resourceDao);
  }

  @Bean
  public Service<ResourceId, Resource> resourceService(
      Repository<UUID, Scheme> schemeRepository,
      Repository<ClassId, Class> classRepository,
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      Dao<UUID, Scheme> schemeDao,
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao) {
    return new ResourceServiceImpl(schemeRepository,
                                   classRepository,
                                   resourceRepository,
                                   resourceIndex,
                                   schemeDao,
                                   resourceDao,
                                   referenceAttributeValueDao);
  }

  // Indices

  @Bean
  public Index<ResourceId, Resource> resourceIndex(
      @Value("${fi.thl.termed.index:}") String indexPath, Gson gson) {
    return new LuceneIndex<ResourceId, Resource>(indexPath, new ResourceDocumentConverter(gson));
  }

  // Repositories

  @Bean
  public Repository<String, User> userRepository(DataSource dataSource) {
    return new UserRepositoryImpl(dataSource);
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
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      AbstractRepository<ClassId, Class> classRepository) {
    return new SchemeRepositoryImpl(schemeDao,
                                    schemePropertyValueDao,
                                    classRepository);
  }

  @Bean
  public AbstractRepository<ClassId, Class> classRepository(
      Dao<ClassId, Class> classDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    return new ClassRepositoryImpl(classDao,
                                   classPropertyValueDao,
                                   textAttributeRepository,
                                   referenceAttributeRepository);
  }

  @Bean
  public AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao) {
    return new TextAttributeRepositoryImpl(textAttributeDao,
                                           textAttributePropertyValueDao);
  }

  @Bean
  public AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao) {
    return new ReferenceAttributeRepositoryImpl(referenceAttributeDao,
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

  // DAOs

  @Bean
  public Dao<String, Property> propertyDao(DataSource dataSource) {
    return new JdbcPropertyDao(dataSource);
  }

  @Bean
  public Dao<PropertyValueId<String>, LangValue> propertyPropertyValueDao(DataSource dataSource) {
    return new JdbcPropertyPropertyValueDao(dataSource);
  }

  @Bean
  public Dao<UUID, Scheme> schemeDao(DataSource dataSource) {
    return new JdbcSchemeDao(dataSource);
  }

  @Bean
  public Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao(DataSource dataSource) {
    return new JdbcSchemePropertyValueDao(dataSource);
  }

  @Bean
  public Dao<ClassId, Class> classDao(DataSource dataSource) {
    return new JdbcClassDao(dataSource);
  }

  @Bean
  public Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao(DataSource dataSource) {
    return new JdbcClassPropertyValueDao(dataSource);
  }

  @Bean
  public Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao(
      DataSource dataSource) {
    return new JdbcReferenceAttributeDao(dataSource);
  }

  @Bean
  public Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao(
      DataSource dataSource) {
    return new JdbcReferenceAttributePropertyValueDao(dataSource);
  }

  @Bean
  public Dao<TextAttributeId, TextAttribute> textAttributeDao(DataSource dataSource) {
    return new JdbcTextAttributeDao(dataSource);
  }

  @Bean
  public Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao(
      DataSource dataSource) {
    return new JdbcTextAttributePropertyValueDao(dataSource);
  }

  @Bean
  public Dao<ResourceId, Resource> resourceDao(DataSource dataSource) {
    return new JdbcResourceDao(dataSource);
  }

  @Bean
  public Dao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueDao(
      DataSource dataSource) {
    return new JdbcResourceReferenceAttributeValueDao(dataSource);
  }

  @Bean
  public Dao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueDao(
      DataSource dataSource) {
    return new JdbcResourceTextAttributeValueDao(dataSource);
  }

}
