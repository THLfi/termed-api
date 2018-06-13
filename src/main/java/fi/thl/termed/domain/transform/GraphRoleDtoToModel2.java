package fi.thl.termed.domain.transform;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class GraphRoleDtoToModel2 implements
    Function<List<String>, Stream<Tuple2<GraphRole, Empty>>> {

  private GraphId graphId;

  public GraphRoleDtoToModel2(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public Stream<Tuple2<GraphRole, Empty>> apply(List<String> roles) {
    return roles.stream().map(role -> Tuple.of(new GraphRole(graphId, role), Empty.INSTANCE));
  }

}
