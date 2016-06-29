package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.UUID;

import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapUtils;
import fi.thl.termed.util.RegularExpressions;

public class TextAttribute implements PropertyEntity {

  private String id;

  private String uri;

  private Integer index;

  private Class domain;

  private String regex;

  private Multimap<String, LangValue> properties;

  public TextAttribute(String id) {
    this.id = id;
  }

  public TextAttribute(String id, String uri) {
    this.id = id;
    this.uri = uri;
  }

  public TextAttribute(String id, String uri, String regex) {
    this.id = id;
    this.uri = uri;
    this.regex = regex;
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

  public String getRegex() {
    return MoreObjects.firstNonNull(regex, RegularExpressions.ALL);
  }

  public void setRegex(String regex) {
    this.regex = regex;
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
        .add("regex", regex)
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
    TextAttribute that = (TextAttribute) o;
    return Objects.equal(id, that.id) &&
           Objects.equal(uri, that.uri) &&
           Objects.equal(index, that.index) &&
           Objects.equal(getDomainId(), that.getDomainId()) &&
           Objects.equal(regex, that.regex) &&
           Objects.equal(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, uri, index, getDomainId(), regex, properties);
  }

}
