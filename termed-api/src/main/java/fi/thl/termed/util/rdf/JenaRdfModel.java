package fi.thl.termed.util.rdf;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.Model;

import java.util.List;

import static com.google.common.collect.Lists.transform;
import static com.hp.hpl.jena.graph.Node.createURI;
import static fi.thl.termed.util.ListUtils.filter;

public class JenaRdfModel implements RdfModel {

  private Graph graph;

  public JenaRdfModel(Model model) {
    this(model.getGraph());
  }

  public JenaRdfModel(Graph graph) {
    this.graph = graph;
  }

  @Override
  public List<RdfResource> find(String predicateUri, String objectUri) {
    List<RdfResource> resources = Lists.newArrayList();

    for (Node subject : subjects(predicateUri, objectUri)) {
      resources.add(toRdfResource(subject));
    }

    return resources;
  }

  private RdfResource toRdfResource(Node subject) {
    RdfResource resource = new RdfResource(subject.isURI() ? subject.getURI() :
                                           subject.getBlankNodeLabel());

    for (Node predicate : predicates(subject)) {
      for (LiteralLabel literalValue : literals(objects(subject, predicate))) {
        resource.addLiteral(predicate.getURI(),
                            literalValue.language(),
                            literalValue.getLexicalForm());
      }
      for (String uriValue : uris(objects(subject, predicate))) {
        resource.addObject(predicate.getURI(),
                           uriValue);
      }
    }
    return resource;
  }

  private List<LiteralLabel> literals(List<Node> nodes) {
    return transform(filter(nodes, new Predicate<Node>() {
      public boolean apply(Node input) {
        return input.isLiteral();
      }
    }), new Function<Node, LiteralLabel>() {
      public LiteralLabel apply(Node input) {
        return input.getLiteral();
      }
    });
  }

  private List<String> uris(List<Node> nodes) {
    return transform(filter(nodes, new Predicate<Node>() {
      public boolean apply(Node input) {
        return input.isURI();
      }
    }), new Function<Node, String>() {
      public String apply(Node input) {
        return input.getURI();
      }
    });
  }

  private List<Node> subjects(String predicateUri, String objectUri) {
    return subjects(graph.find(Node.ANY, createURI(predicateUri), createURI(objectUri)).toList());
  }

  private List<Node> subjects(List<Triple> triples) {
    return transform(triples, new Function<Triple, Node>() {
      public Node apply(Triple input) {
        return input.getSubject();
      }
    });
  }

  private List<Node> predicates(Node subject) {
    return predicates(graph.find(subject, Node.ANY, Node.ANY).toList());
  }

  private List<Node> predicates(List<Triple> triples) {
    return transform(triples, new Function<Triple, Node>() {
      public Node apply(Triple input) {
        return input.getPredicate();
      }
    });
  }

  private List<Node> objects(Node subject, Node predicate) {
    return objects(graph.find(subject, predicate, Node.ANY).toList());
  }

  private List<Node> objects(List<Triple> triples) {
    return transform(triples, new Function<Triple, Node>() {
      public Node apply(Triple input) {
        return input.getObject();
      }
    });
  }

}
