package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;

public class ResourceByUri extends SqlSpecification<ResourceId, Resource> {

  private UUID schemeId;
  private String uri;

  public ResourceByUri(UUID schemeId, String uri) {
    this.schemeId = schemeId;
    this.uri = uri;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    return Objects.equal(resource.getSchemeId(), schemeId) && Objects.equal(resource.getUri(), uri);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and uri = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId, uri};
  }

}
