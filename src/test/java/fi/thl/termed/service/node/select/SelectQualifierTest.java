package fi.thl.termed.service.node.select;

import static com.google.common.collect.ImmutableSet.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SelectQualifierTest {

  private UUID graphId = UUID.randomUUID();

  private TypeId conceptTypeId = TypeId.of("Concept", graphId);
  private TypeId termTypeId = TypeId.of("Term", graphId);
  private TypeId collectionTypeId = TypeId.of("Collection", graphId);

  private Type conceptType = Type.builder()
      .id(conceptTypeId)
      .textAttributes(
          TextAttribute.builder()
              .id("prefLabel", conceptTypeId)
              .regexAll()
              .build(),
          TextAttribute.builder()
              .id("altLabel", conceptTypeId)
              .regexAll()
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("prefLabelXl", conceptTypeId)
              .range(termTypeId)
              .build(),
          ReferenceAttribute.builder()
              .id("broader", conceptTypeId)
              .range(conceptTypeId)
              .build(),
          ReferenceAttribute.builder()
              .id("related", conceptTypeId)
              .range(conceptTypeId)
              .build())
      .build();

  private Type termType = Type.builder()
      .id(termTypeId)
      .textAttributes(
          TextAttribute.builder()
              .id("value", termTypeId)
              .regexAll()
              .build(),
          TextAttribute.builder()
              .id("lang", termTypeId)
              .regexAll()
              .build())
      .build();

  private Type collectionType = Type.builder()
      .id(collectionTypeId)
      .textAttributes(
          TextAttribute.builder()
              .id("prefLabel", collectionTypeId)
              .regexAll()
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("member", collectionTypeId)
              .range(conceptTypeId)
              .build(),
          ReferenceAttribute.builder()
              .id("related", collectionTypeId)
              .range(collectionTypeId)
              .build())
      .build();

  private List<Type> allTypes = ImmutableList.of(conceptType, collectionType, termType);

  @Test
  void testSelectAllProperties() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "altLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectAllProperties())));
  }

  @Test
  void testSelectAllPropertiesFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "altLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllProperties())));
  }

  @Test
  void testSelectAllPropertiesFromMultipleDomainsWithQualifier() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "altLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllProperties("Concept"))));
  }

  @Test
  void testSelectSingleProperty() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectProperty("prefLabel"))));
  }

  @Test
  void testSelectSinglePropertyFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectProperty("prefLabel"))));
  }

  @Test
  void testSelectSinglePropertyWithQualifierFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectProperty("Concept", "prefLabel"))));
  }

  @Test
  void testSelectAllReferences() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(conceptTypeId, "prefLabelXl"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectAllReferences())));
  }

  @Test
  void testSelectAllReferencesFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(conceptTypeId, "prefLabelXl")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(collectionTypeId, "member")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(collectionTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllReferences())));
  }

  @Test
  void testSelectAllReferencesFromMultipleDomainsWithQualifier() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(conceptTypeId, "prefLabelXl"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllReferences("Concept"))));
  }

  @Test
  void testSelectSingleReference() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectReference("related"))));
  }

  @Test
  void testSelectSingleReferenceWithDepth() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(conceptTypeId, "related"), 2)),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectReference("related", 2))));
  }

  @Test
  void testSelectSinglePropertyAndSingleReference() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(collectionTypeId, "member"))),
        new SelectQualifier(allTypes).apply(
            of(collectionType),
            of(
                new SelectProperty("prefLabel"),
                new SelectReference("member"))));
  }

  @Test
  void testSelectSingleQualifiedPropertyAndSingleReference() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(collectionTypeId, "member"))),
        new SelectQualifier(allTypes).apply(
            of(collectionType),
            of(
                new SelectProperty("Concept", "prefLabel"),
                new SelectReference("member"))));
  }

  @Test
  void testSelectSingleReferenceFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(collectionTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectReference("related"))));
  }

  @Test
  void testSelectPropertiesAndReferenceWithDepth() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(termTypeId, "value")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(collectionTypeId, "member")),
            new SelectTypeQualifiedReference(
                new ReferenceAttributeId(conceptTypeId, "prefLabelXl"), 2)
        ),
        new SelectQualifier(allTypes).apply(
            of(collectionType),
            of(
                new SelectProperty("prefLabel"),
                new SelectProperty("value"),
                new SelectReference("member"),
                new SelectReference("prefLabelXl", 2))));
  }

  @Test
  void testSelectPropertiesAndReferenceWithInsufficientDepth() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(collectionTypeId, "member"))
        ),
        new SelectQualifier(allTypes).apply(
            of(collectionType),
            of(
                new SelectProperty("prefLabel"),
                new SelectReference("member"),
                // This select is ignored as field 'prefLabelXl' at depth 2
                // and this select is limited to depth 1 (by default).
                new SelectReference("prefLabelXl"),
                // This is ignored as there are no selected types with property named 'value'.
                // Type Term has field 'value' but prefLabelXl that has 'Term' value is ignored as
                // previously seen.
                new SelectProperty("value"))));
  }

  @Test
  void testSelectAllReferrers() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "member"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectAllReferrers())));
  }

  @Test
  void testSelectAllReferrersFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "member")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(collectionTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllReferrers())));
  }

  @Test
  void testSelectAllReferrersFromMultipleDomainsWithQualifier() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "broader")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "member"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectAllReferrers("Concept"))));
  }

  @Test
  void testSelectSingleReferrer() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectReferrer("related"))));
  }

  @Test
  void testSelectSingleReferrerWithDepth() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related"), 2)),
        new SelectQualifier(allTypes).apply(
            of(conceptType),
            of(new SelectReferrer("related", 2))));
  }

  @Test
  void testSelectSingleReferrerFromMultipleDomains() {
    assertEquals(
        of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "related")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(collectionTypeId, "related"))),
        new SelectQualifier(allTypes).apply(
            of(conceptType, collectionType),
            of(new SelectReferrer("related"))));
  }

  @Test
  void testSelectPropertiesAndReferrerWithDepth() {
    assertEquals(
        of(
            new SelectTypeQualifiedProperty(new TextAttributeId(collectionTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(conceptTypeId, "prefLabel")),
            new SelectTypeQualifiedProperty(new TextAttributeId(termTypeId, "value")),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(conceptTypeId, "member"), 2),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(termTypeId, "prefLabelXl"))
        ),
        new SelectQualifier(allTypes).apply(
            of(termType),
            of(
                new SelectProperty("prefLabel"),
                new SelectProperty("value"),
                new SelectReferrer("prefLabelXl"),
                new SelectReferrer("member", 2))));
  }

}
