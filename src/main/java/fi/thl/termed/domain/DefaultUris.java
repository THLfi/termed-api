package fi.thl.termed.domain;

import static java.lang.String.format;

import fi.thl.termed.util.URIs;

public final class DefaultUris {

  private DefaultUris() {
  }

  public static String propertyUri(String ns, String propertyId) {
    return format("%sproperties/%s", URIs.ensureTrailingSlashOrHash(ns), propertyId);
  }

  public static String uri(String ns, GraphId id) {
    return format("%sgraphs/%s", URIs.ensureTrailingSlashOrHash(ns), id.getId());
  }

  public static String uri(String ns, TypeId id) {
    return format("%sgraphs/%s/types/%s",
        URIs.ensureTrailingSlashOrHash(ns), id.getGraphId(), id.getId());
  }

  public static String uri(String ns, NodeId id) {
    return format("%sgraphs/%s/types/%s/nodes/%s",
        URIs.ensureTrailingSlashOrHash(ns), id.getTypeGraphId(), id.getTypeId(), id.getId());
  }

  public static String uri(String ns, TextAttributeId id) {
    return format("%sgraphs/%s/types/%s/textAttributes/%s",
        URIs.ensureTrailingSlashOrHash(ns),
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

  public static String uri(String ns, ReferenceAttributeId id) {
    return format("%sgraphs/%s/types/%s/referenceAttributes/%s",
        URIs.ensureTrailingSlashOrHash(ns),
        id.getDomainId().getGraphId(), id.getDomainId().getId(), id.getId());
  }

}
