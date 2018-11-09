package fi.thl.termed.web.node;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static fi.thl.termed.util.collect.FunctionUtils.partialApplySecond;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.GraphUtils;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.Tree;
import fi.thl.termed.util.URIs;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/graphs/{graphId}/types/{typeId}/nodes/{nodeId}/trees",
    params = {"jstree=true", "context=true"})
public class NodeJsTreeReadController {

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

    Function<Node, ImmutableList<Node>> referenceLoadingFunction =
        partialApplySecond(new IndexedReferenceLoader(nodeService, user), attributeId);
    Function<Node, ImmutableList<Node>> referrerLoadingFunction =
        partialApplySecond(new IndexedReferrerLoader(nodeService, user), attributeId);

    Set<NodeId> selectedIds = ImmutableSet.of(node.identifier());

    List<List<Node>> paths = GraphUtils.collectPaths(
        node, referrers ? referenceLoadingFunction : referrerLoadingFunction);

    Set<NodeId> pathIds = paths.stream()
        .flatMap(Collection::stream)
        .map(Node::identifier)
        .collect(toImmutableSet());

    Set<Node> roots = paths.stream()
        .map(path -> path.stream().findFirst())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toImmutableSet());

    // function to convert and load node into tree via attributeId
    Function<Node, Tree<Node>> toTree = new GraphUtils.ToTreeFunction<>(
        referrers ? referrerLoadingFunction : referenceLoadingFunction);

    Function<Tree<Node>, JsTree> toJsTree = new NodeTreeToJsTree(
        pathIds::contains,
        selectedIds::contains,
        "prefLabel", lang);

    return roots.stream()
        .map(toTree)
        .map(toJsTree)
        .collect(toImmutableList());
  }

  private class NodeTreeToJsTree implements Function<Tree<Node>, JsTree> {

    private Predicate<NodeId> addChildrenPredicate;

    private Predicate<NodeId> selectedPredicate;

    private String labelAttributeId;

    private String lang;

    NodeTreeToJsTree(
        Predicate<NodeId> addChildrenPredicate,
        Predicate<NodeId> selectedPredicate,
        String labelAttributeId, String lang) {
      checkNotNull(addChildrenPredicate);
      checkNotNull(selectedPredicate);
      checkArgument(labelAttributeId.matches(RegularExpressions.CODE));
      checkArgument(lang.matches(RegularExpressions.CODE));
      this.addChildrenPredicate = addChildrenPredicate;
      this.selectedPredicate = selectedPredicate;
      this.labelAttributeId = labelAttributeId;
      this.lang = lang;
    }

    @Override
    public JsTree apply(Tree<Node> nodeTree) {
      Node node = nodeTree.getData();
      NodeId nodeId = node.identifier();

      JsTree jsTree = new JsTree();

      jsTree.setId(DigestUtils.sha1Hex(
          nodeTree.getPath().stream()
              .map(Node::getId)
              .map(UUID::toString)
              .collect(Collectors.joining("."))));
      jsTree.setIcon(false);
      jsTree.setText(htmlEscape(getLocalizedLabel(node)) +
          smallMuted(
              htmlEscape(getCode(node)),
              htmlEscape(node.getUri().orElse(""))));

      jsTree.setState(ImmutableMap.of(
          "opened", addChildrenPredicate.test(nodeId),
          "selected", selectedPredicate.test(nodeId)));

      String conceptUrl = "/graphs/" + node.getTypeGraphId() +
          "/types/" + node.getTypeId() +
          "/nodes/" + node.getId();

      jsTree.setLinkElementAttributes(
          ImmutableMap.of("href", conceptUrl));
      jsTree.setListElementAttributes(
          ImmutableMap.of(
              "nodeGraphId", Strings.nullToEmpty(UUIDs.toString(node.getTypeGraphId())),
              "nodeTypeId", Strings.nullToEmpty(node.getTypeId()),
              "nodeId", UUIDs.toString(node.getId())));

      List<Tree<Node>> children = nodeTree.getChildren();

      if (children.isEmpty()) {
        jsTree.setChildren(false);
      } else if (addChildrenPredicate.test(nodeId)) {
        jsTree.setChildren(children.stream().map(this).collect(Collectors.toList()));
      } else {
        jsTree.setChildren(true);
      }

      return jsTree;
    }

    private String getLocalizedLabel(Node node) {
      Collection<StrictLangValue> langValues = node.getProperties().get(labelAttributeId);

      for (StrictLangValue langValue : langValues) {
        if (Objects.equals(lang, langValue.getLang())) {
          return langValue.getValue();
        }
      }

      StrictLangValue langValue = Iterables.getFirst(langValues, null);

      if (langValue != null) {
        String langInfo = langValue.getLang().isEmpty() ? "" : " (" + langValue.getLang() + ")";
        return langValue.getValue() + langInfo;
      }

      return "-";
    }

    private String smallMuted(String text, String title) {
      return " <small class='text-muted' title=" + title + ">" + text + "</small>";
    }

    private String getCode(Node node) {
      return node.getUri()
          .map(URIs::localName)
          .orElseGet(() -> node.getCode().orElse(""));
    }

  }

}
