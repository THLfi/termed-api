package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.util.collect.MultimapUtils;

public class Resource implements Auditable, Identifiable<ResourceId> {

  private UUID id;
  private String code;
  private String uri;

  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  private ClassId type;

  private Multimap<String, Permission> permissions;
  private Multimap<String, StrictLangValue> properties;
  private Multimap<String, ResourceId> references;
  private Multimap<String, ResourceId> referrers;

  public Resource() {
  }

  public Resource(UUID id) {
    this.id = id;
  }

  public Resource(ResourceId resourceId) {
    this.id = resourceId.getId();
    this.type = resourceId.getType();
  }

  public Resource(Resource resource) {
    this.id = resource.id;
    this.code = resource.code;
    this.uri = resource.uri;
    this.createdBy = resource.createdBy;
    this.createdDate = resource.createdDate;
    this.lastModifiedBy = resource.lastModifiedBy;
    this.lastModifiedDate = resource.lastModifiedDate;
    this.type = resource.type;
    this.permissions = resource.permissions;
    this.properties = resource.properties;
    this.references = resource.references;
    this.referrers = resource.referrers;
  }

  @Override
  public ResourceId identifier() {
    return new ResourceId(this);
  }

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

  public ClassId getType() {
    return type;
  }

  public void setType(ClassId type) {
    this.type = type;
  }

  public UUID getTypeSchemeId() {
    return type != null ? type.getSchemeId() : null;
  }

  public String getTypeId() {
    return type != null ? type.getId() : null;
  }

  public Multimap<String, Permission> getPermissions() {
    return MultimapUtils.nullToEmpty(permissions);
  }

  public void setPermissions(Multimap<String, Permission> permissions) {
    this.permissions = permissions;
  }

  public Multimap<String, StrictLangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, StrictLangValue> properties) {
    this.properties = properties;
  }

  public void addProperty(String attributeId, String lang, String value) {
    addProperty(attributeId, new StrictLangValue(lang, value));
  }

  public void addProperty(String attributeId, String lang, String value, String regex) {
    addProperty(attributeId, new StrictLangValue(lang, value, regex));
  }

  public void addProperty(String attributeId, StrictLangValue langValue) {
    if (properties == null) {
      properties = LinkedHashMultimap.create();
    }

    properties.put(attributeId, langValue);
  }

  public Multimap<String, ResourceId> getReferences() {
    return MultimapUtils.nullToEmpty(references);
  }

  public void setReferences(Multimap<String, ResourceId> references) {
    this.references = references;
  }

  public void addReferences(String attributeId, List<ResourceId> references) {
    for (ResourceId reference : references) {
      addReference(attributeId, reference);
    }
  }

  public void addReference(String attributeId, ResourceId reference) {
    if (references == null) {
      references = LinkedHashMultimap.create();
    }

    references.put(attributeId, reference);
  }

  public Multimap<String, ResourceId> getReferrers() {
    return MultimapUtils.nullToEmpty(referrers);
  }

  public void setReferrers(Multimap<String, ResourceId> referrers) {
    this.referrers = referrers;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("code", code)
        .add("uri", uri)
        .add("createdBy", createdBy)
        .add("createdDate", createdDate)
        .add("lastModifiedBy", lastModifiedBy)
        .add("lastModifiedDate", lastModifiedDate)
        .add("type", type)
        .add("permissions", permissions)
        .add("properties", properties)
        .add("references", references)
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
    Resource resource = (Resource) o;
    return Objects.equals(id, resource.id) &&
           Objects.equals(code, resource.code) &&
           Objects.equals(uri, resource.uri) &&
           Objects.equals(createdBy, resource.createdBy) &&
           Objects.equals(createdDate, resource.createdDate) &&
           Objects.equals(lastModifiedBy, resource.lastModifiedBy) &&
           Objects.equals(lastModifiedDate, resource.lastModifiedDate) &&
           Objects.equals(type, resource.type) &&
           Objects.equals(permissions, resource.permissions) &&
           Objects.equals(properties, resource.properties) &&
           Objects.equals(references, resource.references);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id,
                        code,
                        uri,
                        createdBy,
                        createdDate,
                        lastModifiedBy,
                        lastModifiedDate,
                        type,
                        permissions,
                        properties,
                        references);
  }

}
