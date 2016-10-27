package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClassId implements Serializable {

  private final String id;

  private final SchemeId scheme;

  public ClassId(Resource resource) {
    this(resource.getType());
  }

  public ClassId(ResourceId resourceId) {
    this(resourceId.getType());
  }

  public ClassId(ClassId classId) {
    this(classId.getId(), classId.getScheme());
  }

  public ClassId(Class cls) {
    this(cls.getId(), cls.getScheme());
  }

  public ClassId(String id, UUID schemeId) {
    this(id, new SchemeId(schemeId));
  }

  public ClassId(String id, SchemeId scheme) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.scheme = checkNotNull(scheme, "scheme can't be null in %s", getClass());
  }

  public String getId() {
    return id;
  }

  public SchemeId getScheme() {
    return scheme;
  }

  public UUID getSchemeId() {
    return scheme != null ? scheme.getId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassId classId = (ClassId) o;
    return Objects.equals(id, classId.id) &&
           Objects.equals(scheme, classId.scheme);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, scheme);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("scheme", scheme)
        .toString();
  }

}
