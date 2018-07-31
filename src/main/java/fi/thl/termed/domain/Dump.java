package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.SetUtils.toImmutableSet;
import static fi.thl.termed.util.collect.StreamUtils.nullToEmpty;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;

import fi.thl.termed.util.collect.Identifiable;
import java.util.List;
import java.util.stream.Stream;

public final class Dump implements Identifiable<DumpId> {

  private final DumpId identifier;

  private final Stream<Graph> graphs;
  private final Stream<Type> types;
  private final Stream<Node> nodes;

  public Dump(Stream<Graph> graphs, Stream<Type> types, Stream<Node> nodes) {
    List<Graph> graphList = toListAndClose(graphs);

    this.identifier = new DumpId(graphList.stream()
        .map(Graph::identifier)
        .collect(toImmutableSet()));

    this.graphs = graphList.stream();
    this.types = types;
    this.nodes = nodes;
  }

  @Override
  public DumpId identifier() {
    return identifier;
  }

  public Stream<Graph> getGraphs() {
    return nullToEmpty(graphs);
  }

  public Stream<Type> getTypes() {
    return nullToEmpty(types);
  }

  public Stream<Node> getNodes() {
    return nullToEmpty(nodes);
  }

}
