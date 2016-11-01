package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.util.collect.MultimapUtils;

public abstract class Attribute {

  private String id;
  private String uri;

  private Integer index;

  private TypeId domain;

  private Multimap<String, Permission> permissions;
  private Multimap<String, LangValue> properties;

  public Attribute(String id, TypeId domain) {
    this.id = id;
    this.domain = domain;
  }

  public Attribute(String id, String uri, TypeId domain) {
    this.id = id;
    this.uri = uri;
    this.domain = domain;
  }

  public Attribute(Attribute attribute) {
    this.id = attribute.id;
    this.uri = attribute.uri;
    this.index = attribute.index;
    this.domain = attribute.domain;
    this.permissions = attribute.permissions;
    this.properties = attribute.properties;
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

  public TypeId getDomain() {
    return domain;
  }

  public void setDomain(TypeId domain) {
    this.domain = domain;
  }

  public UUID getDomainGraphId() {
    return domain != null ? domain.getGraphId() : null;
  }

  public String getDomainId() {
    return domain != null ? domain.getId() : null;
  }

  public Multimap<String, Permission> getPermissions() {
    return MultimapUtils.nullToEmpty(permissions);
  }

  public void setPermissions(Multimap<String, Permission> permissions) {
    this.permissions = permissions;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Attribute attribute = (Attribute) o;
    return Objects.equals(id, attribute.id) &&
           Objects.equals(uri, attribute.uri) &&
           Objects.equals(index, attribute.index) &&
           Objects.equals(domain, attribute.domain) &&
           Objects.equals(permissions, attribute.permissions) &&
           Objects.equals(properties, attribute.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uri, index, domain, permissions, properties);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  public MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
        .add("domain", domain)
        .add("permissions", permissions)
        .add("properties", properties);
  }

}
