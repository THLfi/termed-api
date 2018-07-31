package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Objects;
import java.util.Optional;

public final class Property implements Identifiable<String> {

  private final String id;
  private final String uri;

  private final Integer index;
  private final ImmutableMultimap<String, LangValue> properties;

  public Property(String id, String uri, Integer index, Multimap<String, LangValue> properties) {
    this.id = requireNonNull(id);
    this.uri = uri;
    this.index = index;
    this.properties = nullableImmutableCopyOf(properties);
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(Property property) {
    Builder builder = new Builder(property.getId());
    builder.copyOptionalsFrom(property);
    return builder;
  }

  @Override
  public String identifier() {
    return id;
  }

  public String getId() {
    return id;
  }

  public Optional<String> getUri() {
    return ofNullable(uri);
  }

  public Optional<Integer> getIndex() {
    return ofNullable(index);
  }

  public ImmutableMultimap<String, LangValue> getProperties() {
    return nullToEmpty(properties);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
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
    Property property = (Property) o;
    return Objects.equals(id, property.id) &&
        Objects.equals(uri, property.uri) &&
        Objects.equals(index, property.index) &&
        Objects.equals(properties, property.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uri, index, properties);
  }


  public static final class IdBuilder {

    public Builder id(String id) {
      return new Builder(id);
    }

  }

  public static final class Builder {

    private final String id;
    private String uri;
    private Integer index;
    private Multimap<String, LangValue> properties;

    Builder(String id) {
      this.id = requireNonNull(id);
    }

    public Builder copyOptionalsFrom(Property graph) {
      this.uri = graph.uri;
      this.index = graph.index;
      this.properties = graph.properties;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder index(Integer index) {
      this.index = index;
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

    public Property build() {
      return new Property(id, uri, index, properties);
    }

  }

}
