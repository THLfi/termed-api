package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.UUID;

import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapUtils;

public abstract class Attribute {

  private String id;
  private String uri;

  private Integer index;

  private Class domain;

  private Multimap<String, Permission> permissions;
  private Multimap<String, LangValue> properties;

  public Attribute(Class domain, String id) {
    this.domain = domain;
    this.id = id;
  }

  public Attribute(Class domain, String id, String uri) {
    this.domain = domain;
    this.id = id;
    this.uri = uri;
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

  public Class getDomain() {
    return domain;
  }

  public UUID getDomainSchemeId() {
    return domain != null ? domain.getSchemeId() : null;
  }

  public String getDomainId() {
    return domain != null ? domain.getId() : null;
  }

  public void setDomain(Class domain) {
    this.domain = domain;
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
    return Objects.equal(id, attribute.id) &&
           Objects.equal(uri, attribute.uri) &&
           Objects.equal(index, attribute.index) &&
           Objects.equal(domain, attribute.domain) &&
           Objects.equal(permissions, attribute.permissions) &&
           Objects.equal(properties, attribute.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, uri, index, domain, permissions, properties);
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
