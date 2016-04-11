package fi.thl.termed.service;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceKey;
import fi.thl.termed.domain.ResourceTree;
import fi.thl.termed.domain.User;

public interface ResourceGraphService {


  /**
   * Enumerate all paths from this resource
   */
  List<List<Resource>> findPaths(ResourceKey key, String attributeId, User user);

  List<List<Resource>> findReferrerPaths(ResourceKey key, String attributeId, User user);

  /**
   * Get all trees in scheme
   */
  List<ResourceTree> getTrees(UUID schemeId, String attributeId, User user);

  List<ResourceTree> getReferrerTrees(UUID schemeId, String attributeId, User user);

  /**
   * Get tree where root is given resource
   */
  ResourceTree getTree(ResourceKey key, String attributeId, User user);

  ResourceTree getReferrerTree(ResourceKey key, String attributeId, User user);

  /**
   * Get trees representing resource context
   */
  List<ResourceTree> getContextTrees(ResourceKey key, String attributeId, User user);

  List<ResourceTree> getContextReferrerTrees(ResourceKey key, String attributeId, User user);

  /**
   * Get trees representing resource context in jstree visualizer format
   */
  List<JsTree> getContextJsTrees(ResourceKey key, String attributeId, String lang, User user);

  List<JsTree> getContextJsReferrerTrees(ResourceKey key, String attrId, String lang, User user);

  /**
   * Get all trees in scheme in jstree visualizer format
   */
  List<JsTree> getJsTrees(UUID schemeId, String attributeId, String lang, User user);

  List<JsTree> getJsReferrerTrees(UUID schemeId, String attributeId, String lang, User user);

}
