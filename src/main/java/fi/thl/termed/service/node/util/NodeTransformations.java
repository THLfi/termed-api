package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.collect.StreamUtils.zipIndex;

import com.google.common.collect.Multimap;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class NodeTransformations {

  private NodeTransformations() {
  }

  /**
   * Convert properties multimap to stream of id-value tuples
   */
  public static Stream<Tuple2<NodeAttributeValueId, StrictLangValue>> nodePropertiesToRows(
      NodeId nodeId, Multimap<String, StrictLangValue> properties) {

    Stream<Entry<String, Collection<StrictLangValue>>> entries =
        properties.asMap().entrySet().stream();

    return entries.flatMap(
        entry -> zipIndex(
            entry.getValue().stream(),
            (value, index) ->
                Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

  /**
   * Convert references multimap to stream of id-value tuples
   */
  public static Stream<Tuple2<NodeAttributeValueId, NodeId>> nodeReferencesToRows(
      NodeId nodeId, Multimap<String, NodeId> references) {

    Stream<Entry<String, Collection<NodeId>>> entries =
        references.asMap().entrySet().stream();

    return entries.flatMap(entry -> zipIndex(
        entry.getValue().stream(),
        (value, index) ->
            Tuple.of(new NodeAttributeValueId(nodeId, entry.getKey(), index), value)));
  }

}
