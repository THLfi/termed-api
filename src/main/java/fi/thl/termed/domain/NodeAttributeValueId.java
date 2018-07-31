package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;

public final class NodeAttributeValueId implements Serializable {

  private final NodeId nodeId;

  private final String attributeId;

  private final Integer index;

  public NodeAttributeValueId(NodeId nodeId,
      String attributeId,
      Integer index) {
    this.nodeId = checkNotNull(nodeId, "nodeId can't be null in %s", getClass());
    this.attributeId = checkNotNull(attributeId, "attributeId can't be null in s%", getClass());
    this.index = checkNotNull(index, "index can't be null in s%", getClass());
  }

  public NodeId getNodeId() {
    return nodeId;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public Integer getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeAttributeValueId that = (NodeAttributeValueId) o;
    return Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(attributeId, that.attributeId) &&
        Objects.equals(index, that.index);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId, attributeId, index);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("nodeId", nodeId)
        .add("attributeId", attributeId)
        .add("index", index)
        .toString();
  }

}
