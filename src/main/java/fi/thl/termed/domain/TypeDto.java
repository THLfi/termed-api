package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.UUID;

import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.collect.MultimapUtils;

public class TypeDto {

  private String id;
  private String uri;

  private GraphDto graph;

  private Multimap<String, LangValue> properties;

  private transient Map<String, String> textAttributes;
  private transient Map<String, String> referenceAttributes;

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

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

  public Map<String, String> getTextAttributes() {
    return MapUtils.nullToEmpty(textAttributes);
  }

  public void setTextAttributes(Map<String, String> textAttributes) {
    this.textAttributes = textAttributes;
  }

  public Map<String, String> getReferenceAttributes() {
    return MapUtils.nullToEmpty(referenceAttributes);
  }

  public void setReferenceAttributes(Map<String, String> referenceAttributes) {
    this.referenceAttributes = referenceAttributes;
  }

}
