package fi.thl.termed.service.type;

import static com.google.common.collect.ImmutableMultimap.of;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TypeServiceIntegrationTest {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<String, User> userService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private UUID testGraphId;

  @Before
  public void setUp() {
    testGraphId = nameUUIDFromString("testGraph");
    testUser = new User("testUser", passwordEncoder.encode(randomUUIDString()), AppRole.ADMIN);

    userService.save(testUser, new User("testInitializer", "", AppRole.SUPERUSER));
    graphService.save(new Graph(testGraphId), testUser);
  }

  @Test
  public void shouldSaveAndDeleteNewType() {
    TypeId conceptId = new TypeId("Concept", new GraphId(testGraphId));
    Type concept = new Type(conceptId);

    assertFalse("Type should not exist, previous tests may have failed to delete types properly",
        typeService.get(conceptId, testUser).isPresent());

    typeService.save(concept, testUser);
    assertTrue(typeService.get(conceptId, testUser).isPresent());

    typeService.delete(conceptId, testUser);
    assertFalse(typeService.get(conceptId, testUser).isPresent());
  }

  @Test
  public void shouldSaveNewTypeWithProperties() {
    TypeId conceptId = new TypeId("Concept", new GraphId(testGraphId));
    Type concept = new Type(conceptId);
    concept.setProperties(of("prefLabel", new LangValue("en", "Preferred label")));

    assertFalse("Type should not exist, previous tests may have failed to delete types properly",
        typeService.get(conceptId, testUser).isPresent());

    typeService.save(concept, testUser);

    Type savedType = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);
    assertEquals(
        singleton(new LangValue("en", "Preferred label")),
        savedType.getProperties().get("prefLabel"));

    typeService.delete(conceptId, testUser);
  }

  @Test
  public void shouldUpdateNewTypeWithProperties() {
    TypeId conceptId = new TypeId("Concept", new GraphId(testGraphId));
    Type concept = new Type(conceptId);
    concept.setProperties(of("prefLabel", new LangValue("en", "Preferred label")));

    assertFalse("Type should not exist, previous tests may have failed to delete types properly",
        typeService.get(conceptId, testUser).isPresent());

    typeService.save(concept, testUser);

    Type savedConcept = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);
    assertEquals(
        singleton(new LangValue("en", "Preferred label")),
        savedConcept.getProperties().get("prefLabel"));

    savedConcept.setProperties(of("prefLabel", new LangValue("en", "Primary label")));

    typeService.save(savedConcept, testUser);

    savedConcept = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);
    assertEquals(
        singleton(new LangValue("en", "Primary label")),
        savedConcept.getProperties().get("prefLabel"));

    typeService.delete(conceptId, testUser);
  }

  @Test
  public void shouldSaveNewTypeWithAttributes() {
    TypeId conceptId = new TypeId("Concept", new GraphId(testGraphId));
    Type concept = new Type(conceptId);
    concept.setTextAttributes(asList(
        new TextAttribute("prefLabel", conceptId),
        new TextAttribute("altLabel", conceptId)));
    concept.setReferenceAttributes(singletonList(
        new ReferenceAttribute("broader", conceptId, conceptId)));

    assertFalse("Type should not exist, previous tests may have failed to delete types properly",
        typeService.get(conceptId, testUser).isPresent());

    typeService.save(concept, testUser);

    Type savedType = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);

    assertEquals("Concept", savedType.getId());
    assertEquals(asList("prefLabel", "altLabel"),
        savedType.getTextAttributes().stream().map(Attribute::getId).collect(toList()));

    assertEquals(singletonList("broader"),
        savedType.getReferenceAttributes().stream().map(Attribute::getId).collect(toList()));

    typeService.delete(conceptId, testUser);
  }

  @Test
  public void shouldUpdateNewTypeWithAttributes() {
    TypeId conceptId = new TypeId("Concept", new GraphId(testGraphId));
    Type concept = new Type(conceptId);
    concept.setTextAttributes(asList(
        new TextAttribute("prefLabel", conceptId),
        new TextAttribute("altLabel", conceptId)));
    concept.setReferenceAttributes(singletonList(
        new ReferenceAttribute("broader", conceptId, conceptId)));

    assertFalse("Type should not exist, previous tests may have failed to delete types properly",
        typeService.get(conceptId, testUser).isPresent());

    typeService.save(concept, testUser);

    Type savedType = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);

    assertEquals("Concept", savedType.getId());
    assertEquals(asList("prefLabel", "altLabel"),
        savedType.getTextAttributes().stream().map(Attribute::getId).collect(toList()));
    assertEquals(singletonList("broader"),
        savedType.getReferenceAttributes().stream().map(Attribute::getId).collect(toList()));

    // update and persist
    concept.setTextAttributes(singletonList(
        new TextAttribute("prefLabel", conceptId)));
    concept.setReferenceAttributes(emptyList());
    typeService.save(concept, testUser);

    savedType = typeService.get(conceptId, testUser)
        .orElseThrow(IllegalStateException::new);

    assertEquals("Concept", savedType.getId());
    assertEquals(singletonList("prefLabel"),
        savedType.getTextAttributes().stream().map(Attribute::getId).collect(toList()));
    assertEquals(emptyList(),
        savedType.getReferenceAttributes().stream().map(Attribute::getId).collect(toList()));

    typeService.delete(conceptId, testUser);
  }

}