package fi.thl.termed.domain.event;

import fi.thl.termed.domain.NodeId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public abstract class NodeEvent implements TermedEvent {

  private String user;
  private LocalDateTime date;
  private List<NodeId> nodes;
  private boolean sync;

  NodeEvent(String user, LocalDateTime date, boolean sync, List<NodeId> nodes) {
    this.user = user;
    this.date = date;
    this.sync = sync;
    this.nodes = nodes;
  }

  public String getUser() {
    return user;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public boolean isSync() {
    return sync;
  }

  public List<NodeId> getNodes() {
    return nodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeEvent nodeEvent = (NodeEvent) o;
    return sync == nodeEvent.sync &&
        Objects.equals(user, nodeEvent.user) &&
        Objects.equals(date, nodeEvent.date) &&
        Objects.equals(nodes, nodeEvent.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, date, nodes, sync);
  }

}
