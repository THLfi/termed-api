package fi.thl.termed.util;

import java.util.function.Function;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import fi.thl.termed.util.collect.ListUtils;

/**
 * Lazy loading implementation of a Tree. Checks for loops against the current path to avoid
 * infinite cycles.
 */
public class LazyTree<T> implements Tree<T> {

  private T data;
  private Function<T, List<T>> neighbourFunction;

  private Set<T> path;
  private List<Tree<T>> children;

  /**
   * Construct a new tree based root data and a function for loading children.
   */
  @SuppressWarnings("unchecked")
  public LazyTree(T data, java.util.function.Function<T, List<T>> neighbourFunction) {
    this(data, Sets.newLinkedHashSet(Lists.newArrayList(data)), neighbourFunction);
  }

  private LazyTree(T data, Set<T> path, java.util.function.Function<T, List<T>> neighbourFunction) {
    this.data = data;
    this.path = path;
    this.neighbourFunction = neighbourFunction;
  }

  @Override
  public T getData() {
    return data;
  }

  @Override
  public Set<T> getPath() {
    return path;
  }

  @Override
  public List<Tree<T>> getChildren() {
    if (children == null) {
      loadChildren();
    }
    return children;
  }

  private void loadChildren() {
    List<Tree<T>> children = Lists.newArrayList();

    for (T child : ListUtils.nullToEmpty(neighbourFunction.apply(data))) {
      // check path to avoid loops
      if (!path.contains(child)) {
        Set<T> childPath = Sets.newLinkedHashSet(path);
        childPath.add(child);

        children.add(new LazyTree<T>(child, childPath, neighbourFunction));
      }
    }

    this.children = children;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("path", path)
        .add("data", data)
        .toString();
  }

  /**
   * Identity is based on path + data as same data can appear in many branches of a tree.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LazyTree<?> lazyTree = (LazyTree<?>) o;
    return Objects.equals(path, lazyTree.path) &&
           Objects.equals(data, lazyTree.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, data);
  }

}
