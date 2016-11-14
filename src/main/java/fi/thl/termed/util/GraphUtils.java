package fi.thl.termed.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import fi.thl.termed.util.collect.ListUtils;

public final class GraphUtils {

  private GraphUtils() {
  }

  /**
   * Find root nodes reachable from a node.
   */
  public static <T> List<T> findRoots(T node, Function<T, List<T>> neighbourFunction) {
    return findRoots(collectPaths(node, neighbourFunction));

  }

  public static <T> List<T> findRoots(List<List<T>> paths) {
    Set<T> roots = Sets.newHashSet();

    for (List<T> path : paths) {
      roots.add(path.get(0));
    }

    return Lists.newArrayList(roots);
  }


  /**
   * Collect all reachable nodes from a node using neighbour functions. Flattening results from
   * collectPaths yields the same results but is slightly less efficient.
   */
  public static <T> Set<T> collectNodes(T root,
                                        Function<T, List<T>> neighbourFunction) {
    Set<T> results = Sets.newLinkedHashSet();
    collectNodes(root, neighbourFunction, results);
    return results;
  }

  private static <T> void collectNodes(T node,
                                       Function<T, List<T>> neighbourFunction,
                                       Set<T> results) {
    if (!results.contains(node)) {
      results.add(node);
      for (T neighbour : ListUtils.nullToEmpty(neighbourFunction.apply(node))) {
        collectNodes(neighbour, neighbourFunction, results);
      }
    }
  }

  /**
   * Enumerate all paths leading to a node using neighbour function.
   */
  public static <T> List<List<T>> collectPaths(T node,
                                               Function<T, List<T>> neighbourFunction) {
    List<List<T>> paths = Lists.newArrayList();
    collectPaths(node, neighbourFunction, Sets.<T>newLinkedHashSet(), paths);
    return paths;
  }

  private static <T> void collectPaths(T node,
                                       Function<T, List<T>> neighbourFunction,
                                       Set<T> path,
                                       List<List<T>> results) {

    if (!path.contains(node)) {
      path.add(node);
    } else {
      results.add(toReversedList(path));
      return;
    }

    List<T> neighbours = ListUtils.nullToEmpty(neighbourFunction.apply(node));

    if (!neighbours.isEmpty()) {
      for (T neighbour : neighbours) {
        collectPaths(neighbour, neighbourFunction, Sets.newLinkedHashSet(path), results);
      }
    } else {
      results.add(toReversedList(path));
    }
  }

  private static <T> List<T> toReversedList(Set<T> nodes) {
    return Lists.reverse(Lists.newArrayList(nodes));
  }

  /**
   * Get graph as a tree. Tree returned here is lazy loaded meaning that neighbour function is
   * called when tree node's child list is first requested. Tree can handle loops the graph.
   */
  public static <T> Tree<T> toTree(T rootNode, Function<T, List<T>> neighbourFunction) {
    return new LazyTree<T>(rootNode, neighbourFunction);
  }

  /**
   * Pretty print tree. Useful e.g. for debugging.
   */
  public static <T> String prettyPrintTree(Tree<T> tree) {
    StringBuilder builder = new StringBuilder();
    prettyPrintTree("", tree, builder);
    return builder.toString();
  }

  private static <T> void prettyPrintTree(String indent, Tree<T> tree, StringBuilder builder) {
    builder.append(String.format("%s - %s\n", indent, tree.getData().toString()));
    for (Tree<T> child : tree.getChildren()) {
      prettyPrintTree(indent + "\t", child, builder);
    }
  }

  /**
   * Defined for convenience when a function parameter is required.
   */
  public static class ToTreeFunction<T> implements Function<T, Tree<T>> {

    private Function<T, List<T>> neighbourFunction;

    public ToTreeFunction(Function<T, List<T>> neighbourFunction) {
      this.neighbourFunction = neighbourFunction;
    }

    @Override
    public Tree<T> apply(T node) {
      return new LazyTree<T>(node, neighbourFunction);
    }
  }

}
