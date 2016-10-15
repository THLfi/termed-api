package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.collect.MultimapUtils;

public class Scheme {

  private UUID id;
  private String code;
  private String uri;

  private List<String> roles;
  private Multimap<String, Permission> permissions;
  private Multimap<String, LangValue> properties;

  private List<Class> classes;

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
    this.classes = scheme.classes;
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

  public List<Class> getClasses() {
    return ListUtils.nullToEmpty(classes);
  }

  public void setClasses(List<Class> classes) {
    this.classes = classes;
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
        .add("classes", classes)
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
    return Objects.equal(id, scheme.id) &&
           Objects.equal(code, scheme.code) &&
           Objects.equal(uri, scheme.uri) &&
           Objects.equal(roles, scheme.roles) &&
           Objects.equal(permissions, scheme.permissions) &&
           Objects.equal(properties, scheme.properties) &&
           Objects.equal(classes, scheme.classes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, code, uri, roles, permissions, properties, classes);
  }

}
