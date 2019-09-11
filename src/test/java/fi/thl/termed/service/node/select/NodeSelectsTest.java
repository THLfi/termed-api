package fi.thl.termed.service.node.select;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.collect.Tuple;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NodeSelectsTest {

  @Test
  void shouldUseDeepestSelectOnRefSelects() {
    TypeId exampleTypeId = TypeId.of("ExampleType", UUID.randomUUID());

    assertEquals(
        ImmutableMap.of(Tuple.of(exampleTypeId, "foo"), 10),
        NodeSelects.toReferenceSelectsWithDepths(ImmutableList.of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(exampleTypeId, "foo"), 2),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(exampleTypeId, "foo"), 10))));

    assertEquals(
        ImmutableMap.of(Tuple.of(exampleTypeId, "foo"), 10),
        NodeSelects.toReferenceSelectsWithDepths(ImmutableList.of(
            new SelectTypeQualifiedReference(new ReferenceAttributeId(exampleTypeId, "foo"), 10),
            new SelectTypeQualifiedReference(new ReferenceAttributeId(exampleTypeId, "foo"), 2))));

    assertEquals(
        ImmutableMap.of(Tuple.of(exampleTypeId, "foo"), 10),
        NodeSelects.toReferrerSelectsWithDepths(ImmutableList.of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(exampleTypeId, "foo"), 2),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(exampleTypeId, "foo"), 10))));

    assertEquals(
        ImmutableMap.of(Tuple.of(exampleTypeId, "foo"), 10),
        NodeSelects.toReferrerSelectsWithDepths(ImmutableList.of(
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(exampleTypeId, "foo"), 10),
            new SelectTypeQualifiedReferrer(new ReferenceAttributeId(exampleTypeId, "foo"), 2))));
  }

}
