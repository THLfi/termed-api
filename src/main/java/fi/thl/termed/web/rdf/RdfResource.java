package fi.thl.termed.web.rdf;

import com.google.common.base.MoreObjects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collections;

import fi.thl.termed.domain.LangValue;

/**
 * Class to represent RDF resource with literal and object properties. Object property values are
 * URIs.
 */
public class RdfResource {

  private String uri;

  private Multimap<String, LangValue> literals;

  private Multimap<String, String> objects;

  public RdfResource(String uri) {
    this.uri = uri;
    this.literals = LinkedHashMultimap.create();
    this.objects = LinkedHashMultimap.create();
  }

  public String getUri() {
    return uri;
  }

  public Multimap<String, LangValue> getLiterals() {
    return literals;
  }

  public Iterable<LangValue> getLiterals(String predicateUri) {
    return literals.containsKey(predicateUri) ? literals.get(predicateUri)
                                              : Collections.<LangValue>emptyList();
  }

  public void addLiteral(String predicateURI, String lang, String value) {
    literals.put(predicateURI, new LangValue(lang, value));
  }

  public Multimap<String, String> getObjects() {
    return objects;
  }

  public Iterable<String> getObjects(String predicateUri) {
    return objects.containsKey(predicateUri) ? objects.get(predicateUri)
                                             : Collections.<String>emptySet();
  }

  public void addObject(String predicateUri, String objectUri) {
    objects.put(predicateUri, objectUri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("uri", uri)
        .add("literals", literals)
        .add("objects", objects)
        .toString();
  }

}
