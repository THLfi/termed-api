package fi.thl.termed.service.node.util;

import static java.lang.String.format;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import java.util.UUID;

final class Urns {

  private Urns() {
  }

  static String urn(GraphId id) {
    return format("urn:termed:graph:%s", id.getId());
  }

  static String urn(TypeId id) {
    return format("urn:termed:graph:%s:type:%s", id.getGraphId(), id.getId());
  }

  static String urn(TextAttributeId id) {
    return format("urn:termed:graph:%s:type:%s:textAttribute:%s",
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

  static String urn(ReferenceAttributeId id) {
    return format("urn:termed:graph:%s:type:%s:referenceAttribute:%s",
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

  static String urnUuid(UUID id) {
    return format("urn:uuid:%s", id);
  }

}
