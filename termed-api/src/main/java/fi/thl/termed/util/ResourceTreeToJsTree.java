package fi.thl.termed.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Resource;

/**
 * Recursively transforms ResourceTree to JsTree. Assumes that ResourceTree is really a tree, i.e.
 * does not contain loops.
 */
public class ResourceTreeToJsTree implements Function<Tree<Resource>, JsTree> {

  private Predicate<UUID> addChildrenPredicate;

  private UUID selectedId;

  private String labelAttributeId;

  private String lang;

  private Function<Resource, UUID> getResourceId = new Function<Resource, UUID>() {
    public UUID apply(Resource resource) {
      return resource.getId();
    }
  };

  public ResourceTreeToJsTree(Predicate<UUID> addChildrenPredicate, UUID selectedId,
                              String labelAttributeId, String lang) {
    this.addChildrenPredicate = addChildrenPredicate;
    this.selectedId = selectedId;
    this.labelAttributeId = labelAttributeId;
    this.lang = lang;
  }

  @Override
  public JsTree apply(Tree<Resource> resourceTree) {
    JsTree jsTree = new JsTree();
    Resource resource = resourceTree.getData();

    jsTree.setId(DigestUtils.sha1Hex(
        Joiner.on('.').join(Iterables.transform(resourceTree.getPath(), getResourceId))));
    jsTree.setIcon(false);
    jsTree.setText(getLocalizedLabel(resource) + smallMuted(localName(resource)));

    jsTree.setState(ImmutableMap.of("opened", addChildrenPredicate.apply(resource.getId()),
                                    "selected", selectedId.equals(resource.getId())));

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
    } else if (addChildrenPredicate.apply(resource.getId())) {
      jsTree.setChildren(Lists.transform(children, this));
    } else {
      jsTree.setChildren(true);
    }

    return jsTree;
  }

  private String getLocalizedLabel(Resource resource) {
    List<String> localizedLabels = Lists.newArrayList();

    for (LangValue langValue : resource.getProperties().get(labelAttributeId)) {
      if (Objects.equal(lang, langValue.getLang())) {
        localizedLabels.add(langValue.getValue());
      }
    }

    return Joiner.on(", ").join(localizedLabels);
  }

  private String smallMuted(String text) {
    return " <small class='text-muted'>" + text + "</small>";
  }

  private String localName(Resource resource) {
    return resource.getUri() != null ? localName(resource.getUri()) : "";
  }

  private String localName(String uri) {
    int i = uri.lastIndexOf("#");
    i = i == -1 ? uri.lastIndexOf("/") : -1;
    return uri.substring(i + 1);
  }

}
