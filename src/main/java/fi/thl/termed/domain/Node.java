package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.time.LocalDateTime;
import java.util.Arrays;
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
  private final LocalDateTime createdDate;
  private final String lastModifiedBy;
  private final LocalDateTime lastModifiedDate;

  private final ImmutableMultimap<String, StrictLangValue> properties;
  private final ImmutableMultimap<String, NodeId> references;
  private final ImmutableMultimap<String, NodeId> referrers;

  public Node(UUID id, TypeId type, String code, String uri, Long number,
      String createdBy, LocalDateTime createdDate, String lastModifiedBy,
      LocalDateTime lastModifiedDate,
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

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public LocalDateTime getLastModifiedDate() {
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

    public Builder random(TypeId type) {
      return new Builder(UUID.randomUUID(), type);
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
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

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

    public Builder createdDate(LocalDateTime createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder lastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
      return this;
    }

    public Builder lastModifiedDate(LocalDateTime lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
      return this;
    }

    public Builder properties(Multimap<String, StrictLangValue> properties) {
      this.properties = properties;
      return this;
    }

    public Builder addProperty(String attributeId, String value) {
      return addProperty(attributeId, new StrictLangValue(value));
    }

    public Builder addProperty(String attributeId, String lang, String value) {
      return addProperty(attributeId, new StrictLangValue(lang, value));
    }

    public Builder addProperty(String attributeId, StrictLangValue... strictLangValues) {
      return addProperty(attributeId, Arrays.asList(strictLangValues));
    }

    public Builder addProperty(String attributeId, Iterable<StrictLangValue> strictLangValues) {
      if (properties == null) {
        properties = ArrayListMultimap.create();
      }
      if (properties instanceof ImmutableMultimap) {
        properties = ArrayListMultimap.create(properties);
      }

      properties.putAll(attributeId, strictLangValues);

      return this;
    }

    public Builder addUniqueProperty(String attributeId, String value) {
      return addUniqueProperty(attributeId, new StrictLangValue(value));
    }

    public Builder addUniqueProperty(String attributeId, String lang, String value) {
      return addUniqueProperty(attributeId, new StrictLangValue(lang, value));
    }

    public Builder addUniqueProperty(String attributeId, StrictLangValue... strictLangValues) {
      return addUniqueProperty(attributeId, Arrays.asList(strictLangValues));
    }

    public Builder addUniqueProperty(String attributeId,
        Iterable<StrictLangValue> strictLangValues) {
      if (properties == null) {
        properties = ArrayListMultimap.create();
      }
      if (properties instanceof ImmutableMultimap) {
        properties = ArrayListMultimap.create(properties);
      }

      strictLangValues.forEach(v -> {
        if (!properties.containsEntry(attributeId, v)) {
          properties.put(attributeId, v);
        }
      });

      return this;
    }

    public Builder replaceProperty(String attributeId, String value) {
      return replaceProperty(attributeId, new StrictLangValue(value));
    }

    public Builder replaceProperty(String attributeId, String lang, String value) {
      return replaceProperty(attributeId, new StrictLangValue(lang, value));
    }

    public Builder replaceProperty(String attributeId, StrictLangValue... strictLangValues) {
      return replaceProperty(attributeId, Arrays.asList(strictLangValues));
    }

    public Builder replaceProperty(String attributeId, Iterable<StrictLangValue> strictLangValues) {
      if (properties == null) {
        properties = ArrayListMultimap.create();
      }
      if (properties instanceof ImmutableMultimap) {
        properties = ArrayListMultimap.create(properties);
      }

      properties.replaceValues(attributeId, strictLangValues);

      return this;
    }

    public Builder addReference(String attributeId, NodeId... valueIds) {
      return addReference(attributeId, Arrays.asList(valueIds));
    }

    public Builder addReference(String attributeId, Iterable<NodeId> valueIds) {
      if (references == null) {
        references = ArrayListMultimap.create();
      }
      if (references instanceof ImmutableMultimap) {
        references = ArrayListMultimap.create(references);
      }

      references.putAll(attributeId, valueIds);

      return this;
    }

    public Builder addUniqueReference(String attributeId, NodeId... valueIds) {
      return addUniqueReference(attributeId, Arrays.asList(valueIds));
    }

    public Builder addUniqueReference(String attributeId, Iterable<NodeId> valueIds) {
      if (references == null) {
        references = ArrayListMultimap.create();
      }
      if (references instanceof ImmutableMultimap) {
        references = ArrayListMultimap.create(references);
      }

      valueIds.forEach(v -> {
        if (!references.containsEntry(attributeId, v)) {
          references.put(attributeId, v);
        }
      });

      return this;
    }

    public Builder replaceReference(String attributeId, NodeId... valueIds) {
      return replaceReference(attributeId, Arrays.asList(valueIds));
    }

    public Builder replaceReference(String attributeId, Iterable<NodeId> valueIds) {
      if (references == null) {
        references = ArrayListMultimap.create();
      }
      if (references instanceof ImmutableMultimap) {
        references = ArrayListMultimap.create(references);
      }

      references.replaceValues(attributeId, valueIds);

      return this;
    }

    public Builder addReferrer(String attributeId, Iterable<NodeId> valueIds) {
      if (referrers == null) {
        referrers = ArrayListMultimap.create();
      }
      if (referrers instanceof ImmutableMultimap) {
        referrers = ArrayListMultimap.create(referrers);
      }

      referrers.putAll(attributeId, valueIds);

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
