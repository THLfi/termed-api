package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import java.util.Date;
import java.util.UUID;

import fi.thl.termed.util.collect.MultimapUtils;

public class NodeDto {

  private UUID id;
  private String code;
  private String uri;

  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  private TypeDto type;

  private Multimap<String, LangValue> properties;
  private Multimap<String, NodeDto> references;
  private Multimap<String, NodeDto> referrers;

  // node dto may use attribute ids or uris as keys, this flag is for serializers
  private transient boolean uriAttributeKeys;

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

  public TypeDto getType() {
    return type;
  }

  public void setType(TypeDto type) {
    this.type = type;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
  }

  public Multimap<String, NodeDto> getReferences() {
    return MultimapUtils.nullToEmpty(references);
  }

  public void setReferences(Multimap<String, NodeDto> references) {
    this.references = references;
  }

  public Multimap<String, NodeDto> getReferrers() {
    return MultimapUtils.nullToEmpty(referrers);
  }

  public void setReferrers(Multimap<String, NodeDto> referrers) {
    this.referrers = referrers;
  }

  public boolean isUriAttributeKeys() {
    return uriAttributeKeys;
  }

  public void setUriAttributeKeys(boolean uriAttributeKeys) {
    this.uriAttributeKeys = uriAttributeKeys;
  }

}
