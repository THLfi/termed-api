package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class GraphRole implements Serializable {

  private final GraphId graph;

  private final String role;

  public GraphRole(GraphId graph, String role) {
    this.graph = checkNotNull(graph, "graph can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
  }

  public GraphId getGraph() {
    return graph;
  }

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphRole that = (GraphRole) o;
    return Objects.equals(graph, that.graph) &&
           Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("graph", graph)
        .add("role", role)
        .toString();
  }

}
