package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;

public class ResourceByUri extends SqlSpecification<ResourceId, Resource> {

  private String uri;

  public ResourceByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean accept(ResourceId key, Resource value) {
    return Objects.equal(value.getUri(), uri);
  }

  @Override
  public String sqlQueryTemplate() {
    return "uri = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{uri};
  }

}
