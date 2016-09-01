package fi.thl.termed.exchange.rdf;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.AbstractExchange;
import fi.thl.termed.service.Service;
import fi.thl.termed.util.rdf.RdfModel;

public class ResourceRdfExchange extends AbstractExchange<ResourceId, Resource, RdfModel> {

  private Service<UUID, Scheme> schemeService;

  public ResourceRdfExchange(Service<ResourceId, Resource> resourceService,
                             Service<UUID, Scheme> schemeService) {
    super(resourceService);
    this.schemeService = schemeService;
  }

  @Override
  protected Map<String, Class> requiredArgs() {
    return ImmutableMap.<String, Class>of("schemeId", UUID.class);
  }

  @Override
  protected RdfModel doExport(List<Resource> values, Map<String, Object> args, User currentUser) {
    Scheme scheme = schemeService.get((UUID) args.get("schemeId"), currentUser);
    return new ResourcesToRdfModel(scheme).apply(values);
  }

  @Override
  protected List<Resource> doImport(RdfModel value, Map<String, Object> args, User currentUser) {
    Scheme scheme = schemeService.get((UUID) args.get("schemeId"), currentUser);
    return new RdfModelToResources(scheme).apply(value);
  }

}
