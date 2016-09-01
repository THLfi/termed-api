package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.SchemeRole;

public class SchemeRoleDtoToModel implements Function<List<String>, Map<SchemeRole, Void>> {

  private UUID schemeId;

  public SchemeRoleDtoToModel(UUID schemeId) {
    this.schemeId = schemeId;
  }

  public static SchemeRoleDtoToModel create(UUID schemeId) {
    return new SchemeRoleDtoToModel(schemeId);
  }

  @Override
  public Map<SchemeRole, Void> apply(List<String> roles) {
    Map<SchemeRole, Void> map = Maps.newHashMap();
    for (String role : roles) {
      map.put(new SchemeRole(schemeId, role), null);
    }
    return map;
  }

}
