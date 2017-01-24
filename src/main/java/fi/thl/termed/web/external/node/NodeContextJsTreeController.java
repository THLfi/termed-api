package fi.thl.termed.web.external.node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.Tree;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.transform.NodeTreeToJsTree;

import static fi.thl.termed.util.FunctionUtils.partialApplySecond;

@RestController
@RequestMapping(value = "/api/graphs/{graphId}/types/{typeId}/nodes/{nodeId}/trees",
    params = {"jstree=true", "context=true"})
public class NodeContextJsTreeController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetJsonMapping
  public List<JsTree> getContextJsTrees(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeId") UUID nodeId,
      @RequestParam(value = "attributeId", defaultValue = "broader") String attributeId,
      @RequestParam(value = "lang", defaultValue = "fi") String lang,
      @RequestParam(value = "referrers", defaultValue = "true") boolean referrers,
      @AuthenticationPrincipal User user) {

    Node node = nodeService.get(new NodeId(nodeId, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Function<Node, List<Node>> referenceLoadingFunction =
        partialApplySecond(new IndexedReferenceLoader(nodeService, user), attributeId);
    Function<Node, List<Node>> referrerLoadingFunction =
        partialApplySecond(new IndexedReferrerLoader(nodeService, user), attributeId);

    Set<Node> roots = Sets.newLinkedHashSet();
    Set<NodeId> pathIds = Sets.newHashSet();
    Set<NodeId> selectedIds = Sets.newHashSet();

    List<List<Node>> paths = GraphUtils.collectPaths(
        node, referrers ? referenceLoadingFunction : referrerLoadingFunction);
    roots.addAll(GraphUtils.findRoots(paths));
    pathIds.addAll(Lists.transform(ListUtils.flatten(paths), NodeId::new));
    selectedIds.add(new NodeId(node));

    // function to convert and load node into tree via attributeId
    Function<Node, Tree<Node>> toTree = new GraphUtils.ToTreeFunction<>(
        referrers ? referrerLoadingFunction : referenceLoadingFunction);

    Function<Tree<Node>, JsTree> toJsTree =
        new NodeTreeToJsTree(pathIds::contains,
                             selectedIds::contains,
                             "prefLabel", lang);

    return new ArrayList<>(roots).stream()
        .map(toTree)
        .map(toJsTree).collect(Collectors.toList());
  }

}
