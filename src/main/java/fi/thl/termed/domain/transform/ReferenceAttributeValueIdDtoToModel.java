package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;

public class ReferenceAttributeValueIdDtoToModel
    implements Function<Multimap<String, NodeId>, Map<NodeAttributeValueId, NodeId>> {

  private NodeId nodeId;

  public ReferenceAttributeValueIdDtoToModel(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public Map<NodeAttributeValueId, NodeId> apply(Multimap<String, NodeId> input) {

    Map<NodeAttributeValueId, NodeId> result = Maps.newLinkedHashMap();

    for (String attributeId : input.keySet()) {
      int index = 0;

      for (NodeId value : Sets.newLinkedHashSet(input.get(attributeId))) {
        result.put(new NodeAttributeValueId(nodeId, attributeId, index++), value);
      }
    }

    return result;
  }

}
