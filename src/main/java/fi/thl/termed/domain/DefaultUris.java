package fi.thl.termed.domain;

import static java.lang.String.format;

public final class DefaultUris {

  private DefaultUris() {
  }

  public static String propertyUri(String propertyId) {
    return format("termed:property:%s", propertyId);
  }

  public static String uri(GraphId id) {
    return format("termed:graph:%s", id.getId());
  }

  public static String uri(TypeId id) {
    return format("termed:graph:%s:type:%s", id.getGraphId(), id.getId());
  }

  public static String uri(NodeId id) {
    return format("termed:graph:%s:type:%s:node:%s",
        id.getTypeGraphId(), id.getTypeId(), id.getId());
  }

  public static String uri(TextAttributeId id) {
    return format("termed:graph:%s:type:%s:textAttribute:%s",
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

  public static String uri(ReferenceAttributeId id) {
    return format("termed:graph:%s:type:%s:referenceAttribute:%s",
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

}
