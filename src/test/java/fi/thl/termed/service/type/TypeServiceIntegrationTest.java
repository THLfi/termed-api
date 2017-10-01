package fi.thl.termed.service.type;

import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.UUIDs.randomUUIDString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.BadRequestException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
  private Service<String, Property> propertyService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testDataLoader = new User("TestDataLoader", "", SUPERUSER);
  private boolean labelPropertyInsertedByTest = false;

  private Graph[] graphs;
  private User user;

  @Before
  public void setUp() {
    graphs = new Graph[]{
        new Graph(randomUUID()),
        new Graph(randomUUID()),
        new Graph(randomUUID()),
    };
    user = new User("TestUser-" + randomUUID(), passwordEncoder.encode(randomUUIDString()), ADMIN);

    graphService.save(asList(graphs), INSERT, defaultOpts(), testDataLoader);
    userService.save(user, INSERT, defaultOpts(), testDataLoader);

    if (!propertyService.exists("label", testDataLoader)) {
      propertyService.save(new Property("label"), INSERT, defaultOpts(), testDataLoader);
      labelPropertyInsertedByTest = true;
    }
  }

  @After
  public void tearDown() {
    graphService.delete(stream(graphs).map(Graph::identifier).collect(toList()),
        defaultOpts(), testDataLoader);
    userService.delete(user.identifier(), defaultOpts(), testDataLoader);
    if (labelPropertyInsertedByTest) {
      propertyService.delete("label", defaultOpts(), testDataLoader);
    }
  }

  @Test
  public void shouldInsertAndDeleteNewType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    typeService.delete(typeId, defaultOpts(), user);
    assertFalse(typeService.get(typeId, user).isPresent());
  }

  @Test
  public void shouldInsertAndDeleteMultipleNewTypes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    Type type0 = Type.builder().id(typeId0).build();

    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());
    Type type1 = Type.builder().id(typeId1).build();

    assertFalse(typeService.exists(typeId0, user));
    assertFalse(typeService.exists(typeId1, user));

    typeService.save(asList(type0, type1), INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId0, user).isPresent());
    assertTrue(typeService.get(typeId1, user).isPresent());

    typeService.delete(asList(typeId0, typeId1), defaultOpts(), user);
    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());
  }

  @Test
  public void shouldAllowRepeatedUpsertOfType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, UPSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    typeService.save(type, UPSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    typeService.delete(typeId, defaultOpts(), user);
    assertFalse(typeService.get(typeId, user).isPresent());
  }

  @Test
  public void shouldNotUpdateNonExistentType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    try {
      typeService.save(type, UPDATE, defaultOpts(), user);
      fail("Expected BadRequestException");
    } catch (BadRequestException e) {
      assertFalse(typeService.exists(typeId, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

  @Test
  public void shouldNotInsertExistingType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    try {
      typeService.save(type, INSERT, defaultOpts(), user);
      fail("Expected BadRequestException");
    } catch (BadRequestException e) {
      assertTrue(typeService.exists(typeId, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }

    typeService.delete(typeId, defaultOpts(), user);
    assertFalse(typeService.get(typeId, user).isPresent());
  }

  @Test
  public void shouldSaveTypeWithProperties() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .properties("label", LangValue.of("TestType label")).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);

    Type savedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("TestType label")),
        savedType.getProperties().get("label"));

    typeService.delete(typeId, defaultOpts(), user);
  }

  @Test
  public void shouldUpdateTypeWithProperties() {
    TypeId typeId = TypeId.of("Concept", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .properties("label", LangValue.of("TestType label")).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);

    Type savedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("TestType label")),
        savedType.getProperties().get("label"));

    Type updatedType = Type.builderFromCopyOf(savedType)
        .properties("label", LangValue.of("Updated TestType label")).build();

    typeService.save(updatedType, UPDATE, defaultOpts(), user);

    Type savedUpdatedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);
    assertEquals(
        singletonList(LangValue.of("Updated TestType label")),
        savedUpdatedType.getProperties().get("label"));

    typeService.delete(typeId, defaultOpts(), user);
  }

  @Test
  public void shouldSaveTypeWithAttributes() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .textAttributes(
            new TextAttribute("label", typeId),
            new TextAttribute("note", typeId))
        .referenceAttributes(
            new ReferenceAttribute("child", typeId, typeId))
        .build();

    assertFalse(typeService.get(typeId, user).isPresent());

    typeService.save(type, INSERT, defaultOpts(), user);

    Type savedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);

    assertEquals("TestType", savedType.getId());
    assertEquals(
        asList("label", "note"),
        attributeIds(savedType.getTextAttributes()));

    assertEquals(
        singletonList("child"),
        attributeIds(savedType.getReferenceAttributes()));

    typeService.delete(typeId, defaultOpts(), user);
  }

  @Test
  public void shouldSaveTypesWithCrossReferencingAttributes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(new ReferenceAttribute("neighbour", typeId0, typeId1))
        .build();
    Type type1 = Type.builder().id(typeId1)
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    typeService.save(asList(type0, type1), INSERT, defaultOpts(), user);

    Type savedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);

    assertEquals(typeId1, savedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(asList(typeId0, typeId1), defaultOpts(), user);
  }

  @Test
  public void shouldNotSaveTypeWithReferencingAttributePointingNonExistentType() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(new ReferenceAttribute("neighbour", typeId0, typeId1))
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    try {
      typeService.save(type0, INSERT, defaultOpts(), user);
      fail("Expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
      assertFalse(typeService.exists(typeId0, user));
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

  @Test
  public void shouldSaveAndDeleteAndSaveTypesWithCrossReferencingAttributes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(new ReferenceAttribute("neighbour", typeId0, typeId1))
        .build();
    Type type1 = Type.builder().id(typeId1)
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    typeService.save(asList(type0, type1), INSERT, defaultOpts(), user);
    Type savedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);
    assertEquals(typeId1, savedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(asList(typeId0, typeId1), defaultOpts(), user);

    typeService.save(asList(type0, type1), INSERT, defaultOpts(), user);
    Type reSavedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);
    assertEquals(typeId1, reSavedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(asList(typeId0, typeId1), defaultOpts(), user);

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());
  }

  @Test
  public void shouldSaveTypesWithCrossReferencingAttributesOverDifferentGraphs() {
    TypeId graph0TypeId = TypeId.of("TestType", graphs[0].identifier());
    TypeId graph1TypeId = TypeId.of("TestType", graphs[1].identifier());

    Type graph0Type = Type.builder().id(graph0TypeId)
        .referenceAttributes(new ReferenceAttribute("neighbour", graph0TypeId, graph1TypeId))
        .build();
    Type graph1Type = Type.builder().id(graph1TypeId)
        .build();

    assertFalse(typeService.get(graph0TypeId, user).isPresent());
    assertFalse(typeService.get(graph1TypeId, user).isPresent());

    typeService.save(asList(graph0Type, graph1Type), INSERT, defaultOpts(), user);

    Type savedGraph0Type = typeService.get(graph0TypeId, user).orElseThrow(AssertionError::new);

    assertEquals(graph1TypeId, savedGraph0Type.getReferenceAttributes().get(0).getRange());

    typeService.delete(asList(graph0TypeId, graph1TypeId), defaultOpts(), user);
  }

  @Test
  public void shouldUpdateTypeWithAttributes() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .textAttributes(
            new TextAttribute("label", typeId),
            new TextAttribute("note", typeId))
        .referenceAttributes(
            new ReferenceAttribute("child", typeId, typeId))
        .build();

    assertFalse(typeService.get(typeId, user).isPresent());

    typeService.save(type, INSERT, defaultOpts(), user);

    Type savedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);

    assertEquals("TestType", savedType.getId());
    assertEquals(
        asList("label", "note"),
        attributeIds(savedType.getTextAttributes()));
    assertEquals(
        singletonList("child"),
        attributeIds(savedType.getReferenceAttributes()));

    // update and persist
    Type updatedType = Type.builderFromCopyOf(type)
        .textAttributes(new TextAttribute("label", typeId))
        .referenceAttributes(emptyList())
        .build();

    typeService.save(updatedType, UPDATE, defaultOpts(), user);

    Type updatedSavedType = typeService.get(typeId, user).orElseThrow(AssertionError::new);

    assertEquals("TestType", updatedSavedType.getId());
    assertEquals(
        singletonList("label"),
        attributeIds(updatedSavedType.getTextAttributes()));
    assertEquals(
        emptyList(),
        attributeIds(updatedSavedType.getReferenceAttributes()));

    typeService.delete(typeId, defaultOpts(), user);
  }

  @Test
  public void shouldCascadeCrossReferencingAttributes() {
    TypeId graph0TypeId = TypeId.of("TestType", graphs[0].identifier());
    TypeId graph1TypeId = TypeId.of("TestType", graphs[1].identifier());

    Type graph0Type = Type.builder().id(graph0TypeId)
        .referenceAttributes(new ReferenceAttribute("neighbour", graph0TypeId, graph1TypeId))
        .build();
    Type graph1Type = Type.builder().id(graph1TypeId)
        .build();

    assertFalse(typeService.get(graph0TypeId, user).isPresent());
    assertFalse(typeService.get(graph1TypeId, user).isPresent());

    typeService.save(asList(graph0Type, graph1Type), INSERT, defaultOpts(), user);

    Type savedGraph0Type = typeService.get(graph0TypeId, user).orElseThrow(AssertionError::new);

    assertEquals(
        graph1TypeId,
        savedGraph0Type.getReferenceAttributes().get(0).getRange());

    typeService.delete(graph1TypeId, defaultOpts(), user);

    Type savedGraph0TypeWithCascadedRefAttributes = typeService.get(graph0TypeId, user)
        .orElseThrow(AssertionError::new);

    assertTrue(savedGraph0TypeWithCascadedRefAttributes.getReferenceAttributes().isEmpty());

    typeService.delete(graph0TypeId, defaultOpts(), user);
  }

  private List<String> attributeIds(List<? extends Attribute> attributes) {
    return attributes.stream().map(Attribute::getId).collect(toList());
  }

}