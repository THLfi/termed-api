package fi.thl.termed.domain.transform;

import java.util.function.Function;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeRole;

public class SchemeRoleDtoToModel implements Function<List<String>, Map<SchemeRole, Empty>> {

  private UUID schemeId;

  public SchemeRoleDtoToModel(UUID schemeId) {
    this.schemeId = schemeId;
  }

  public static SchemeRoleDtoToModel create(UUID schemeId) {
    return new SchemeRoleDtoToModel(schemeId);
  }

  @Override
  public Map<SchemeRole, Empty> apply(List<String> roles) {
    Map<SchemeRole, Empty> map = Maps.newHashMap();
    for (String role : roles) {
      map.put(new SchemeRole(schemeId, role), Empty.INSTANCE);
    }
    return map;
  }

}
