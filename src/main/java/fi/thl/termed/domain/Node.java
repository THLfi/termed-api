package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.collect.MultimapUtils;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Node implements Identifiable<NodeId> {

  private UUID id;
  private String code;
  private String uri;
  private Integer number;

  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  private TypeId type;

  private Multimap<String, StrictLangValue> properties;
  private Multimap<String, NodeId> references;
  private Multimap<String, NodeId> referrers;

  public Node() {
  }

  public Node(UUID id) {
    this.id = id;
  }

  public Node(NodeId nodeId) {
    this.id = nodeId.getId();
    this.type = nodeId.getType();
  }

  public Node(Node node) {
    this.id = node.id;
    this.code = node.code;
    this.uri = node.uri;
    this.number = node.number;
    this.createdBy = node.createdBy;
    this.createdDate = node.createdDate;
    this.lastModifiedBy = node.lastModifiedBy;
    this.lastModifiedDate = node.lastModifiedDate;
    this.type = node.type;
    this.properties = node.properties;
    this.references = node.references;
    this.referrers = node.referrers;
  }

  @Override
  public NodeId identifier() {
    return new NodeId(this);
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

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
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

  public TypeId getType() {
    return type;
  }

  public void setType(TypeId type) {
    this.type = type;
  }

  public GraphId getTypeGraph() {
    return type != null ? type.getGraph() : null;
  }

  public UUID getTypeGraphId() {
    return type != null ? type.getGraphId() : null;
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

  public Multimap<String, NodeId> getReferences() {
    return MultimapUtils.nullToEmpty(references);
  }

  public void setReferences(Multimap<String, NodeId> references) {
    this.references = references;
  }

  public void addReferences(String attributeId, List<NodeId> references) {
    for (NodeId reference : references) {
      addReference(attributeId, reference);
    }
  }

  public void addReference(String attributeId, NodeId reference) {
    if (references == null) {
      references = LinkedHashMultimap.create();
    }

    references.put(attributeId, reference);
  }

  public Multimap<String, NodeId> getReferrers() {
    return MultimapUtils.nullToEmpty(referrers);
  }

  public void setReferrers(Multimap<String, NodeId> referrers) {
    this.referrers = referrers;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("code", code)
        .add("uri", uri)
        .add("number", number)
        .add("createdBy", createdBy)
        .add("createdDate", createdDate)
        .add("lastModifiedBy", lastModifiedBy)
        .add("lastModifiedDate", lastModifiedDate)
        .add("type", type)
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
    Node node = (Node) o;
    return Objects.equals(id, node.id) &&
        Objects.equals(code, node.code) &&
        Objects.equals(uri, node.uri) &&
        Objects.equals(number, node.number) &&
        Objects.equals(createdBy, node.createdBy) &&
        Objects.equals(createdDate, node.createdDate) &&
        Objects.equals(lastModifiedBy, node.lastModifiedBy) &&
        Objects.equals(lastModifiedDate, node.lastModifiedDate) &&
        Objects.equals(type, node.type) &&
        Objects.equals(properties, node.properties) &&
        Objects.equals(references, node.references);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id,
        code,
        uri,
        number,
        createdBy,
        createdDate,
        lastModifiedBy,
        lastModifiedDate,
        type,
        properties,
        references);
  }

}
