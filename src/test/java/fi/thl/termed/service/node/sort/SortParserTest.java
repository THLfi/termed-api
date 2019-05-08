package fi.thl.termed.service.node.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.util.query.Sort;
import java.util.List;
import org.junit.jupiter.api.Test;

class SortParserTest {

  private SortParser sp = new SortParser();

  @Test
  void shouldParseSortByNumber() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("number"));

    assertEquals(sortPrefLabelDesc, sp.apply("n"));
    assertEquals(sortPrefLabelDesc, sp.apply("n asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n+asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number"));
    assertEquals(sortPrefLabelDesc, sp.apply("number asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number+asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n.sortable"));
    assertEquals(sortPrefLabelDesc, sp.apply("n.sortable asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n.sortable+asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number.sortable"));
    assertEquals(sortPrefLabelDesc, sp.apply("number.sortable asc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number.sortable+asc"));
  }

  @Test
  void shouldParseSortByNumberDesc() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("number", true));

    assertEquals(sortPrefLabelDesc, sp.apply("n desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("n.sortable+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("number.sortable+desc"));
  }

  @Test
  void shouldParseSortByCreatedDate() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("createdDate"));

    assertEquals(sortPrefLabelDesc, sp.apply("createdDate"));
    assertEquals(sortPrefLabelDesc, sp.apply("createdDate.sortable"));
  }

  @Test
  void shouldParseSortByCreatedDateDesc() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("createdDate", true));

    assertEquals(sortPrefLabelDesc, sp.apply("createdDate desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("createdDate+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("createdDate.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("createdDate.sortable+desc"));
  }

  @Test
  void shouldParseSortByLastModifiedDate() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("lastModifiedDate"));

    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate"));
    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate.sortable"));
  }

  @Test
  void shouldParseSortByLastModifiedDateDesc() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("lastModifiedDate", true));

    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("lastModifiedDate.sortable+desc"));
  }

  @Test
  void shouldParseSortProperty() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("properties.prefLabel"));

    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel"));
    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel.sortable"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel.sortable"));
  }

  @Test
  void shouldParseSortPropertyLocalized() {
    List<Sort> sortPrefLabelFiDesc = ImmutableList.of(Sort.sort("properties.prefLabel.fi"));

    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi.sortable"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi.sortable"));
  }

  @Test
  void shouldParseSortPropertyDesc() {
    List<Sort> sortPrefLabelDesc = ImmutableList.of(Sort.sort("properties.prefLabel", true));

    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("p.prefLabel.sortable+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel+desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel.sortable desc"));
    assertEquals(sortPrefLabelDesc, sp.apply("properties.prefLabel.sortable+desc"));
  }

  @Test
  void shouldParseSortPropertyLocalizedDesc() {
    List<Sort> sortPrefLabelFiDesc = ImmutableList.of(Sort.sort("properties.prefLabel.fi", true));

    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi+desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi.sortable desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("p.prefLabel.fi.sortable+desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi+desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi.sortable desc"));
    assertEquals(sortPrefLabelFiDesc, sp.apply("properties.prefLabel.fi.sortable+desc"));
  }

}
