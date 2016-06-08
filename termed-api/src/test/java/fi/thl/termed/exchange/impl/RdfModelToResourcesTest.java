package fi.thl.termed.exchange.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.util.MultimapTypeAdapterFactory;
import fi.thl.termed.util.rdf.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModel;

import static com.hp.hpl.jena.graph.Node.createLiteral;
import static com.hp.hpl.jena.graph.Node.createURI;

public class RdfModelToResourcesTest {

  private Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
      .create();

  @Test
  public void shouldParseRdfModel() throws JSONException {
    Scheme simpleFoafScheme = createExampleScheme();
    Graph resourceGraph = createExampleRdfGraph();

    RdfModelToResources parser = new RdfModelToResources(simpleFoafScheme);
    RdfModel rdfModel = new JenaRdfModel(resourceGraph);

    // compare as json for easy checking that returned data contains at least expected fields
    String resourcesAsJson = gson.toJson(parser.apply(rdfModel));
    String
        expected =
        "[{'uri':'http://ex.org/bob','type':{'id':'Person'},'properties':{'name':[{'value':'Bob'}]}},"
        +
        " {'uri':'http://ex.org/tim','type':{'id':'Person'},'properties':{'name':[{'value':'Tim'}]}},"
        +
        " {'uri':'http://ex.org/admins','type':{'id':'Group'}," +
        "  'properties':{'name':[{'value':'Admins group'}]}," +
        "  'references':{'member':[{'uri':'http://ex.org/bob'},{'uri':'http://ex.org/tim'}]}}]";

    JSONAssert.assertEquals(expected, resourcesAsJson, JSONCompareMode.LENIENT);
  }

  private Scheme createExampleScheme() {
    Class person = new Class("Person", "http://xmlns.com/foaf/0.1/Person");
    person.setTextAttributes(Lists.newArrayList(
        new TextAttribute("name", "http://xmlns.com/foaf/0.1/name"),
        new TextAttribute("age", "http://xmlns.com/foaf/0.1/age")));

    Class group = new Class("Group", "http://xmlns.com/foaf/0.1/Group");
    group.setTextAttributes(Lists.newArrayList(
        new TextAttribute("name", "http://xmlns.com/foaf/0.1/name")));
    group.setReferenceAttributes(Lists.newArrayList(
        new ReferenceAttribute("member", "http://xmlns.com/foaf/0.1/member", person)));

    Scheme scheme = new Scheme(UUID.randomUUID());
    scheme.setClasses(Lists.newArrayList(person, group));

    return scheme;
  }

  private Graph createExampleRdfGraph() {
    Graph g = Factory.createDefaultGraph();

    g.add(object("http://ex.org/tim", RDF.type.getURI(), "http://xmlns.com/foaf/0.1/Person"));
    g.add(literal("http://ex.org/tim", "http://xmlns.com/foaf/0.1/name", "Tim"));

    g.add(object("http://ex.org/bob", RDF.type.getURI(), "http://xmlns.com/foaf/0.1/Person"));
    g.add(literal("http://ex.org/bob", "http://xmlns.com/foaf/0.1/name", "Bob"));

    g.add(object("http://ex.org/admins", RDF.type.getURI(), "http://xmlns.com/foaf/0.1/Group"));
    g.add(literal("http://ex.org/admins", "http://xmlns.com/foaf/0.1/name", "Admins group"));
    g.add(object("http://ex.org/admins", "http://xmlns.com/foaf/0.1/member", "http://ex.org/tim"));
    g.add(object("http://ex.org/admins", "http://xmlns.com/foaf/0.1/member", "http://ex.org/bob"));

    return g;
  }

  private Triple object(String subjectUri, String predicateUri, String objectUri) {
    return new Triple(createURI(subjectUri), createURI(predicateUri), createURI(objectUri));
  }

  private Triple literal(String subjectUri, String predicateUri, String literalValue) {
    return new Triple(createURI(subjectUri), createURI(predicateUri), createLiteral(literalValue));
  }

}
