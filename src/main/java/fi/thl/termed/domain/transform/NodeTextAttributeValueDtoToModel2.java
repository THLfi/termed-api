package fi.thl.termed.domain.transform;

import static fi.thl.termed.util.collect.StreamUtils.zipIndex;

import com.google.common.collect.Multimap;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

public class NodeTextAttributeValueDtoToModel2 implements
    Function<Multimap<String, StrictLangValue>, Stream<Tuple2<NodeAttributeValueId, StrictLangValue>>> {

  private NodeId nodeId;

  public NodeTextAttributeValueDtoToModel2(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public Stream<Tuple2<NodeAttributeValueId, StrictLangValue>> apply(
      Multimap<String, StrictLangValue> input) {

    Stream<Entry<String, Collection<StrictLangValue>>> entries = input.asMap().entrySet().stream();

    return entries.flatMap(
        entry -> zipIndex(
            entry.getValue().stream().distinct(),
            (value, index) ->
                Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

}
