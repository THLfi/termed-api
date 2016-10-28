package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;

public class SchemeRoleDtoToModel implements Function<List<String>, Map<SchemeRole, Empty>> {

  private SchemeId schemeId;

  public SchemeRoleDtoToModel(SchemeId schemeId) {
    this.schemeId = schemeId;
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
