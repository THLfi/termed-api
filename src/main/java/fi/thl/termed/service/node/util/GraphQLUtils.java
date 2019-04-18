package fi.thl.termed.service.node.util;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;

public final class GraphQLUtils {

  private GraphQLUtils() {
  }

  public static String toGraphQlTypeName(TypeId typeId) {
    return ("_" + UUIDs.toString(typeId.getGraphId()) + "_" + typeId.getId()).replace('-', '_');
  }

}
