package fi.thl.termed.exchange.tree;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.Tree;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * Recursively transforms ResourceTree to JsTree. Assumes that ResourceTree is really a tree, i.e.
 * does not contain loops.
 */
public class ResourceTreeToJsTree implements Function<Tree<Resource>, JsTree> {

  private Predicate<ResourceId> addChildrenPredicate;

  private Predicate<ResourceId> selectedPredicate;

  private String labelAttributeId;

  private String lang;

  private Function<Resource, UUID> getResourceId = new Function<Resource, UUID>() {
    public UUID apply(Resource resource) {
      return resource.getId();
    }
  };

  public ResourceTreeToJsTree(Predicate<ResourceId> addChildrenPredicate,
                              Predicate<ResourceId> selectedPredicate,
                              String labelAttributeId, String lang) {
    this.addChildrenPredicate = addChildrenPredicate;
    this.selectedPredicate = selectedPredicate;
    this.labelAttributeId = labelAttributeId;
    this.lang = lang;
  }

  @Override
  public JsTree apply(Tree<Resource> resourceTree) {
    JsTree jsTree = new JsTree();
    Resource resource = resourceTree.getData();
    ResourceId resourceId = new ResourceId(resource);

    jsTree.setId(DigestUtils.sha1Hex(
        Joiner.on('.').join(resourceTree.getPath().stream().map(Resource::getId)
                                .collect(Collectors.toList()))));
    jsTree.setIcon(false);
    jsTree.setText(htmlEscape(getLocalizedLabel(resource)) +
                   smallMuted(htmlEscape(getCode(resource))));

    jsTree.setState(ImmutableMap.of("opened", addChildrenPredicate.apply(resourceId),
                                    "selected", selectedPredicate.apply(resourceId)));

    String conceptUrl = "/schemes/" + resource.getSchemeId() +
                        "/classes/" + resource.getTypeId() +
                        "/resources/" + resource.getId();

    jsTree.setLinkElementAttributes(ImmutableMap.of("href", conceptUrl));
    jsTree.setListElementAttributes(
        ImmutableMap.of("resourceSchemeId", resource.getSchemeId().toString(),
                        "resourceTypeId", resource.getTypeId(),
                        "resourceId", resource.getId().toString()));

    List<Tree<Resource>> children = resourceTree.getChildren();

    if (children.isEmpty()) {
      jsTree.setChildren(false);
    } else if (addChildrenPredicate.apply(resourceId)) {
      jsTree.setChildren(children.stream().map(this).collect(Collectors.toList()));
    } else {
      jsTree.setChildren(true);
    }

    return jsTree;
  }

  private String getLocalizedLabel(Resource resource) {
    Collection<StrictLangValue> langValues = resource.getProperties().get(labelAttributeId);

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

  private String getCode(Resource resource) {
    if (resource.getCode() != null) {
      return resource.getCode();
    }
    if (resource.getUri() != null) {
      return getLocalName(resource.getUri());
    }
    return "";
  }

  private String getLocalName(String uri) {
    int i = uri.lastIndexOf("#");
    i = i == -1 ? uri.lastIndexOf("/") : -1;
    return uri.substring(i + 1);
  }

}
