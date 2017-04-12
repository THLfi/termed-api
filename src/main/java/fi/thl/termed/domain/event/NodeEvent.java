package fi.thl.termed.domain.event;

import fi.thl.termed.domain.NodeId;
import java.util.Date;
import java.util.Objects;

public abstract class NodeEvent implements TermedEvent {

  private String user;
  private Date date;
  private NodeId node;
  private boolean sync;

  NodeEvent(String user, Date date, boolean sync, NodeId node) {
    this.user = user;
    this.date = date;
    this.sync = sync;
    this.node = node;
  }

  public String getUser() {
    return user;
  }

  public Date getDate() {
    return date;
  }

  public boolean isSync() {
    return sync;
  }

  public NodeId getNode() {
    return node;
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
        Objects.equals(node, nodeEvent.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, date, sync, node);
  }

}
