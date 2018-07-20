package fi.thl.termed.domain.transform;

import static fi.thl.termed.util.collect.StreamUtils.zipIndex;

import com.google.common.collect.Multimap;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReferenceAttributeValueIdDtoToModel2
    implements Function<Multimap<String, NodeId>, Stream<Tuple2<NodeAttributeValueId, NodeId>>> {

  private NodeId nodeId;

  public ReferenceAttributeValueIdDtoToModel2(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public Stream<Tuple2<NodeAttributeValueId, NodeId>> apply(Multimap<String, NodeId> input) {

    Stream<Entry<String, Collection<NodeId>>> entries = input.asMap().entrySet().stream();

    return entries.flatMap(entry -> zipIndex(
        entry.getValue().stream().distinct(),
        (value, index) ->
            Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

}
