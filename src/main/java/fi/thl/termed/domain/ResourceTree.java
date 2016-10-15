package fi.thl.termed.domain;

import java.util.List;
import java.util.UUID;

public class ResourceTree extends Resource {

  private List<UUID> path;

  private List<ResourceTree> children;

  public ResourceTree(Resource resource) {
    super(resource);
  }

  public List<UUID> getPath() {
    return path;
  }

  public void setPath(List<UUID> path) {
    this.path = path;
  }

  public List<ResourceTree> getChildren() {
    return children;
  }

  public void setChildren(List<ResourceTree> children) {
    this.children = children;
  }

}
