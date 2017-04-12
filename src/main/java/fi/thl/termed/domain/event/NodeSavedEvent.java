package fi.thl.termed.domain.event;

import fi.thl.termed.domain.NodeId;
import java.util.Date;

public class NodeSavedEvent extends NodeEvent {

  public NodeSavedEvent(String user, Date date, boolean async, NodeId node) {
    super(user, date, async, node);
  }

}
