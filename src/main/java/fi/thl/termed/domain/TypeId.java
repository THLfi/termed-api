package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TypeId implements Serializable {

  private final String id;

  private final GraphId graph;

  public TypeId(String id, UUID graphId) {
    this(id, new GraphId(graphId));
  }

  public TypeId(String id, GraphId graph) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.graph = checkNotNull(graph, "graph can't be null in %s", getClass());
  }

  public static TypeId of(String id, UUID graphId) {
    return of(id, GraphId.of(graphId));
  }

  public static TypeId of(String id, GraphId graphId) {
    return new TypeId(id, graphId);
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
