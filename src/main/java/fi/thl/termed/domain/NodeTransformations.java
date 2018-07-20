package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.StreamUtils.zipIndex;

import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class NodeTransformations {

  private NodeTransformations() {
  }

  public static Stream<Tuple2<NodeAttributeValueId, StrictLangValue>> nodePropertiesToRows(
      NodeId nodeId, Multimap<String, StrictLangValue> properties) {

    Stream<Entry<String, Collection<StrictLangValue>>> entries =
        properties.asMap().entrySet().stream();

    return entries.flatMap(
        entry -> zipIndex(
            entry.getValue().stream().distinct(),
            (value, index) ->
                Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

  public static Stream<Tuple2<NodeAttributeValueId, NodeId>> nodeReferencesToRows(
      NodeId nodeId, Multimap<String, NodeId> references) {

    Stream<Entry<String, Collection<NodeId>>> entries =
        references.asMap().entrySet().stream();

    return entries.flatMap(entry -> zipIndex(
        entry.getValue().stream().distinct(),
        (value, index) ->
            Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

}
