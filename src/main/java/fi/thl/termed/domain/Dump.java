package fi.thl.termed.domain;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.SetUtils.toImmutableSet;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.collect.StreamUtils;
import java.util.stream.Stream;

public final class Dump implements Identifiable<DumpId> {

  private final ImmutableList<Graph> graphs;
  private final ImmutableList<Type> types;
  private final Stream<Node> nodes;

  public Dump(Stream<Graph> graphs, Stream<Type> types, Stream<Node> nodes) {
    this.graphs = requireNonNull(graphs).collect(toImmutableList());
    this.types = requireNonNull(types).collect(toImmutableList());
    this.nodes = requireNonNull(nodes);
  }

  @Override
  public DumpId identifier() {
    return new DumpId(getGraphs().map(GraphId::new).collect(toImmutableSet()));
  }

  public Stream<Graph> getGraphs() {
    return ListUtils.nullToEmpty(graphs).stream();
  }

  public Stream<Type> getTypes() {
    return ListUtils.nullToEmpty(types).stream();
  }

  public Stream<Node> getNodes() {
    return StreamUtils.nullToEmpty(nodes);
  }

}
