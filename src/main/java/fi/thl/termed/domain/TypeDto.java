package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import fi.thl.termed.util.collect.MultimapUtils;

public class TypeDto {

  private String id;
  private String uri;

  private GraphDto graph;

  private Multimap<String, LangValue> properties;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public GraphDto getGraph() {
    return graph;
  }

  public void setGraph(GraphDto graph) {
    this.graph = graph;
  }

  public String getGraphUri() {
    return graph != null ? graph.getUri() : null;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

}
