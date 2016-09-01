package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;

public class ResourceByCode extends SqlSpecification<ResourceId, Resource> {

  private UUID schemeId;
  private String typeId;
  private String code;

  public ResourceByCode(UUID schemeId, String typeId, String code) {
    this.schemeId = schemeId;
    this.typeId = typeId;
    this.code = code;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    return Objects.equal(resource.getSchemeId(), schemeId) &&
           Objects.equal(resource.getTypeId(), typeId) &&
           Objects.equal(resource.getCode(), code);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and type_id = ? and code = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId, typeId, code};
  }

}
