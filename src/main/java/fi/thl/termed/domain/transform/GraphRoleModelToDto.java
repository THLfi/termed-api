package fi.thl.termed.domain.transform;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphRole;

public class GraphRoleModelToDto implements Function<Map<GraphRole, Empty>, List<String>> {

  @Override
  public List<String> apply(Map<GraphRole, Empty> map) {
    List<String> roles = Lists.newArrayList();
    for (GraphRole graphRole : map.keySet()) {
      roles.add(graphRole.getRole());
    }
    return roles;
  }

}
