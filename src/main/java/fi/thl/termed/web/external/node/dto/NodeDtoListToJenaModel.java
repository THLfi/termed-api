package fi.thl.termed.web.external.node.dto;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.vocab.TermedMeta;
import fi.thl.termed.util.URIs;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class NodeDtoListToJenaModel implements Function<List<NodeDto>, Model> {

  @Override
  public Model apply(List<NodeDto> nodes) {
    Model model = ModelFactory.createDefaultModel();

    for (NodeDto nodeDto : nodes) {
      TypeDto typeDto = nodeDto.getType();

      Resource subject = createResource(uri(nodeDto));

      addResource(model, subject, RDF.type, typeDto.getUri());
      addResource(model, subject, TermedMeta.graph, typeDto.getGraphUri());

      addLiteral(model, subject, TermedMeta.id, nodeDto.getId());
      addLiteral(model, subject, TermedMeta.code, nodeDto.getCode());
      addLiteral(model, subject, TermedMeta.createdBy, nodeDto.getCreatedBy());
      addLiteral(model, subject, TermedMeta.createdDate, nodeDto.getCreatedDate());
      addLiteral(model, subject, TermedMeta.lastModifiedBy, nodeDto.getLastModifiedBy());
      addLiteral(model, subject, TermedMeta.lastModifiedDate, nodeDto.getLastModifiedDate());

      for (Map.Entry<String, LangValue> entry : nodeDto.getProperties().entries()) {
        addLiteral(model, subject, model.createProperty(entry.getKey()), entry.getValue());
      }

      for (Map.Entry<String, NodeDto> entry : nodeDto.getReferences().entries()) {
        addResource(model, subject, model.createProperty(entry.getKey()), uri(entry.getValue()));
      }
    }

    return model;
  }

  private void addResource(Model model, Resource subject, Property predicate, String objectUri) {
    if (objectUri != null) {
      model.add(model.createStatement(subject, predicate, model.createResource(objectUri)));
    }
  }

  private void addLiteral(Model model, Resource subject, Property predicate, UUID uuid) {
    if (uuid != null) {
      model.add(model.createStatement(subject, predicate, uuid.toString()));
    }
  }

  private void addLiteral(Model model, Resource subject, Property predicate, Date date) {
    if (date != null) {
      model.add(model.createStatement(subject, predicate, new DateTime(date).toString()));
    }
  }

  private void addLiteral(Model model, Resource subject, Property predicate, String literal) {
    if (literal != null) {
      model.add(model.createStatement(subject, predicate, literal));
    }
  }

  private void addLiteral(Model model, Resource subject, Property predicate, LangValue langVal) {
    if (langVal != null) {
      model.add(model.createStatement(subject, predicate, langVal.getValue(), langVal.getLang()));
    }
  }

  private String uri(NodeDto nodeDto) {
    return nodeDto.getUri() != null ? nodeDto.getUri() : URIs.uuidUrn(nodeDto.getId());
  }

}
