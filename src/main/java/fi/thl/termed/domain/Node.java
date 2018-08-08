package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.MapUtils.entry;
import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Node implements Identifiable<NodeId> {

  private final UUID id;
  private final TypeId type;

  private final String code;
  private final String uri;
  private final Long number;

  private final String createdBy;
  private final Date createdDate;
  private final String lastModifiedBy;
  private final Date lastModifiedDate;

  private final ImmutableMultimap<String, StrictLangValue> properties;
  private final ImmutableMultimap<String, NodeId> references;
  private final ImmutableMultimap<String, NodeId> referrers;

  public Node(UUID id, TypeId type, String code, String uri, Long number,
      String createdBy, Date createdDate, String lastModifiedBy, Date lastModifiedDate,
      Multimap<String, StrictLangValue> properties,
      Multimap<String, NodeId> references,
      Multimap<String, NodeId> referrers) {
    this.id = id;
    this.type = requireNonNull(type);
    this.code = code;
    this.uri = uri;
    this.number = number;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
    this.properties = nullableImmutableCopyOf(properties);
    this.references = nullableImmutableCopyOf(references);
    this.referrers = nullableImmutableCopyOf(referrers);
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(Node node) {
    Builder builder = new Builder(node.getId(), node.getType());
    builder.copyOptionalsFrom(node);
    return builder;
  }

  @Override
  public NodeId identifier() {
    return new NodeId(this);
  }

  public UUID getId() {
    return id;
  }

  public Optional<String> getCode() {
    return ofNullable(code);
  }

  public Optional<String> getUri() {
    return ofNullable(uri);
  }

  public Long getNumber() {
    return number;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public TypeId getType() {
    return type;
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

  public ImmutableMultimap<String, StrictLangValue> getProperties() {
    return nullToEmpty(properties);
  }

  public ImmutableMultimap<String, NodeId> getReferences() {
    return nullToEmpty(references);
  }

  public ImmutableMultimap<String, NodeId> getReferrers() {
    return nullToEmpty(referrers);
  }

  public Optional<StrictLangValue> getFirstPropertyValue(String attributeId) {
    return getProperties().get(attributeId).stream().findFirst();
  }

  public Optional<NodeId> getFirstReferenceValue(String attributeId) {
    return getReferences().get(attributeId).stream().findFirst();
  }

  public Optional<NodeId> getFirstReferrerValue(String attributeId) {
    return getReferrers().get(attributeId).stream().findFirst();
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

  public static final class IdBuilder {

    IdBuilder() {
    }

    public Builder id(UUID id, String typeId, UUID graphId) {
      return new Builder(id, TypeId.of(typeId, graphId));
    }

    public Builder id(UUID id, TypeId type) {
      return new Builder(id, type);
    }

    public Builder id(NodeId nodeId) {
      return new Builder(nodeId.getId(), nodeId.getType());
    }

  }

  public static final class Builder {

    private final UUID id;
    private final TypeId type;

    private String code;
    private String uri;
    private Long number;

    private String createdBy;
    private Date createdDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;

    private Multimap<String, StrictLangValue> properties;
    private Multimap<String, NodeId> references;
    private Multimap<String, NodeId> referrers;

    Builder(UUID id, TypeId type) {
      this.id = id;
      this.type = requireNonNull(type);
    }

    public Builder copyOptionalsFrom(Node node) {
      this.code = node.code;
      this.uri = node.uri;
      this.number = node.number;

      this.createdBy = node.createdBy;
      this.createdDate = node.createdDate;
      this.lastModifiedBy = node.lastModifiedBy;
      this.lastModifiedDate = node.lastModifiedDate;

      this.properties = node.properties;
      this.references = node.references;
      this.referrers = node.referrers;

      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder number(Long number) {
      this.number = number;
      return this;
    }

    public Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public Builder createdDate(Date createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder lastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
      return this;
    }

    public Builder lastModifiedDate(Date lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
      return this;
    }

    public Builder properties(Multimap<String, StrictLangValue> properties) {
      this.properties = properties;
      return this;
    }

    public Builder addProperties(String k0, String v0) {
      return addProperties(
          k0, new StrictLangValue(v0));
    }

    public Builder addProperties(String k0, String v0, String k1, String v1) {
      return addProperties(
          k0, new StrictLangValue(v0),
          k1, new StrictLangValue(v1));
    }

    public Builder addProperties(String k0, String v0, String k1, String v1, String k2, String v2) {
      return addProperties(
          k0, new StrictLangValue(v0),
          k1, new StrictLangValue(v1),
          k2, new StrictLangValue(v2));
    }

    public Builder addProperties(String k0, StrictLangValue v0) {
      return addProperties(entry(k0, v0));
    }

    public Builder addProperties(String k0, StrictLangValue v0, String k1, StrictLangValue v1) {
      return addProperties(entry(k0, v0), entry(k1, v1));
    }

    public Builder addProperties(String k0, StrictLangValue v0, String k1, StrictLangValue v1,
        String k2, StrictLangValue v2) {
      return addProperties(entry(k0, v0), entry(k1, v1), entry(k2, v2));
    }

    @SafeVarargs
    public final Builder addProperties(Map.Entry<String, StrictLangValue>... entries) {
      return addProperties(ImmutableMultimap.copyOf(Arrays.asList(entries)));
    }

    public Builder addProperties(Multimap<String, StrictLangValue> newProperties) {
      if (properties == null) {
        properties = LinkedHashMultimap.create();
      }
      if (properties instanceof ImmutableMultimap) {
        properties = LinkedHashMultimap.create(properties);
      }

      properties.putAll(newProperties);

      return this;
    }

    public Builder addReferences(String k0, NodeId v0) {
      return addReferences(entry(k0, v0));
    }

    public Builder addReferences(String k0, NodeId v0, String k1, NodeId v1) {
      return addReferences(entry(k0, v0), entry(k1, v1));
    }

    public Builder addReferences(String k0, NodeId v0, String k1, NodeId v1, String k2, NodeId v2) {
      return addReferences(entry(k0, v0), entry(k1, v1), entry(k2, v2));
    }

    @SafeVarargs
    public final Builder addReferences(Map.Entry<String, NodeId>... entries) {
      return addReferences(ImmutableMultimap.copyOf(Arrays.asList(entries)));
    }

    public Builder addReferences(Multimap<String, NodeId> newReferences) {
      if (references == null) {
        references = LinkedHashMultimap.create();
      }
      if (references instanceof ImmutableMultimap) {
        references = LinkedHashMultimap.create(references);
      }

      references.putAll(newReferences);

      return this;
    }

    public Builder references(Multimap<String, NodeId> references) {
      this.references = references;
      return this;
    }

    public Builder referrers(Multimap<String, NodeId> referrers) {
      this.referrers = referrers;
      return this;
    }

    public Node build() {
      return new Node(id, type, code, uri, number, createdBy, createdDate, lastModifiedBy,
          lastModifiedDate, properties, references, referrers);
    }

  }

}
