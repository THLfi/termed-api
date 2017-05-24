package fi.thl.termed.service.node.util;

import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;
import static java.util.Collections.singletonList;
import static org.apache.jena.rdf.model.ResourceFactory.createLangLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.MemoryBasedSystemDao;
import fi.thl.termed.util.permission.PermitAllPermissionEvaluator;
import fi.thl.termed.util.service.DaoForwardingRepository;
import fi.thl.termed.util.service.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.Before;
import org.junit.Test;

public class NodeRdfGraphWrapperTest {

  private Model model;

  @Before
  public void setUp() {
    Map<NodeId, Node> nodes = new HashMap<>();
    Map<TypeId, Type> types = new HashMap<>();

    Service<NodeId, Node> nodeService = new DaoForwardingRepository<>(
        new AuthorizedDao<>(new MemoryBasedSystemDao<>(nodes),
            new PermitAllPermissionEvaluator<>()));
    Service<TypeId, Type> typeService = new DaoForwardingRepository<>(
        new AuthorizedDao<>(new MemoryBasedSystemDao<>(types),
            new PermitAllPermissionEvaluator<>()));

    UUID graphId = nameUUIDFromString("test-graph");

    TypeId conceptId = new TypeId("Concept", graphId);
    Type concept = new Type(conceptId);
    concept.setUri(SKOS.Concept.getURI());
    concept.setTextAttributes(singletonList(
        new TextAttribute("prefLabel", SKOS.prefLabel.getURI(), conceptId)));
    concept.setReferenceAttributes(singletonList(
        new ReferenceAttribute("broader", SKOS.broader.getURI(), conceptId, conceptId)));
    types.put(conceptId, concept);

    NodeId concept1Id = new NodeId(nameUUIDFromString("1"), conceptId);
    Node concept1 = new Node(concept1Id);
    concept1.setUri("http://example.org/Concept_1");
    concept1.addProperty("prefLabel", "en", "Concept 1");
    nodes.put(concept1Id, concept1);

    NodeId concept2Id = new NodeId(nameUUIDFromString("2"), conceptId);
    Node concept2 = new Node(concept2Id);
    concept2.setUri("http://example.org/Concept_2");
    concept2.addProperty("prefLabel", "en", "Concept 2");
    concept2.addReference("broader", concept1Id);
    nodes.put(concept2Id, concept2);

    GraphBase graphBase = new NodeRdfGraphWrapper(
        nodeService, typeService, graphId, new User("test", "", ADMIN), false);

    model = ModelFactory.createModelForGraph(graphBase);
  }

  @Test
  public void wrappedGraphShouldListAllTriples() {
    Set<Statement> expected = ImmutableSet.of(
        createStatement(createResource("http://example.org/Concept_1"), RDF.type, SKOS.Concept),
        createStatement(createResource("http://example.org/Concept_1"), SKOS.prefLabel,
            createLangLiteral("Concept 1", "en")),
        createStatement(createResource("http://example.org/Concept_2"), RDF.type, SKOS.Concept),
        createStatement(createResource("http://example.org/Concept_2"), SKOS.prefLabel,
            createLangLiteral("Concept 2", "en")),
        createStatement(createResource("http://example.org/Concept_2"), SKOS.broader,
            createResource("http://example.org/Concept_1")));

    assertEquals(expected, model.listStatements().toSet());
  }

  @Test
  public void wrappedGraphShouldListTriplesByLiteralValue() {
    Statement expected = createStatement(createResource("http://example.org/Concept_1"),
        SKOS.prefLabel, createLangLiteral("Concept 1", "en"));

    ExtendedIterator<Statement> iterator = model.listStatements(null, null, "Concept 1", "en");

    assertTrue(iterator.hasNext());
    assertEquals(expected, iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void wrappedGraphShouldListTriplesByReferenceValue() {
    Statement expected = createStatement(createResource("http://example.org/Concept_2"),
        SKOS.broader, createResource("http://example.org/Concept_1"));

    ExtendedIterator<Statement> iterator = model.listStatements(null, null,
        createResource("http://example.org/Concept_1"));

    assertTrue(iterator.hasNext());
    assertEquals(expected, iterator.next());
    assertFalse(iterator.hasNext());
  }

}