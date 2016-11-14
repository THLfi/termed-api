package fi.thl.termed.util.rdf;

import org.springframework.http.MediaType;

public final class RdfMediaTypes {

  public static final String N_TRIPLES_VALUE = "application/n-triples;charset=UTF-8";
  public static final String RDF_XML_VALUE = "application/rdf+xml;charset=UTF-8";
  public static final String LD_JSON_VALUE = "application/ld+json;charset=UTF-8";
  public static final String TURTLE_VALUE = "text/turtle;charset=UTF-8";
  public static final String N3_VALUE = "text/n3;charset=UTF-8";

  public static final MediaType N_TRIPLES = MediaType.valueOf(N_TRIPLES_VALUE);
  public static final MediaType RDF_XML = MediaType.valueOf(RDF_XML_VALUE);
  public static final MediaType LD_JSON = MediaType.valueOf(LD_JSON_VALUE);
  public static final MediaType TURTLE = MediaType.valueOf(TURTLE_VALUE);
  public static final MediaType N3 = MediaType.valueOf(N3_VALUE);

  private RdfMediaTypes() {
  }

}
