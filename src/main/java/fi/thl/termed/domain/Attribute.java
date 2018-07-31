package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;
import static fi.thl.termed.util.collect.MultimapUtils.nullableImmutableCopyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class Attribute {

  protected final String id;
  protected final TypeId domain;

  protected final String uri;
  protected final Integer index;

  protected final ImmutableMultimap<String, Permission> permissions;
  protected final ImmutableMultimap<String, LangValue> properties;

  Attribute(String id, TypeId domain, String uri, Integer index,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties) {
    this.id = requireNonNull(id);
    this.domain = requireNonNull(domain);
    this.uri = uri;
    this.index = index;
    this.permissions = nullableImmutableCopyOf(permissions);
    this.properties = nullableImmutableCopyOf(properties);
  }

  public final String getId() {
    return id;
  }

  public final TypeId getDomain() {
    return domain;
  }

  public final String getDomainId() {
    return domain != null ? domain.getId() : null;
  }

  public final UUID getDomainGraphId() {
    return domain != null ? domain.getGraphId() : null;
  }

  public final Optional<String> getUri() {
    return ofNullable(uri);
  }

  public final Optional<Integer> getIndex() {
    return ofNullable(index);
  }

  public final ImmutableMultimap<String, Permission> getPermissions() {
    return nullToEmpty(permissions);
  }

  public final ImmutableMultimap<String, LangValue> getProperties() {
    return nullToEmpty(properties);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Attribute attribute = (Attribute) o;
    return Objects.equals(id, attribute.id) &&
        Objects.equals(uri, attribute.uri) &&
        Objects.equals(index, attribute.index) &&
        Objects.equals(domain, attribute.domain) &&
        Objects.equals(permissions, attribute.permissions) &&
        Objects.equals(properties, attribute.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uri, index, domain, permissions, properties);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  public MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("uri", uri)
        .add("index", index)
        .add("domain", domain)
        .add("permissions", permissions)
        .add("properties", properties);
  }

}
