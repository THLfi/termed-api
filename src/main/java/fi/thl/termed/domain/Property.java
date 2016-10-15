package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapUtils;

public class Property {

  private String id;

  private String uri;

  private Integer index;

  private Multimap<String, LangValue> properties;

  public Property(String id) {
    this.id = id;
  }

  public Property(String id, String uri, Integer index) {
    this.id = id;
    this.uri = uri;
    this.index = index;
  }

  public Property(Property property) {
    this.id = property.id;
    this.uri = property.uri;
    this.index = property.index;
    this.properties = property.properties;
  }

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

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
        .add("properties", properties)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Property property = (Property) o;
    return Objects.equal(id, property.id) &&
           Objects.equal(uri, property.uri) &&
           Objects.equal(index, property.index) &&
           Objects.equal(properties, property.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, uri, index, properties);
  }

}
