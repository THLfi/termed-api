package fi.thl.termed.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeAndResources;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.exchange.Exporter;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.web.DumpAndRestoreController;
import fi.thl.termed.web.PropertyController;
import fi.thl.termed.web.ResourceContextJsTreeController;
import fi.thl.termed.web.ResourceControllerSpringImpl;
import fi.thl.termed.web.ResourceRdfController;
import fi.thl.termed.web.ResourceTableController;
import fi.thl.termed.web.ResourceTreeController;
import fi.thl.termed.web.SchemeController;
import fi.thl.termed.web.UserController;

/**
 * Configures controllers.
 */
@Configuration
public class Controllers {

  @Bean
  public DumpAndRestoreController dumpAndRestoreController(
      Service<UUID, SchemeAndResources> schemeAndResourcesService,
      Dao<UUID, Scheme> schemeDao) {
    return new DumpAndRestoreController(schemeAndResourcesService, schemeDao);
  }

  @Bean
  public ResourceControllerSpringImpl resourceController(
      Service<ResourceId, Resource> resourceService,
      Dao<ClassId, Class> classDao,
      Dao<TextAttributeId, TextAttribute> textAttributeDao) {
    return new ResourceControllerSpringImpl(resourceService, classDao, textAttributeDao);
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
  public ResourceTableController resourceTableController(
      Exchange<ResourceId, Resource, List<String[]>> resourceTableExchange) {
    return new ResourceTableController(resourceTableExchange);
  }

  @Bean
  public ResourceRdfController resourceRdfController(
      Exchange<ResourceId, Resource, RdfModel> resourceRdfExchange) {
    return new ResourceRdfController(resourceRdfExchange);
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

}
