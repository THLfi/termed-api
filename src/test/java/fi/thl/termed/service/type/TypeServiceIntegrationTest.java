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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
class TypeServiceIntegrationTest {

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

  @BeforeAll
  void setUp() {
    graphs = new Graph[]{
        Graph.builder().id(randomUUID()).build(),
        Graph.builder().id(randomUUID()).build(),
        Graph.builder().id(randomUUID()).build(),
    };
    user = new User("TestUser-" + randomUUID(), passwordEncoder.encode(randomUUIDString()), ADMIN);

    graphService.save(Stream.of(graphs), INSERT, defaultOpts(), testDataLoader);
    userService.save(user, INSERT, defaultOpts(), testDataLoader);

    if (!propertyService.exists("label", testDataLoader)) {
      propertyService.save(Property.builder().id("label").build(),
          INSERT, defaultOpts(), testDataLoader);
      labelPropertyInsertedByTest = true;
    }
  }

  @AfterAll
  void tearDown() {
    graphService.delete(stream(graphs).map(Graph::identifier), defaultOpts(), testDataLoader);
    userService.delete(user.identifier(), defaultOpts(), testDataLoader);
    if (labelPropertyInsertedByTest) {
      propertyService.delete("label", defaultOpts(), testDataLoader);
    }
  }

  @Test
  void shouldInsertAndDeleteNewType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    typeService.delete(typeId, defaultOpts(), user);
    assertFalse(typeService.get(typeId, user).isPresent());
  }

  @Test
  void shouldNotInsertTypeWithIllegalId() {
    TypeId typeId = TypeId.of("Te$tType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> typeService.save(type, INSERT, defaultOpts(), user));

    assertFalse(typeService.exists(typeId, user));
  }

  @Test
  void shouldInsertAndDeleteMultipleNewTypes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    Type type0 = Type.builder().id(typeId0).build();

    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());
    Type type1 = Type.builder().id(typeId1).build();

    assertFalse(typeService.exists(typeId0, user));
    assertFalse(typeService.exists(typeId1, user));

    typeService.save(Stream.of(type0, type1), INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId0, user).isPresent());
    assertTrue(typeService.get(typeId1, user).isPresent());

    typeService.delete(Stream.of(typeId0, typeId1), defaultOpts(), user);
    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());
  }

  @Test
  void shouldAllowRepeatedUpsertOfType() {
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
  void shouldNotUpdateNonExistentType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, UPDATE, defaultOpts(), user);

    assertFalse(typeService.exists(typeId, user));
  }

  @Test
  void shouldNotInsertExistingType() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId).build();

    assertFalse(typeService.exists(typeId, user));

    typeService.save(type, INSERT, defaultOpts(), user);
    assertTrue(typeService.get(typeId, user).isPresent());

    assertThrows(DuplicateKeyException.class,
        () -> typeService.save(type, INSERT, defaultOpts(), user));

    assertTrue(typeService.exists(typeId, user));

    typeService.delete(typeId, defaultOpts(), user);

    assertFalse(typeService.get(typeId, user).isPresent());
  }

  @Test
  void shouldSaveTypeWithProperties() {
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
  void shouldUpdateTypeWithProperties() {
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
  void shouldSaveTypeWithAttributes() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .textAttributes(
            TextAttribute.builder().id("label", typeId).regexAll().build(),
            TextAttribute.builder().id("note", typeId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("child", typeId).range(typeId).build())
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
  void shouldNotSaveTypeWithIllegalAttributeIds() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .textAttributes(
            TextAttribute.builder().id("label", typeId).regexAll().build(),
            TextAttribute.builder().id("note!", typeId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("child", typeId).range(typeId).build())
        .build();

    assertFalse(typeService.exists(typeId, user));

    assertThrows(DataIntegrityViolationException.class,
        () -> typeService.save(type, INSERT, defaultOpts(), user));

    assertFalse(typeService.exists(typeId, user));
  }

  @Test
  void shouldSaveTypesWithCrossReferencingAttributes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(
            ReferenceAttribute.builder().id("neighbour", typeId0).range(typeId1).build())
        .build();
    Type type1 = Type.builder().id(typeId1)
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    typeService.save(Stream.of(type0, type1), INSERT, defaultOpts(), user);

    Type savedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);

    assertEquals(typeId1, savedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(Stream.of(typeId0, typeId1), defaultOpts(), user);
  }

  @Test
  void shouldNotSaveTypeWithReferencingAttributePointingNonExistentType()
      throws InterruptedException {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(
            ReferenceAttribute.builder().id("neighbour", typeId0).range(typeId1).build())
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    assertThrows(DataIntegrityViolationException.class,
        () -> typeService.save(type0, INSERT, defaultOpts(), user));

    assertFalse(typeService.exists(typeId0, user));
  }

  @Test
  void shouldSaveAndDeleteAndSaveTypesWithCrossReferencingAttributes() {
    TypeId typeId0 = TypeId.of("TestType0", graphs[0].identifier());
    TypeId typeId1 = TypeId.of("TestType1", graphs[0].identifier());

    Type type0 = Type.builder().id(typeId0)
        .referenceAttributes(
            ReferenceAttribute.builder().id("neighbour", typeId0).range(typeId1).build())
        .build();
    Type type1 = Type.builder().id(typeId1)
        .build();

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());

    typeService.save(Stream.of(type0, type1), INSERT, defaultOpts(), user);
    Type savedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);
    assertEquals(typeId1, savedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(Stream.of(typeId0, typeId1), defaultOpts(), user);

    typeService.save(Stream.of(type0, type1), INSERT, defaultOpts(), user);
    Type reSavedType0 = typeService.get(typeId0, user).orElseThrow(AssertionError::new);
    assertEquals(typeId1, reSavedType0.getReferenceAttributes().get(0).getRange());

    typeService.delete(Stream.of(typeId0, typeId1), defaultOpts(), user);

    assertFalse(typeService.get(typeId0, user).isPresent());
    assertFalse(typeService.get(typeId1, user).isPresent());
  }

  @Test
  void shouldSaveTypesWithCrossReferencingAttributesOverDifferentGraphs() {
    TypeId graph0TypeId = TypeId.of("TestType", graphs[0].identifier());
    TypeId graph1TypeId = TypeId.of("TestType", graphs[1].identifier());

    Type graph0Type = Type.builder().id(graph0TypeId)
        .referenceAttributes(
            ReferenceAttribute.builder().id("neighbour", graph0TypeId).range(graph1TypeId).build())
        .build();
    Type graph1Type = Type.builder().id(graph1TypeId)
        .build();

    assertFalse(typeService.get(graph0TypeId, user).isPresent());
    assertFalse(typeService.get(graph1TypeId, user).isPresent());

    typeService.save(Stream.of(graph0Type, graph1Type), INSERT, defaultOpts(), user);

    Type savedGraph0Type = typeService.get(graph0TypeId, user).orElseThrow(AssertionError::new);

    assertEquals(graph1TypeId, savedGraph0Type.getReferenceAttributes().get(0).getRange());

    typeService.delete(Stream.of(graph0TypeId, graph1TypeId), defaultOpts(), user);
  }

  @Test
  void shouldUpdateTypeWithAttributes() {
    TypeId typeId = TypeId.of("TestType", graphs[0].identifier());
    Type type = Type.builder().id(typeId)
        .textAttributes(
            TextAttribute.builder().id("label", typeId).regexAll().build(),
            TextAttribute.builder().id("note", typeId).regexAll().build())
        .referenceAttributes(
            ReferenceAttribute.builder().id("child", typeId).range(typeId).build())
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
        .textAttributes(TextAttribute.builder().id("label", typeId).regexAll().build())
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
  void shouldCascadeCrossReferencingAttributes() {
    TypeId graph0TypeId = TypeId.of("TestType", graphs[0].identifier());
    TypeId graph1TypeId = TypeId.of("TestType", graphs[1].identifier());

    Type graph0Type = Type.builder().id(graph0TypeId)
        .referenceAttributes(
            ReferenceAttribute.builder().id("neighbour", graph0TypeId).range(graph1TypeId).build())
        .build();
    Type graph1Type = Type.builder().id(graph1TypeId)
        .build();

    assertFalse(typeService.get(graph0TypeId, user).isPresent());
    assertFalse(typeService.get(graph1TypeId, user).isPresent());

    typeService.save(Stream.of(graph0Type, graph1Type), INSERT, defaultOpts(), user);

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