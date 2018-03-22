package fi.thl.termed.domain;

import static java.util.stream.Stream.empty;

import java.util.stream.Stream;

public class Dump {

  private final Stream<Graph> graphs;
  private final Stream<Type> types;
  private final Stream<Node> nodes;

  public Dump(
      Stream<Graph> graphs,
      Stream<Type> types,
      Stream<Node> nodes) {
    this.graphs = graphs;
    this.types = types;
    this.nodes = nodes;
  }

  public Stream<Graph> getGraphs() {
    return graphs != null ? graphs : empty();
  }

  public Stream<Type> getTypes() {
    return types != null ? types : empty();
  }

  public Stream<Node> getNodes() {
    return nodes != null ? nodes : empty();
  }

}
