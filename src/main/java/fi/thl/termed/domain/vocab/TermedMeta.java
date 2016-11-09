package fi.thl.termed.domain.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class TermedMeta {

  public static final String uri = "http://termed.thl.fi/meta/";

  public static final Resource Graph = resource("Graph");
  public static final Resource Type = resource("Type");
  public static final Resource TextAttribute = resource("TextAttribute");
  public static final Resource ReferenceAttribute = resource("ReferenceAttribute");
  public static final Resource Node = resource("Node");

  public static final Property id = property("id");
  public static final Property code = property("code");
  public static final Property graph = property("graph");
  public static final Property createdBy = property("createdBy");
  public static final Property createdDate = property("createdDate");
  public static final Property lastModifiedBy = property("lastModifiedBy");
  public static final Property lastModifiedDate = property("lastModifiedDate");

  private TermedMeta() {
  }

  private static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

  private static Property property(String local) {
    return ResourceFactory.createProperty(uri, local);
  }

}
