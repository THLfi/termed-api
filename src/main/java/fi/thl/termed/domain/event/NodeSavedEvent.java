package fi.thl.termed.domain.event;

import com.google.common.base.MoreObjects;
import fi.thl.termed.domain.NodeId;
import java.util.Objects;

public class NodeSavedEvent {

  private NodeId node;
  private String username;

  public NodeSavedEvent(NodeId node, String username) {
    this.node = node;
    this.username = username;
  }

  public NodeId getNode() {
    return node;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeSavedEvent that = (NodeSavedEvent) o;
    return Objects.equals(node, that.node) && Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, username);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("node", node)
        .add("username", username)
        .toString();
  }

}
