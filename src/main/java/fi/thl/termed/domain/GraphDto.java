package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import java.util.UUID;

import fi.thl.termed.util.collect.MultimapUtils;

public class GraphDto {

  private UUID id;
  private String code;
  private String uri;

  private Multimap<String, LangValue> properties;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

}
