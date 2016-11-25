package fi.thl.termed.domain;

import java.util.List;

public class Dump {

  private List<Graph> graphs;
  private List<Type> types;
  private List<Node> nodes;

  public List<Graph> getGraphs() {
    return graphs;
  }

  public void setGraphs(List<Graph> graphs) {
    this.graphs = graphs;
  }

  public List<Type> getTypes() {
    return types;
  }

  public void setTypes(List<Type> types) {
    this.types = types;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }

}
