package fi.thl.termed.domain;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.MultimapUtils;
import fi.thl.termed.util.StrictLangValue;

public class Resource {

  private UUID id;
  private String code;
  private String uri;

  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  private Scheme scheme;
  private Class type;

  private Multimap<String, StrictLangValue> properties;
  private Multimap<String, Resource> references;
  private Multimap<String, Resource> referrers;

  public Resource() {
  }

  public Resource(UUID id) {
    this.id = id;
  }

  public Resource(ResourceId resourceId) {
    this.scheme = new Scheme(resourceId.getSchemeId());
    this.type = new Class(resourceId.getTypeId());
    this.id = resourceId.getId();
  }

  public Resource(Scheme scheme, Class type, UUID id) {
    this.scheme = scheme;
    this.type = type;
    this.id = id;
  }

  public Resource(Resource resource) {
    this.id = resource.id;
    this.code = resource.code;
    this.uri = resource.uri;
    this.createdBy = resource.createdBy;
    this.createdDate = resource.createdDate;
    this.lastModifiedBy = resource.lastModifiedBy;
    this.lastModifiedDate = resource.lastModifiedDate;
    this.scheme = resource.scheme;
    this.type = resource.type;
    this.properties = resource.properties;
    this.references = resource.references;
    this.referrers = resource.referrers;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void ensureId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
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

  public Scheme getScheme() {
    return scheme;
  }

  public void setScheme(Scheme scheme) {
    this.scheme = scheme;
  }

  public UUID getSchemeId() {
    return scheme != null ? scheme.getId() : null;
  }

  public Class getType() {
    return type;
  }

  public void setType(Class type) {
    this.type = type;
  }

  public String getTypeId() {
    return type != null ? type.getId() : null;
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

  public Multimap<String, Resource> getReferences() {
    return MultimapUtils.nullToEmpty(references);
  }

  public void setReferences(Multimap<String, Resource> references) {
    this.references = references;
  }

  public Multimap<String, ResourceId> getReferenceIds() {
    return Multimaps.transformValues(getReferences(), new Function<Resource, ResourceId>() {
      public ResourceId apply(Resource r) {
        return new ResourceId(r.getSchemeId(), r.getTypeId(), r.getId());
      }
    });
  }

  public void addReferences(String attributeId, List<Resource> references) {
    for (Resource reference : references) {
      addReference(attributeId, reference);
    }
  }

  public void addReference(String attributeId, Resource reference) {
    if (references == null) {
      references = LinkedHashMultimap.create();
    }

    references.put(attributeId, reference);
  }

  public Multimap<String, Resource> getReferrers() {
    return MultimapUtils.nullToEmpty(referrers);
  }

  public void setReferrers(Multimap<String, Resource> referrers) {
    this.referrers = referrers;
  }

  public Multimap<String, ResourceId> getReferrerIds() {
    return Multimaps.transformValues(getReferrers(), new Function<Resource, ResourceId>() {
      public ResourceId apply(Resource r) {
        return new ResourceId(r.getSchemeId(), r.getTypeId(), r.getId());
      }
    });
  }

  public void addReferrer(String attributeId, Resource referrer) {
    if (referrers == null) {
      referrers = LinkedHashMultimap.create();
    }

    referrers.put(attributeId, referrer);
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
        .add("schemeId", getSchemeId())
        .add("typeId", getTypeId())
        .add("properties", properties)
        .add("references", getReferenceIds())
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
    return Objects.equal(id, resource.id) &&
           Objects.equal(code, resource.code) &&
           Objects.equal(uri, resource.uri) &&
           Objects.equal(createdBy, resource.createdBy) &&
           Objects.equal(createdDate, resource.createdDate) &&
           Objects.equal(lastModifiedBy, resource.lastModifiedBy) &&
           Objects.equal(lastModifiedDate, resource.lastModifiedDate) &&
           Objects.equal(getSchemeId(), resource.getSchemeId()) &&
           Objects.equal(getTypeId(), resource.getTypeId()) &&
           Objects.equal(properties, resource.properties) &&
           Objects.equal(getReferenceIds(), resource.getReferenceIds());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id,
                            code,
                            uri,
                            createdBy,
                            createdDate,
                            lastModifiedBy,
                            lastModifiedDate,
                            getSchemeId(),
                            getTypeId(),
                            properties,
                            getReferenceIds());
  }

}
