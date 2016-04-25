package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;

public class ResourceByCode extends SqlSpecification<ResourceId, Resource> {

  private String code;

  public ResourceByCode(String code) {
    this.code = code;
  }

  @Override
  public boolean accept(ResourceId key, Resource value) {
    return Objects.equal(value.getCode(), code);
  }

  @Override
  public String sqlQueryTemplate() {
    return "code = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{code};
  }

}
