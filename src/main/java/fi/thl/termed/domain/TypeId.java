package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class TypeId implements Serializable {

  private final String id;

  private final GraphId graph;

  public TypeId(Node node) {
    this(node.getType());
  }

  public TypeId(NodeId nodeId) {
    this(nodeId.getType());
  }

  public TypeId(TypeId typeId) {
    this(typeId.getId(), typeId.getGraph());
  }

  public TypeId(Type cls) {
    this(cls.getId(), cls.getGraph());
  }

  public TypeId(String id, UUID graphId) {
    this(id, new GraphId(graphId));
  }

  public TypeId(String id, GraphId graph) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.graph = checkNotNull(graph, "graph can't be null in %s", getClass());
  }

  public String getId() {
    return id;
  }

  public GraphId getGraph() {
    return graph;
  }

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TypeId typeId = (TypeId) o;
    return Objects.equals(id, typeId.id) &&
           Objects.equals(graph, typeId.graph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, graph);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("graph", graph)
        .toString();
  }

}
