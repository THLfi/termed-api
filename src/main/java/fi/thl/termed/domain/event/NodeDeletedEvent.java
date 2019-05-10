package fi.thl.termed.domain.event;

import fi.thl.termed.domain.NodeId;
import java.time.LocalDateTime;
import java.util.List;

public class NodeDeletedEvent extends NodeEvent {

  public NodeDeletedEvent(String user, LocalDateTime date, boolean async, List<NodeId> nodes) {
    super(user, date, async, nodes);
  }

}
