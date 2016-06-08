package fi.thl.termed.exchange.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.ListUtils;
import fi.thl.termed.util.Tree;

/**
 * Exporter for jstree visualization.
 */
public class ResourceContextJsTreeExporter
    extends AbstractExporter<ResourceId, Resource, List<JsTree>> {

  public ResourceContextJsTreeExporter(Service<ResourceId, Resource> service) {
    super(service);
  }

  @Override
  protected Map<String, Class> requiredArgs() {
    return ImmutableMap.<String, Class>of(
        "attributeId", String.class,
        "referrers", Boolean.class,
        "labelAttributeId", String.class,
        "lang", String.class);
  }

  @Override
  protected List<JsTree> doExport(List<Resource> values, Map<String, Object> args, User user) {
    String attributeId = (String) args.get("attributeId");
    Boolean referrers = (Boolean) args.get("referrers");
    String labelAttributeId = (String) args.get("labelAttributeId");
    String lang = (String) args.get("lang");

    Function<Resource, ResourceId> toResourceId = new ToResourceId();

    Function<Resource, List<Resource>> referenceLoadingFunction =
        new IndexedReferenceLoader(service, user, attributeId);
    Function<Resource, List<Resource>> referrerLoadingFunction =
        new IndexedReferrerLoader(service, user, attributeId);

    Set<Resource> roots = Sets.newLinkedHashSet();
    Set<ResourceId> pathIds = Sets.newHashSet();
    Set<ResourceId> selectedIds = Sets.newHashSet();

    for (Resource resource : values) {
      List<List<Resource>> paths = GraphUtils.collectPaths(
          resource, referrers ? referenceLoadingFunction : referrerLoadingFunction);
      roots.addAll(GraphUtils.findRoots(paths));
      pathIds.addAll(Lists.transform(ListUtils.flatten(paths), toResourceId));
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
