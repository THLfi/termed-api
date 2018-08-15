package fi.thl.termed.service.node;

import static fi.thl.termed.domain.User.newSuperuser;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.UUID.randomUUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class BaseNodeServiceIntegrationTest {

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

  @Before
  public void setUp() {
    insertTestUser();
    insertTestGraph();
    insertTestTypes();
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

  protected abstract List<Type> buildTestTypes();

  @After
  public void tearDown() {
    deleteTestNodes();
    deleteTestTypes();
    deleteTestGraph();
    deleteTestUser();
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
