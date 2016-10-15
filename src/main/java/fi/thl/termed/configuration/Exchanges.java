package fi.thl.termed.configuration;

import com.google.gson.Gson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.exchange.Exporter;
import fi.thl.termed.exchange.rdf.ResourceRdfExchange;
import fi.thl.termed.exchange.table.ResourceTableExchange;
import fi.thl.termed.exchange.tree.ResourceContextJsTreeExporter;
import fi.thl.termed.exchange.tree.ResourceTreeExporter;
import fi.thl.termed.service.Service;
import fi.thl.termed.util.rdf.RdfModel;

/**
 * Configures Exporters and Importers (or both together as an Exchange)
 */
@Configuration
public class Exchanges {

  @Bean
  public Exchange<ResourceId, Resource, List<String[]>> resourceTableExchange(
      Service<ResourceId, Resource> resourceService,
      Gson gson) {
    return new ResourceTableExchange(resourceService, gson);
  }

  @Bean
  public Exchange<ResourceId, Resource, RdfModel> resourceRdfExchange(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService) {
    return new ResourceRdfExchange(resourceService, schemeService);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<JsTree>> resourceContextJsTreeExporter(
      Service<ResourceId, Resource> resourceService,
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao) {
    return new ResourceContextJsTreeExporter(resourceService, referenceAttributeDao);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter(
      Service<ResourceId, Resource> resourceService,
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao) {
    return new ResourceTreeExporter(resourceService, referenceAttributeDao);
  }

}
