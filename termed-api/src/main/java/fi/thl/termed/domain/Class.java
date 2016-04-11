package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.ListUtils;
import fi.thl.termed.util.MultimapUtils;

public class Class implements PropertyEntity {

  private String id;

  private String uri;

  private Integer index;

  private Scheme scheme;

  private Multimap<String, LangValue> properties;

  private List<TextAttribute> textAttributes;

  private List<ReferenceAttribute> referenceAttributes;

  public Class(String id) {
    this.id = id;
  }

  public Class(String id, String uri) {
    this.id = id;
    this.uri = uri;
  }

  public Class(String id, Multimap<String, LangValue> properties) {
    this.id = id;
    this.properties = properties;
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

  public Scheme getScheme() {
    return scheme;
  }

  public void setScheme(Scheme scheme) {
    this.scheme = scheme;
  }

  public UUID getSchemeId() {
    return scheme != null ? scheme.getId() : null;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

  public List<TextAttribute> getTextAttributes() {
    return ListUtils.nullToEmpty(textAttributes);
  }

  public void setTextAttributes(List<TextAttribute> textAttributes) {
    this.textAttributes = textAttributes;
  }

  public List<ReferenceAttribute> getReferenceAttributes() {
    return ListUtils.nullToEmpty(referenceAttributes);
  }

  public void setReferenceAttributes(List<ReferenceAttribute> referenceAttributes) {
    this.referenceAttributes = referenceAttributes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
        .add("schemeId", getSchemeId())
        .add("properties", properties)
        .add("textAttributes", textAttributes)
        .add("referenceAttributes", referenceAttributes)
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
    Class cls = (Class) o;
    return Objects.equal(id, cls.id) &&
           Objects.equal(uri, cls.uri) &&
           Objects.equal(index, cls.index) &&
           Objects.equal(getSchemeId(), cls.getSchemeId()) &&
           Objects.equal(properties, cls.properties) &&
           Objects.equal(textAttributes, cls.textAttributes) &&
           Objects.equal(referenceAttributes, cls.referenceAttributes);
  }

  @Override
  public int hashCode() {
    return Objects
        .hashCode(id, uri, index, getSchemeId(), properties, textAttributes, referenceAttributes);
  }

}
