package fi.thl.termed.service.node;

import static fi.thl.termed.domain.User.newSuperuser;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.query.Queries.query;
import static fi.thl.termed.util.query.Specifications.asLucene;
import static fi.thl.termed.util.query.Specifications.asSql;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
abstract class BaseNodeServiceIntegrationTest {

  protected User user;
  protected UUID graphId;

  @Autowired
  protected Service<GraphId, Graph> graphService;
  @Autowired
  protected Service<TypeId, Type> typeService;
  @Autowired
  protected Service<NodeId, Node> nodeService;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private Service<String, User> userService;

  @BeforeEach
  public void setUp() {
    insertTestUser();
    insertTestGraph();
    insertTestTypes();
    verifyGraphIsEmpty();
  }

  private void insertTestUser() {
    user = User.newAdmin("TestUser-" + randomUUID(), passwordEncoder.encode(randomUUIDString()));
    userService.save(user, INSERT, defaultOpts(), newSuperuser("test-initializer"));
  }

  private void insertTestGraph() {
    graphId = UUID.randomUUID();
    graphService.save(Graph.builder().id(graphId).build(), INSERT, defaultOpts(), user);
  }

  private void insertTestTypes() {
    typeService.save(buildTestTypes().stream(), INSERT, defaultOpts(), user);
  }

  private void verifyGraphIsEmpty() {
    assertEquals(0, nodeService.count(asSql(new NodesByGraphId(graphId)), user));
    assertEquals(0, nodeService.count(asLucene(new NodesByGraphId(graphId)), user));
  }

  private List<Type> buildTestTypes() {
    TypeId personId = TypeId.of("Person", graphId);
    TypeId groupId = TypeId.of("Group", graphId);

    Type person = Type.builder().id(personId)
        .textAttributes(
            TextAttribute.builder().id("name", personId).regexAll().build(),
            TextAttribute.builder().id("email", personId).regex("^.*@.*$").build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("knows", personId).range(personId).build())
        .build();

    Type group = Type.builder().id(groupId)
        .textAttributes(
            TextAttribute.builder().id("name", groupId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("member", groupId).range(personId).build())
        .build();

    return Arrays.asList(person, group);
  }

  @AfterEach
  public void tearDown() {
    verifyNodeIndexIntegrity();
    deleteTestNodes();
    deleteTestTypes();
    deleteTestGraph();
    deleteTestUser();
  }

  private void verifyNodeIndexIntegrity() {
    NodesByGraphId nodesByGraphId = new NodesByGraphId(graphId);

    try (
        Stream<Node> dbNodeStream = nodeService.values(query(asSql(nodesByGraphId)), user);
        Stream<Node> indexNodeStream = nodeService.values(query(asLucene(nodesByGraphId)), user)) {

      Supplier<TreeSet<Node>> nodeTreeSetSupplier =
          () -> new TreeSet<>(Comparator.comparing(Node::getId));

      Set<Node> dbNodes = dbNodeStream.collect(toCollection(nodeTreeSetSupplier));
      Set<Node> indexNodes = indexNodeStream.collect(toCollection(nodeTreeSetSupplier));

      assertEquals(dbNodes, indexNodes);
    }
  }

  private void deleteTestNodes() {
    nodeService.delete(
        nodeService.keys(new Query<>(new NodesByGraphId(graphId)), user), defaultOpts(), user);
  }

  private void deleteTestTypes() {
    typeService.delete(
        typeService.keys(new Query<>(new TypesByGraphId(graphId)), user), defaultOpts(), user);
  }

  private void deleteTestGraph() {
    graphService.delete(new GraphId(graphId), defaultOpts(), user);
  }

  private void deleteTestUser() {
    userService.delete(user.identifier(), defaultOpts(), newSuperuser("test-cleaner"));
  }

}
