package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.UnresolvedSpecificationException;
import java.util.Objects;

public class NodesByGraphUri implements Specification<NodeId, Node> {

  private String graphUri;

  public NodesByGraphUri(String graphUri) {
    this.graphUri = graphUri;
  }

  public String getGraphUri() {
    return graphUri;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    throw new UnresolvedSpecificationException("Can't specify directly with graph uri");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByGraphUri that = (NodesByGraphUri) o;
    return Objects.equals(graphUri, that.graphUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphUri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("graphUri", graphUri)
        .toString();
  }

}
