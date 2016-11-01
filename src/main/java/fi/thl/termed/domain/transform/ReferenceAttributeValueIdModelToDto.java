package fi.thl.termed.domain.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;

public class ReferenceAttributeValueIdModelToDto
    implements Function<Map<NodeAttributeValueId, NodeId>, Multimap<String, NodeId>> {

  @Override
  public Multimap<String, NodeId> apply(Map<NodeAttributeValueId, NodeId> input) {
    Multimap<String, NodeId> map = LinkedHashMultimap.create();

    for (Map.Entry<NodeAttributeValueId, NodeId> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getValue());
    }

    return map;
  }

}
