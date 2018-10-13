package fi.thl.termed.web;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import org.apache.jena.sparql.vocabulary.FOAF;

class ApiExampleData {

  static Multimap<String, Permission> testPermissions = ImmutableMultimap.<String, Permission>builder()
      .putAll("guest", READ)
      .putAll("admin", READ, INSERT, UPDATE, DELETE).build();

  static GraphId exampleGraphId = GraphId.random();
  static Graph exampleGraph = Graph.builder()
      .id(exampleGraphId)
      .code("example-graph")
      .uri("http://example.org/termed/example-graph/")
      .roles(asList("guest", "admin"))
      .permissions(testPermissions)
      .properties("prefLabel", LangValue.of("en", "Example Graph"))
      .build();

  static TypeId personTypeId = TypeId.of("Person", exampleGraphId);
  static Type personType = Type.builder()
      .id(personTypeId)
      .uri(FOAF.Person.getURI())
      .nodeCodePrefix("PERSON-")
      .permissions(testPermissions)
      .properties("prefLabel", LangValue.of("en", "Person"))
      .textAttributes(
          TextAttribute.builder()
              .id("name", personTypeId)
              .regex("^\\w+$")
              .uri(FOAF.name.getURI())
              .permissions(testPermissions)
              .properties("prefLabel", LangValue.of("en", "Name"))
              .build(),
          TextAttribute.builder()
              .id("email", personTypeId)
              .regex("^.*@.*$")
              .uri(FOAF.mbox.getURI())
              .permissions(testPermissions)
              .properties("prefLabel", LangValue.of("en", "E-mail"))
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("knows", personTypeId)
              .range(personTypeId)
              .uri(FOAF.knows.getURI())
              .permissions(testPermissions)
              .properties("prefLabel", LangValue.of("en", "Knows"))
              .build())
      .build();

  static NodeId exampleNode0Id = NodeId.random(personTypeId);
  static NodeId exampleNode1Id = NodeId.random(personTypeId);

  static Node exampleNode0 = Node.builder().id(exampleNode0Id)
      .code("example-node-0")
      .uri("http://example.org/example-node-0")
      .addProperty("name", "John")
      .addProperty("email", "john@example.org")
      .addReference("knows", exampleNode1Id)
      .build();

  static Node exampleNode1 = Node.builder().id(exampleNode1Id)
      .code("example-node-1")
      .uri("http://example.org/example-node-1")
      .addProperty("name", "Jane")
      .addProperty("email", "jane@example.org")
      .build();

}
