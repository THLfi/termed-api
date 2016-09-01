package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.UUID;

import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapUtils;

public class ReferenceAttribute implements PropertyEntity {

  private String id;
  private String uri;

  private Integer index;

  private Class domain;
  private Class range;

  private Multimap<String, Permission> permissions;
  private Multimap<String, LangValue> properties;

  public ReferenceAttribute(String id, String uri) {
    this.id = id;
    this.uri = uri;
  }

  public ReferenceAttribute(Class range, String id, String uri) {
    this.id = id;
    this.uri = uri;
    this.range = range;
  }

  public ReferenceAttribute(Class range, String id) {
    this.id = id;
    this.range = range;
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

  public void setDomain(Class domain) {
    this.domain = domain;
  }

  public UUID getDomainSchemeId() {
    return domain != null ? domain.getSchemeId() : null;
  }

  public String getDomainId() {
    return domain != null ? domain.getId() : null;
  }

  public Class getRange() {
    return range;
  }

  public void setRange(Class range) {
    this.range = range;
  }

  public UUID getRangeSchemeId() {
    return range != null ? range.getSchemeId() : null;
  }

  public String getRangeId() {
    return range != null ? range.getId() : null;
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
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
        .add("domainId", getDomainId())
        .add("rangeId", getRangeId())
        .add("permissions", permissions)
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
    ReferenceAttribute that = (ReferenceAttribute) o;
    return Objects.equal(id, that.id) &&
           Objects.equal(uri, that.uri) &&
           Objects.equal(index, that.index) &&
           Objects.equal(getDomainId(), that.getDomainId()) &&
           Objects.equal(getRangeId(), that.getRangeId()) &&
           Objects.equal(permissions, that.permissions) &&
           Objects.equal(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, uri, index, getDomainId(), getRangeId(), permissions, properties);
  }

}
