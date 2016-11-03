package fi.thl.termed.web.external.node.jstree;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.Tree;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * Recursively transforms node tree to JsTree. Assumes that node tree is really a tree, i.e. does
 * not contain loops.
 */
public class NodeTreeToJsTree implements Function<Tree<Node>, JsTree> {

  private Predicate<NodeId> addChildrenPredicate;

  private Predicate<NodeId> selectedPredicate;

  private String labelAttributeId;

  private String lang;

  public NodeTreeToJsTree(Predicate<NodeId> addChildrenPredicate,
                          Predicate<NodeId> selectedPredicate,
                          String labelAttributeId, String lang) {
    this.addChildrenPredicate = addChildrenPredicate;
    this.selectedPredicate = selectedPredicate;
    this.labelAttributeId = labelAttributeId;
    this.lang = lang;
  }

  @Override
  public JsTree apply(Tree<Node> nodeTree) {
    JsTree jsTree = new JsTree();
    Node node = nodeTree.getData();
    NodeId nodeId = new NodeId(node);

    jsTree.setId(DigestUtils.sha1Hex(
        Joiner.on('.').join(nodeTree.getPath().stream().map(Node::getId)
                                .collect(Collectors.toList()))));
    jsTree.setIcon(false);
    jsTree.setText(htmlEscape(getLocalizedLabel(node)) +
                   smallMuted(htmlEscape(getCode(node))));

    jsTree.setState(ImmutableMap.of("opened", addChildrenPredicate.apply(nodeId),
                                    "selected", selectedPredicate.apply(nodeId)));

    String conceptUrl = "/graphs/" + node.getTypeGraphId() +
                        "/types/" + node.getTypeId() +
                        "/nodes/" + node.getId();

    jsTree.setLinkElementAttributes(ImmutableMap.of("href", conceptUrl));
    jsTree.setListElementAttributes(
        ImmutableMap.of("nodeGraphId", node.getTypeGraphId().toString(),
                        "nodeTypeId", node.getTypeId(),
                        "nodeId", node.getId().toString()));

    List<Tree<Node>> children = nodeTree.getChildren();

    if (children.isEmpty()) {
      jsTree.setChildren(false);
    } else if (addChildrenPredicate.apply(nodeId)) {
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

  private String smallMuted(String text) {
    return " <small class='text-muted'>" + text + "</small>";
  }

  private String getCode(Node node) {
    if (node.getCode() != null) {
      return node.getCode();
    }
    if (node.getUri() != null) {
      return getLocalName(node.getUri());
    }
    return "";
  }

  private String getLocalName(String uri) {
    int i = uri.lastIndexOf("#");
    i = i == -1 ? uri.lastIndexOf("/") : -1;
    return uri.substring(i + 1);
  }

}
