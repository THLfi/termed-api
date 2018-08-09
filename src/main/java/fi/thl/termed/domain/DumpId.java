package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;

public final class DumpId implements Serializable {

  private final ImmutableSet<GraphId> graphIds;

  public DumpId(GraphId... graphIds) {
    this(ImmutableSet.copyOf(graphIds));
  }

  public DumpId(Iterable<GraphId> graphIds) {
    this.graphIds = ImmutableSet.copyOf(
        requireNonNull(graphIds, () -> "id can't be null in " + getClass()));
  }

  public ImmutableSet<GraphId> getGraphIds() {
    return graphIds;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("graphIds", graphIds).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DumpId dumpId = (DumpId) o;
    return Objects.equals(graphIds, dumpId.graphIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphIds);
  }

}
