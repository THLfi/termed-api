package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.UnresolvedSpecificationException;
import java.util.Objects;

public class NodesByGraphCode implements Specification<NodeId, Node> {

  private String graphCode;

  public NodesByGraphCode(String graphCode) {
    Preconditions.checkArgument(graphCode.matches(RegularExpressions.CODE));
    this.graphCode = graphCode;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    throw new UnresolvedSpecificationException("Can't specify directly with graph code");
  }

  public String getGraphCode() {
    return graphCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByGraphCode that = (NodesByGraphCode) o;
    return Objects.equals(graphCode, that.graphCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphCode);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("graphCode", graphCode)
        .toString();
  }

}
