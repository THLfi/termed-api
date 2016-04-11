package fi.thl.termed.service;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.dao.ResourceBulkInsertDao;
import fi.thl.termed.repository.dao.ResourceDao;
import fi.thl.termed.repository.spesification.ResourceSpecificationBySchemeId;
import fi.thl.termed.util.MapUtils;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.rdf.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModelToResources;

@Service
@Transactional
public class ResourceRdfServiceImpl implements ResourceRdfService {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  @Qualifier("schemeService")
  private SchemeService schemeService;

  @Autowired
  @Qualifier("resourceService")
  private ResourceService resourceService;

  @Autowired
  private ResourceDao resourceDao;

  @Autowired
  private ResourceBulkInsertDao resourceBulkInsertDao;

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Model save(UUID schemeId, Model model, User currentUser) {
    Scheme scheme = schemeService.get(schemeId, currentUser);

    log.info("Parsing RDF-model");

    List<Resource> resources = new RdfModelToResources(scheme).apply(new JenaRdfModel(model));

    if (resourceDao.getKeys(new ResourceSpecificationBySchemeId(schemeId)).isEmpty() &&
        resourceBulkInsertDao.isSupported()) {

      log.info("Inserting {} resources", resources.size());

      List<Resource> processed =
          Lists.transform(resources,
                          Functions.compose(
                              new PopulateAuditFields(currentUser.getUsername(), new Date()),
                              new PopulateIdsFromUris()));

      resourceBulkInsertDao.insert(MapUtils.toMap(processed, new ExtractResourceIdFunction()));
    } else {
      resourceService.save(resources, currentUser);
    }

    // returns empty model for now
    return ModelFactory.createDefaultModel();
  }

  private class ExtractResourceIdFunction implements Function<Resource, ResourceId> {

    @Override
    public ResourceId apply(Resource input) {
      return new ResourceId(input);
    }
  }

  private class PopulateIdsFromUris implements Function<Resource, Resource> {

    @Override
    public Resource apply(Resource input) {
      input.setId(UUIDs.nameUUIDFromString(input.getUri()));
      for (Resource reference : input.getReferences().values()) {
        reference.setId(UUIDs.nameUUIDFromString(reference.getUri()));
      }
      return input;
    }
  }

  private class PopulateAuditFields implements Function<Resource, Resource> {

    private String currentUser;
    private Date currentDate;

    public PopulateAuditFields(String currentUser, Date currentDate) {
      this.currentUser = currentUser;
      this.currentDate = currentDate;
    }

    @Override
    public Resource apply(Resource input) {
      input.setCreatedBy(currentUser);
      input.setCreatedDate(currentDate);
      input.setLastModifiedBy(currentUser);
      input.setLastModifiedDate(currentDate);
      return input;
    }
  }

}
