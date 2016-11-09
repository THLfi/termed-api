package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.Optional;

import fi.thl.termed.util.collect.MultimapUtils;

public class TypeDto {

  private String id;
  private String uri;

  private GraphDto graph;

  private Multimap<String, LangValue> properties;

  // provided as a reference for serializers that use fully qualified uris as attribute keys
  private transient Map<String, String> textAttributeUriIndex;
  private transient Map<String, String> referenceAttributeUriIndex;

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

  public Optional<String> getTextAttributeUri(String id) {
    return Optional.of(textAttributeUriIndex.get(id));
  }

  public void setTextAttributeUriIndex(Map<String, String> textAttributeUriIndex) {
    this.textAttributeUriIndex = textAttributeUriIndex;
  }

  public Optional<String> getReferenceAttributeUri(String id) {
    return Optional.of(referenceAttributeUriIndex.get(id));
  }

  public void setReferenceAttributeUriIndex(Map<String, String> referenceAttributeUriIndex) {
    this.referenceAttributeUriIndex = referenceAttributeUriIndex;
  }

}
