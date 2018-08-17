package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import fi.thl.termed.util.UUIDs;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class GraphId implements Serializable {

  private final UUID id;

  public GraphId(Graph graph) {
    this(graph.getId());
  }

  public GraphId(UUID id) {
    this.id = requireNonNull(id, () -> "id can't be null in " + getClass());
  }

  public static GraphId fromUuidString(String id) {
    return of(UUIDs.fromString(id));
  }

  public static GraphId of(Graph graph) {
    return of(graph.getId());
  }

  public static GraphId of(UUID id) {
    return new GraphId(id);
  }

  public static GraphId random() {
    return new GraphId(UUID.randomUUID());
  }

  public UUID getId() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphId graphId = (GraphId) o;
    return Objects.equals(id, graphId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
