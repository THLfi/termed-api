package fi.thl.termed.util;

import java.util.List;
import java.util.Set;

public interface Tree<T> {

  /**
   * @return data contained in this tree node.
   */
  T getData();

  /**
   * Get path from root to this node. Path can't contain duplicates as tree can't have loops. Path
   * is expected to be in proper order from root to this node (returned collections is implemented
   * as e.g. LinkedHashSet).
   *
   * @return path from root to this node.
   */
  Set<T> getPath();

  /**
   * @return list of child trees.
   */
  List<Tree<T>> getChildren();

}
