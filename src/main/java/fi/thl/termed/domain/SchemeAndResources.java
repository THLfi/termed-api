package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.util.List;

public class SchemeAndResources {

  private Scheme scheme;
  private List<Resource> resources;

  public SchemeAndResources(Scheme scheme, List<Resource> resources) {
    this.scheme = scheme;
    this.resources = resources;
  }

  public Scheme getScheme() {
    return scheme;
  }

  public List<Resource> getResources() {
    return resources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemeAndResources that = (SchemeAndResources) o;
    return Objects.equals(scheme, that.scheme) &&
           Objects.equals(resources, that.resources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scheme, resources);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("scheme", scheme)
        .add("resources", resources)
        .toString();
  }

}
