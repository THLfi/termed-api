package fi.thl.termed.util.jena;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;


public class JenaRdfModel implements RdfModel {

  private Graph graph;

  public JenaRdfModel(RdfModel rdfModel) {
    if (rdfModel instanceof JenaRdfModel) {
      this.graph = ((JenaRdfModel) rdfModel).getGraph();
    } else {
      save(rdfModel.find());
    }
  }

  public JenaRdfModel(Model model) {
    this(model.getGraph());
  }

  public JenaRdfModel(Graph graph) {
    this.graph = graph;
  }

  public Model getModel() {
    return ModelFactory.createModelForGraph(graph);
  }

  public Graph getGraph() {
    return graph;
  }

  @Override
  public List<RdfResource> find(String predicateUri, String objectUri) {
    return subjects(predicateUri, objectUri).stream()
        .map(this::toRdfResource).collect(Collectors.toList());
  }

  @Override
  public List<RdfResource> find() {
    return subjects().stream()
        .map(this::toRdfResource).collect(Collectors.toList());
  }

  @Override
  public Optional<RdfResource> find(String subjectUri) {
    return subjects(subjectUri).stream().map(this::toRdfResource).findFirst();
  }

  private RdfResource toRdfResource(Node subject) {
    RdfResource resource = new RdfResource(
        subject.isURI() ? subject.getURI() : subject.getBlankNodeLabel());

    for (Node predicate : predicates(subject)) {
      for (LiteralLabel literal : literals(objects(subject, predicate))) {
        resource.addLiteral(predicate.getURI(), literal.language(), literal.getLexicalForm());
      }
      for (String uri : uris(objects(subject, predicate))) {
        resource.addObject(predicate.getURI(), uri);
      }
    }
    return resource;
  }

  private List<LiteralLabel> literals(List<Node> nodes) {
    return nodes.stream().filter(Node::isLiteral).map(Node::getLiteral)
        .collect(Collectors.toList());
  }

  private List<String> uris(List<Node> nodes) {
    return nodes.stream().filter(Node::isURI).map(Node::getURI)
        .collect(Collectors.toList());
  }

  private List<Node> subjects() {
    return subjects(graph.find(Node.ANY, Node.ANY, Node.ANY).toList());
  }

  private List<Node> subjects(String predicateUri, String objectUri) {
    return subjects(graph.find(Node.ANY,
                               NodeFactory.createURI(predicateUri),
                               NodeFactory.createURI(objectUri)).toList());
  }

  private List<Node> subjects(String subjectUri) {
    return subjects(graph.find(NodeFactory.createURI(subjectUri),
                               Node.ANY, Node.ANY).toList());
  }

  private List<Node> subjects(List<Triple> triples) {
    return triples.stream().map(Triple::getSubject).collect(Collectors.toList());
  }

  private List<Node> predicates(Node subject) {
    return predicates(graph.find(subject, Node.ANY, Node.ANY).toList());
  }

  private List<Node> predicates(List<Triple> triples) {
    return triples.stream().map(Triple::getPredicate).collect(Collectors.toList());
  }

  private List<Node> objects(Node subject, Node predicate) {
    return objects(graph.find(subject, predicate, Node.ANY).toList());
  }

  private List<Node> objects(List<Triple> triples) {
    return triples.stream().map(Triple::getObject).collect(Collectors.toList());
  }

  @Override
  public JenaRdfModel save(List<RdfResource> resources) {
    for (RdfResource resource : resources) {
      Node subject = NodeFactory.createURI(resource.getUri());

      for (Map.Entry<String, LangValue> entry : resource.getLiterals().entries()) {
        LangValue langValue = entry.getValue();
        graph.add(Triple.create(
            subject,
            NodeFactory.createURI(entry.getKey()),
            NodeFactory.createLiteral(langValue.getValue(), langValue.getLang(), false)));
      }

      for (Map.Entry<String, String> entry : resource.getObjects().entries()) {
        graph.add(Triple.create(
            subject,
            NodeFactory.createURI(entry.getKey()),
            NodeFactory.createURI(entry.getValue())));
      }
    }
    return this;
  }

}
