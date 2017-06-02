package fi.thl.termed.domain.event;

import fi.thl.termed.domain.NodeId;
import java.util.Date;
import java.util.List;

public class NodeSavedEvent extends NodeEvent {

  public NodeSavedEvent(String user, Date date, boolean async, List<NodeId> nodes) {
    super(user, date, async, nodes);
  }

}
