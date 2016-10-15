package fi.thl.termed.util.rdf;

import java.util.List;

public interface RdfModel {

  /**
   * List subjects that match provided predicate and object. To get for example all foaf:Person
   * instances in the model, one could use find("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
   * "http://xmlns.com/foaf/0.1/Person");
   */
  List<RdfResource> find(String predicateUri, String objectUri);

  /**
   * List all subjects.
   */
  List<RdfResource> find();

  /**
   * Save resources with literal and object properties (i.e. all triples describing a resource).
   */
  void save(List<RdfResource> resources);

}
