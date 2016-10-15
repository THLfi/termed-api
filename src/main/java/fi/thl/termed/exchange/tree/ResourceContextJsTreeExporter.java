package fi.thl.termed.exchange.tree;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.AbstractExporter;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.Tree;

/**
 * Exporter for jstree visualization.
 */
public class ResourceContextJsTreeExporter
    extends AbstractExporter<ResourceId, Resource, List<JsTree>> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;

  public ResourceContextJsTreeExporter(
      Service<ResourceId, Resource> service,
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
        .put("referrers", Boolean.class)
        .put("labelAttributeId", String.class)
        .put("lang", String.class).build();
  }

  @Override
  protected List<JsTree> doExport(List<Resource> values, Map<String, Object> args, User user) {
    UUID schemeId = (UUID) args.get("schemeId");
    String typeId = (String) args.get("typeId");
    String attributeId = (String) args.get("attributeId");
    Boolean referrers = (Boolean) args.get("referrers");
    String labelAttributeId = (String) args.get("labelAttributeId");
    String lang = (String) args.get("lang");

    ClassId domainId = new ClassId(schemeId, typeId);
    ReferenceAttributeId referenceAttributeId = new ReferenceAttributeId(domainId, attributeId);
    ReferenceAttribute referenceAttribute =
        referenceAttributeDao.get(referenceAttributeId, user).get();

    if (referenceAttribute == null) {
      return Lists.newArrayList();
    }

    ClassId rangeId = new ClassId(referenceAttribute.getRange());

    Function<Resource, List<Resource>> referenceLoadingFunction =
        new IndexedReferenceLoader(service, user, referenceAttributeId, rangeId);
    Function<Resource, List<Resource>> referrerLoadingFunction =
        new IndexedReferrerLoader(service, user, referenceAttributeId, rangeId);

    Set<Resource> roots = Sets.newLinkedHashSet();
    Set<ResourceId> pathIds = Sets.newHashSet();
    Set<ResourceId> selectedIds = Sets.newHashSet();

    for (Resource resource : values) {
      List<List<Resource>> paths = GraphUtils.collectPaths(
          resource, referrers ? referenceLoadingFunction : referrerLoadingFunction);
      roots.addAll(GraphUtils.findRoots(paths));
      pathIds.addAll(Lists.transform(ListUtils.flatten(paths), new ToResourceId()));
      selectedIds.add(new ResourceId(resource));
    }

    // function to convert and load resource into tree via attributeId
    Function<Resource, Tree<Resource>> toTree = new GraphUtils.ToTreeFunction<Resource>(
        referrers ? referrerLoadingFunction : referenceLoadingFunction);

    Function<Tree<Resource>, JsTree> toJsTree =
        new ResourceTreeToJsTree(Predicates.in(pathIds),
                                 Predicates.in(selectedIds),
                                 labelAttributeId, lang);

    return Lists.transform(Lists.newArrayList(roots), Functions.compose(toJsTree, toTree));
  }

}
