package fi.thl.termed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class URIsTest {

  @Test
  void shouldFindLocalName() {
    assertEquals("type", URIs.localName("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
    assertEquals("name", URIs.localName("http://xmlns.com/foaf/0.1/name"));
    assertEquals("13", URIs.localName("nasa:access:13"));
    assertEquals("test", URIs.localName("test"));
  }

}