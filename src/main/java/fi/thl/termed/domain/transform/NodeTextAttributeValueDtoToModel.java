package fi.thl.termed.domain.transform;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.RegularExpressions;

import static com.google.common.base.MoreObjects.firstNonNull;

public class NodeTextAttributeValueDtoToModel
    implements
    Function<Multimap<String, StrictLangValue>, Map<NodeAttributeValueId, StrictLangValue>> {

  private NodeId nodeId;

  public NodeTextAttributeValueDtoToModel(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public Map<NodeAttributeValueId, StrictLangValue> apply(
      Multimap<String, StrictLangValue> input) {

    Map<NodeAttributeValueId, StrictLangValue> values = Maps.newLinkedHashMap();

    for (String attributeId : input.keySet()) {
      int index = 0;

      for (StrictLangValue value : Sets.newLinkedHashSet(input.get(attributeId))) {
        if (!Strings.isNullOrEmpty(value.getValue())) {
          values.put(new NodeAttributeValueId(nodeId, attributeId, index++),
                     new StrictLangValue(value.getLang(),
                                         value.getValue(),
                                         firstNonNull(value.getRegex(),
                                                      RegularExpressions.ALL)));
        }
      }
    }

    return values;
  }

}
