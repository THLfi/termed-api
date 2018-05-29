package fi.thl.termed.domain;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static fi.thl.termed.util.collect.ListUtils.nullToEmpty;
import static fi.thl.termed.util.collect.ListUtils.nullableImmutableCopyOf;
import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Type implements Identifiable<TypeId> {

  private final String id;
  private final GraphId graph;

  private final String uri;
  private final String nodeCodePrefix;
  private final Integer index;

  private final ImmutableMultimap<String, Permission> permissions;
  private final ImmutableMultimap<String, LangValue> properties;

  private final ImmutableList<TextAttribute> textAttributes;
  private final ImmutableList<ReferenceAttribute> referenceAttributes;

  public Type(String id, GraphId graph, String uri, String nodeCodePrefix, Integer index,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties,
      List<TextAttribute> textAttributes,
      List<ReferenceAttribute> referenceAttributes) {
    this.id = requireNonNull(id);
    this.graph = requireNonNull(graph);
    this.uri = uri;
    this.nodeCodePrefix = nodeCodePrefix;
    this.index = index;
    this.permissions = nullableImmutableCopyOf(permissions);
    this.properties = nullableImmutableCopyOf(properties);
    this.textAttributes = nullableImmutableCopyOf(textAttributes);
    this.referenceAttributes = nullableImmutableCopyOf(referenceAttributes);
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(Type type) {
    Builder builder = new Builder(type.getId(), type.getGraph());
    builder.copyOptionalsFrom(type);
    return builder;
  }

  public String getId() {
    return id;
  }

  public GraphId getGraph() {
    return graph;
  }

  public UUID getGraphId() {
    return graph != null ? graph.getId() : null;
  }

  public Optional<String> getUri() {
    return ofNullable(uri);
  }

  public Optional<String> getNodeCodePrefix() {
    return ofNullable(nodeCodePrefix);
  }

  public String getNodeCodePrefixOrDefault() {
    return getNodeCodePrefix().orElse(UPPER_CAMEL.to(LOWER_HYPHEN, id) + "-");
  }

  public Optional<Integer> getIndex() {
    return ofNullable(index);
  }

  public ImmutableMultimap<String, Permission> getPermissions() {
    return nullToEmpty(permissions);
  }

  public ImmutableMultimap<String, LangValue> getProperties() {
    return nullToEmpty(properties);
  }

  public ImmutableList<TextAttribute> getTextAttributes() {
    return nullToEmpty(textAttributes);
  }

  public ImmutableList<ReferenceAttribute> getReferenceAttributes() {
    return nullToEmpty(referenceAttributes);
  }

  @Override
  public TypeId identifier() {
    return new TypeId(id, graph);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("graph", graph)
        .add("uri", uri)
        .add("nodeCodePrefix", nodeCodePrefix)
        .add("index", index)
        .add("permissions", permissions)
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
    Type cls = (Type) o;
    return Objects.equals(id, cls.id) &&
        Objects.equals(graph, cls.graph) &&
        Objects.equals(uri, cls.uri) &&
        Objects.equals(nodeCodePrefix, cls.nodeCodePrefix) &&
        Objects.equals(index, cls.index) &&
        Objects.equals(permissions, cls.permissions) &&
        Objects.equals(properties, cls.properties) &&
        Objects.equals(textAttributes, cls.textAttributes) &&
        Objects.equals(referenceAttributes, cls.referenceAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, graph, uri, nodeCodePrefix, index, permissions, properties,
        textAttributes, referenceAttributes);
  }

  public static final class IdBuilder {

    IdBuilder() {
    }

    public Builder id(String id, UUID graphId) {
      return new Builder(id, GraphId.of(graphId));
    }

    public Builder id(String id, GraphId graph) {
      return new Builder(id, graph);
    }

    public Builder id(TypeId typeId) {
      return new Builder(typeId.getId(), typeId.getGraph());
    }

  }

  public static final class Builder {

    private final String id;
    private final GraphId graph;

    private String uri;
    private String nodeCodePrefix;
    private Integer index;
    private Multimap<String, Permission> permissions;
    private Multimap<String, LangValue> properties;
    private List<TextAttribute> textAttributes;
    private List<ReferenceAttribute> referenceAttributes;

    Builder(String id, GraphId graph) {
      this.id = requireNonNull(id);
      this.graph = requireNonNull(graph);
    }

    public Builder copyOptionalsFrom(Type type) {
      this.uri = type.uri;
      this.nodeCodePrefix = type.nodeCodePrefix;
      this.index = type.index;
      this.permissions = type.permissions;
      this.properties = type.properties;
      this.textAttributes = type.textAttributes;
      this.referenceAttributes = type.referenceAttributes;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder nodeCodePrefix(String nodeCodePrefix) {
      this.nodeCodePrefix = nodeCodePrefix;
      return this;
    }

    public Builder index(Integer index) {
      this.index = index;
      return this;
    }

    public Builder permissions(Multimap<String, Permission> permissions) {
      this.permissions = permissions;
      return this;
    }

    public Builder properties(Multimap<String, LangValue> properties) {
      this.properties = properties;
      return this;
    }

    public Builder properties(String k0, LangValue v0) {
      this.properties = ImmutableMultimap.of(k0, v0);
      return this;
    }

    public Builder properties(String k0, LangValue v0, String k1, LangValue v1) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1);
      return this;
    }

    public Builder properties(String k0, LangValue v0, String k1, LangValue v1, String k2,
        LangValue v2) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1, k2, v2);
      return this;
    }

    public Builder textAttributes(TextAttribute... textAttributes) {
      this.textAttributes = Arrays.asList(textAttributes);
      return this;
    }

    public Builder textAttributes(List<TextAttribute> textAttributes) {
      this.textAttributes = textAttributes;
      return this;
    }

    public Builder referenceAttributes(ReferenceAttribute... referenceAttributes) {
      this.referenceAttributes = Arrays.asList(referenceAttributes);
      return this;
    }

    public Builder referenceAttributes(List<ReferenceAttribute> referenceAttributes) {
      this.referenceAttributes = referenceAttributes;
      return this;
    }

    public Type build() {
      return new Type(id, graph, uri, nodeCodePrefix, index,
          permissions, properties, textAttributes, referenceAttributes);
    }

  }

}
