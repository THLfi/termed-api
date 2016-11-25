package fi.thl.termed.web.external.node.dto;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.assertj.core.util.Strings;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import fi.thl.termed.domain.GraphDto;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.vocab.TermedMeta;
import fi.thl.termed.util.URIs;

import static java.lang.String.format;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class NodeDtoListToJenaModel implements Function<List<NodeDto>, Model> {

  private String baseUri;

  public NodeDtoListToJenaModel(String baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public Model apply(List<NodeDto> nodes) {
    Model model = ModelFactory.createDefaultModel();

    model.setNsPrefix("skos", SKOS.uri);
    model.setNsPrefix("rdfs", RDFS.uri);
    model.setNsPrefix("rdf", RDF.uri);
    model.setNsPrefix("owl", OWL.NS);
    model.setNsPrefix("termed", TermedMeta.uri);

    for (NodeDto nodeDto : nodes) {
      TypeDto typeDto = nodeDto.getType();
      GraphDto graphDto = typeDto.getGraph();

      if (!Strings.isNullOrEmpty(graphDto.getCode()) &&
          !Strings.isNullOrEmpty(graphDto.getUri())) {
        model.setNsPrefix(graphDto.getCode(), graphDto.getUri());
      }

      Resource subject = createResource(uri(nodeDto));

      addResource(model, subject, RDF.type, typeUri(typeDto));
      addResource(model, subject, TermedMeta.graph, graphUri(graphDto));

      addLiteral(model, subject, TermedMeta.id, nodeDto.getId());
      addLiteral(model, subject, TermedMeta.typeId, typeDto.getId());
      addLiteral(model, subject, TermedMeta.graphId, graphDto.getId());
      addLiteral(model, subject, TermedMeta.graphCode, graphDto.getCode());
      addLiteral(model, subject, TermedMeta.code, nodeDto.getCode());
      addLiteral(model, subject, TermedMeta.createdBy, nodeDto.getCreatedBy());
      addLiteral(model, subject, TermedMeta.createdDate, nodeDto.getCreatedDate());
      addLiteral(model, subject, TermedMeta.lastModifiedBy, nodeDto.getLastModifiedBy());
      addLiteral(model, subject, TermedMeta.lastModifiedDate, nodeDto.getLastModifiedDate());

      for (Map.Entry<String, LangValue> entry : nodeDto.getProperties().entries()) {
        addLiteral(model, subject,
                   model.createProperty(textAttributeUri(typeDto, entry.getKey())),
                   entry.getValue());
      }

      for (Map.Entry<String, NodeDto> entry : nodeDto.getReferences().entries()) {
        addResource(model, subject,
                    model.createProperty(referenceAttributeUri(typeDto, entry.getKey())),
                    uri(entry.getValue()));
      }
    }

    return model;
  }

  private String graphUri(GraphDto graph) {
    return !Strings.isNullOrEmpty(graph.getUri()) ? graph.getUri() : format(
        "%s/graphs/%s", baseUri, graph.getId());
  }

  private String typeUri(TypeDto type) {
    return !Strings.isNullOrEmpty(type.getUri()) ? type.getUri() : format(
        "%s/graphs/%s/types/%s", baseUri, type.getGraphId(), type.getId());
  }

  private String textAttributeUri(TypeDto type, String key) {
    String uri = type.getTextAttributes().get(key);
    return !Strings.isNullOrEmpty(uri) ? uri : format(
        "%s/graphs/%s/types/%s/textAttributes/%s",
        baseUri, type.getGraphId(), type.getId(), key);
  }


  private String referenceAttributeUri(TypeDto type, String key) {
    String uri = type.getReferenceAttributes().get(key);
    return !Strings.isNullOrEmpty(uri) ? uri : format(
        "%s/graphs/%s/types/%s/referenceAttributes/%s",
        baseUri, type.getGraphId(), type.getId(), key);
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
