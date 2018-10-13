package fi.thl.termed.service.node.specification;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.BoostSpecification.boost;
import static fi.thl.termed.util.query.NotSpecification.not;
import static fi.thl.termed.util.query.OrSpecification.or;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NodeSpecificationParserTest {

  private NodeSpecificationParser parser = new NodeSpecificationParser();

  @Test
  void shouldParsePrefixQuery() {
    assertEquals(or(and(
        new NodesByPropertyPrefix("label", "", "ab"))),
        parser.apply("properties.label:ab*"));
  }

  @Test
  void shouldParseBooleanQueries() {
    assertEquals(or(and(
        new NodesByPropertyPrefix("label", "", "ab"),
        new NodesByPropertyPrefix("note", "", "ab"))),
        parser.apply("properties.label:ab* AND properties.note:ab*"));

    assertEquals(or(
        and(new NodesByPropertyPrefix("label", "", "ab"),
            new NodesByPropertyPrefix("note", "", "ab")),
        and(new NodesByPropertyPrefix("note", "", "ac"))),
        parser.apply("p.label:ab* AND p.note:ab* OR p.note:ac*"));

    assertEquals(or(and(
        new NodesByPropertyPrefix("label", "", "ab"),
        or(and(new NodesByPropertyPrefix("note", "", "ab")),
            and(new NodesByPropertyPrefix("note", "", "ac"))))),
        parser.apply("p.label:ab* AND (p.note:ab* OR p.note:ac*)"));

    assertEquals(or(
        and(new NodesByPropertyPrefix("label", "", "ab"),
            not(new NodesByPropertyPrefix("note", "", "ab"))),
        and(new NodesByPropertyPrefix("note", "", "ac"))),
        parser.apply("p.label:ab* AND NOT p.note:ab* OR p.note:ac*"));
  }

  @Test
  void shouldParseBoostQueries() {
    assertEquals(or(and(
        boost(new NodesByPropertyPrefix("label", "", "ab"), 2),
        new NodesByPropertyPrefix("note", "", "ab"))),
        parser.apply("properties.label:ab*^2 AND properties.note:ab*"));

    assertEquals(or(and(
        boost(new NodesByPropertyPrefix("label", "", "ab"), 2),
        boost(new NodesByPropertyPrefix("note", "", "ab"), 3))),
        parser.apply("properties.label:ab*^2 AND properties.note:ab*^3"));

    assertEquals(or(and(
        boost(not(new NodesByPropertyPrefix("label", "", "ab")), 2),
        boost(new NodesByPropertyPrefix("note", "", "ab"), 3))),
        parser.apply("NOT properties.label:ab*^2 AND properties.note:ab*^3"));
  }

}