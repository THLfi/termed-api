package fi.thl.termed.web.docs;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import org.apache.jena.sparql.vocabulary.FOAF;

class DocsExampleData {

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

  static GraphId anotherGraphId = GraphId.random();
  static Graph anotherGraph = Graph.builder()
      .id(anotherGraphId)
      .code("another-graph")
      .uri("http://example.org/termed/another-graph/")
      .roles(asList("guest", "admin"))
      .permissions(testPermissions)
      .properties("prefLabel", LangValue.of("en", "Another Graph"))
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

  static TypeId groupTypeId = TypeId.of("Group", exampleGraphId);
  static Type groupType = Type.builder()
      .id(groupTypeId)
      .uri(FOAF.Group.getURI())
      .nodeCodePrefix("GROUP-")
      .permissions(testPermissions)
      .properties("prefLabel", LangValue.of("en", "Group"))
      .textAttributes(
          TextAttribute.builder()
              .id("name", groupTypeId)
              .regex("^\\w+$")
              .uri(FOAF.name.getURI())
              .permissions(testPermissions)
              .properties("prefLabel", LangValue.of("en", "Name"))
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("member", groupTypeId)
              .range(personTypeId)
              .uri(FOAF.member.getURI())
              .permissions(testPermissions)
              .properties("prefLabel", LangValue.of("en", "Member"))
              .build())
      .build();

  static String exampleUserName = "exampleUsername";
  static User exampleUser = new User(exampleUserName, "examplePassword", AppRole.USER,
      ImmutableList.of(new GraphRole(exampleGraphId, "guest")));

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
