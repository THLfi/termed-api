package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.collect.MultimapUtils;

public class Scheme implements Identifiable<SchemeId> {

  private UUID id;
  private String code;
  private String uri;

  private List<String> roles;
  private Multimap<String, Permission> permissions;
  private Multimap<String, LangValue> properties;

  public Scheme(UUID id) {
    this.id = id;
  }

  public Scheme(UUID id, String code, String uri) {
    this.id = id;
    this.code = code;
    this.uri = uri;
  }

  public Scheme(UUID id, Multimap<String, LangValue> properties) {
    this.id = id;
    this.properties = properties;
  }

  public Scheme(Scheme scheme) {
    this.id = scheme.id;
    this.code = scheme.code;
    this.uri = scheme.uri;
    this.roles = scheme.roles;
    this.permissions = scheme.permissions;
    this.properties = scheme.properties;
  }

  @Override
  public SchemeId identifier() {
    return new SchemeId(this);
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

  public List<String> getRoles() {
    return ListUtils.nullToEmpty(roles);
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public Multimap<String, Permission> getPermissions() {
    return MultimapUtils.nullToEmpty(permissions);
  }

  public void setPermissions(Multimap<String, Permission> permissions) {
    this.permissions = permissions;
  }

  public Multimap<String, LangValue> getProperties() {
    return MultimapUtils.nullToEmpty(properties);
  }

  public void setProperties(Multimap<String, LangValue> properties) {
    this.properties = properties;
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
    Scheme scheme = (Scheme) o;
    return Objects.equals(id, scheme.id) &&
           Objects.equals(code, scheme.code) &&
           Objects.equals(uri, scheme.uri) &&
           Objects.equals(roles, scheme.roles) &&
           Objects.equals(permissions, scheme.permissions) &&
           Objects.equals(properties, scheme.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, uri, roles, permissions, properties);
  }

}
