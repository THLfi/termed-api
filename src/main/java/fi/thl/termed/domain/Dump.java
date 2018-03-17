package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.ListUtils.nullToEmpty;

import com.google.common.collect.ImmutableList;

public class Dump {

  private final ImmutableList<Graph> graphs;
  private final ImmutableList<Type> types;
  private final ImmutableList<Node> nodes;

  public Dump(
      ImmutableList<Graph> graphs,
      ImmutableList<Type> types,
      ImmutableList<Node> nodes) {
    this.graphs = graphs;
    this.types = types;
    this.nodes = nodes;
  }

  public ImmutableList<Graph> getGraphs() {
    return nullToEmpty(graphs);
  }

  public ImmutableList<Type> getTypes() {
    return nullToEmpty(types);
  }

  public ImmutableList<Node> getNodes() {
    return nullToEmpty(nodes);
  }

}
