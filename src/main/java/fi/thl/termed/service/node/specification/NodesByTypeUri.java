package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.UnresolvedSpecificationException;
import java.util.Objects;

public class NodesByTypeUri implements Specification<NodeId, Node> {

  private String typeUri;

  public NodesByTypeUri(String typeUri) {
    this.typeUri = typeUri;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    throw new UnresolvedSpecificationException("Can't specify directly with type uri");
  }

  public String getTypeUri() {
    return typeUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByTypeUri that = (NodesByTypeUri) o;
    return Objects.equals(typeUri, that.typeUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeUri);
  }

  @Override
  public String toString() {
    return "type.uri = " + typeUri;
  }

}
