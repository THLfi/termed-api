package fi.thl.termed.exchange.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.AbstractExporter;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.Tree;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.Service;

/**
 * Export resources with reference/referrer tree
 */
public class ResourceTreeExporter extends AbstractExporter<ResourceId, Resource, List<Resource>> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;

  public ResourceTreeExporter(Service<ResourceId, Resource> service,
                              Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao) {
    super(service);
    this.referenceAttributeDao = referenceAttributeDao;
  }

  @Override
  protected Map<String, Class> requiredArgs() {
    return ImmutableMap.<String, Class>builder()
        .put("schemeId", UUID.class)
        .put("typeId", String.class)
        .put("attributeId", String.class)
        .put("referrers", Boolean.class).build();
  }

  @Override
  protected List<Resource> doExport(List<Resource> values, Map<String, Object> args, User user) {
    UUID schemeId = (UUID) args.get("schemeId");
    String typeId = (String) args.get("typeId");
    String attributeId = (String) args.get("attributeId");
    Boolean referrers = (Boolean) args.get("referrers");

    ClassId domainId = new ClassId(schemeId, typeId);
    ReferenceAttributeId referenceAttributeId = new ReferenceAttributeId(domainId, attributeId);
    ReferenceAttribute referenceAttribute =
        referenceAttributeDao.get(referenceAttributeId, user).get();
    ClassId rangeId = new ClassId(referenceAttribute.getRange());

    java.util.function.Function<Resource, List<Resource>> referenceLoadingFunction =
        referrers ? new IndexedReferrerLoader(service, user, referenceAttributeId, rangeId)
                  : new IndexedReferenceLoader(service, user, referenceAttributeId, rangeId);

    // function to convert and load resource into tree via attributeId
    Function<Resource, Tree<Resource>> toTree =
        new GraphUtils.ToTreeFunction<Resource>(referenceLoadingFunction);

    // function to convert tree back into resource
    // where resource have refs populated based on the tree
    Function<Tree<Resource>, Resource> fromTree =
        new ToResourceTree(attributeId, referrers);

    return new ArrayList<>(values).stream()
        .map(toTree)
        .map(fromTree).collect(Collectors.toList());
  }

  /**
   * Recursively populates resource reference/referrer values from given tree.
   */
  private class ToResourceTree implements Function<Tree<Resource>, Resource> {

    private String attributeId;
    private boolean referrers;

    public ToResourceTree(String attributeId, boolean referrers) {
      this.attributeId = attributeId;
      this.referrers = referrers;
    }

    @Override
    public Resource apply(Tree<Resource> input) {
      Resource resource = new Resource(input.getData());

      if (referrers) {
        Multimap<String, Resource> refs = resource.getReferrers();
        refs.removeAll(attributeId);
        refs.putAll(attributeId,
                    input.getChildren().stream().map(this).collect(Collectors.toList()));
        resource.setReferrers(refs);
      } else {
        Multimap<String, Resource> refs = resource.getReferences();
        refs.removeAll(attributeId);
        refs.putAll(attributeId,
                    input.getChildren().stream().map(this).collect(Collectors.toList()));
        resource.setReferences(refs);
      }

      return resource;
    }

  }

}
