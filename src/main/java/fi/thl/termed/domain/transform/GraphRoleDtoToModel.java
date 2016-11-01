package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;

public class GraphRoleDtoToModel implements Function<List<String>, Map<GraphRole, Empty>> {

  private GraphId graphId;

  public GraphRoleDtoToModel(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public Map<GraphRole, Empty> apply(List<String> roles) {
    Map<GraphRole, Empty> map = Maps.newHashMap();
    for (String role : roles) {
      map.put(new GraphRole(graphId, role), Empty.INSTANCE);
    }
    return map;
  }

}
