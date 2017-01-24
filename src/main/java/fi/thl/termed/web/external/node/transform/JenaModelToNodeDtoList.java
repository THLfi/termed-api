package fi.thl.termed.web.external.node.transform;


import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import fi.thl.termed.domain.GraphDto;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.vocab.TermedMeta;

public class JenaModelToNodeDtoList implements Function<Model, List<NodeDto>> {

  @Override
  public List<NodeDto> apply(Model model) {
    ResIterator resources = model.listResourcesWithProperty(RDF.type);
    List<NodeDto> nodes = new ArrayList<>();

    for (Resource resource : resources.toList()) {
      NodeDto nodeDto = new NodeDto();

      GraphDto typeGraph = new GraphDto();
      typeGraph.setUri(getUri(model, resource, TermedMeta.graph).orElse(null));

      TypeDto type = new TypeDto();
      type.setUri(getUri(model, resource, RDF.type).orElse(null));
      type.setGraph(typeGraph);

      nodeDto.setType(type);

      nodeDto.setCode(getString(model, resource, TermedMeta.code).orElse(null));
      nodeDto.setCreatedBy(getString(model, resource, TermedMeta.createdBy).orElse(null));
      nodeDto.setCreatedDate(getDate(model, resource, TermedMeta.createdDate).orElse(null));
      nodeDto.setLastModifiedBy(getString(model, resource, TermedMeta.lastModifiedBy).orElse(null));
      nodeDto.setLastModifiedDate(getDate(model, resource, TermedMeta.lastModifiedDate)
                                      .orElse(null));

      Multimap<String, LangValue> properties = LinkedHashMultimap.create();
      Multimap<String, NodeDto> references = LinkedHashMultimap.create();

      for (Statement s : model.listStatements(resource, null, (RDFNode) null).toList()) {
        String predicateUri = s.getPredicate().getURI();
        RDFNode object = s.getObject();

        if (object.isLiteral()) {
          Literal literal = object.asLiteral();
          properties.put(predicateUri, new LangValue(literal.getLanguage(), literal.getString()));
        } else if (object.isURIResource()) {
          NodeDto value = new NodeDto();
          value.setUri(object.asResource().getURI());
          references.put(predicateUri, value);
        }
      }

      nodeDto.setProperties(properties);
      nodeDto.setReferences(references);

      nodes.add(nodeDto);
    }

    return nodes;
  }

  private Optional<String> getUri(Model model, Resource subject, Property property) {
    return model.listObjectsOfProperty(subject, property).toList().stream()
        .filter(RDFNode::isURIResource)
        .map(RDFNode::asResource)
        .findFirst()
        .map(Resource::getURI);
  }

  private Optional<Date> getDate(Model model, Resource subject, Property property) {
    return model.listObjectsOfProperty(subject, property).toList().stream()
        .filter(RDFNode::isLiteral)
        .map(RDFNode::asLiteral)
        .findFirst()
        .map(l -> new DateTime(l.getString()).toDate());
  }

  private Optional<String> getString(Model model, Resource subject, Property property) {
    return model.listObjectsOfProperty(subject, property).toList().stream()
        .filter(RDFNode::isLiteral)
        .map(RDFNode::asLiteral)
        .findFirst()
        .map(Literal::getString);
  }

}
