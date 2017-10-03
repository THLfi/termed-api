package fi.thl.termed.domain;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Graph implements Identifiable<GraphId> {

  private final UUID id;
  private final String code;
  private final String uri;

  private final ImmutableList<String> roles;
  private final ImmutableMultimap<String, Permission> permissions;
  private final ImmutableMultimap<String, LangValue> properties;

  public Graph(UUID id, String code, String uri,
      List<String> roles,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties) {
    this.id = requireNonNull(id);
    this.code = code;
    this.uri = uri;
    this.roles = nullableImmutableCopyOf(roles);
    this.permissions = nullableImmutableCopyOf(permissions);
    this.properties = nullableImmutableCopyOf(properties);
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(Graph graph) {
    Builder builder = new Builder(graph.getId());
    builder.copyOptionalsFrom(graph);
    return builder;
  }

  @Override
  public GraphId identifier() {
    return new GraphId(this);
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

  public ImmutableList<String> getRoles() {
    return nullToEmpty(roles);
  }

  public ImmutableMultimap<String, Permission> getPermissions() {
    return nullToEmpty(permissions);
  }

  public ImmutableMultimap<String, LangValue> getProperties() {
    return nullToEmpty(properties);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("code", code)
        .add("uri", uri)
        .add("roles", roles)
        .add("permissions", permissions)
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
    Graph graph = (Graph) o;
    return Objects.equals(id, graph.id) &&
        Objects.equals(code, graph.code) &&
        Objects.equals(uri, graph.uri) &&
        Objects.equals(roles, graph.roles) &&
        Objects.equals(permissions, graph.permissions) &&
        Objects.equals(properties, graph.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, uri, roles, permissions, properties);
  }

  public static final class IdBuilder {

    public Builder id(UUID id) {
      return new Builder(id);
    }

    public Builder id(GraphId graphId) {
      return new Builder(graphId.getId());
    }

  }

  public static final class Builder {

    private final UUID id;
    private String code;
    private String uri;
    private List<String> roles;
    private Multimap<String, Permission> permissions;
    private Multimap<String, LangValue> properties;

    Builder(UUID id) {
      this.id = requireNonNull(id);
    }

    public Builder copyOptionalsFrom(Graph graph) {
      this.code = graph.code;
      this.uri = graph.uri;
      this.roles = graph.roles;
      this.permissions = graph.permissions;
      this.properties = graph.properties;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder permissions(Multimap<String, Permission> permissions) {
      this.permissions = permissions;
      return this;
    }

    public Builder roles(List<String> roles) {
      this.roles = roles;
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

    public Graph build() {
      return new Graph(id, code, uri, roles, permissions, properties);
    }

  }

}
