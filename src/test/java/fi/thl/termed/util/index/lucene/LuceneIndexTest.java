package fi.thl.termed.util.index.lucene;

import static fi.thl.termed.util.collect.StreamUtils.findFirstAndClose;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LuceneIndexTest {

  private LuceneIndex<Integer, TestObject> index;

  @BeforeEach
  void setUp() {
    this.index = new LuceneIndex<>(
        "",
        new JsonStringConverter<>(Integer.class),
        new JsonDocumentConverter<>(new Gson(), TestObject.class));

    index.index(1, new TestObject(1, "First", "This is an example body about dogs"));
    index.index(2, new TestObject(2, "Second", "This is an example body about cats"));
    index.index(3, new TestObject(3, "Third", "This is an example body about horses"));

    index.refreshBlocking();
  }

  @Test
  void shouldFindById() {
    assertEquals("This is an example body about cats",
        findFirstAndClose(index.get(term("id", "2"), null, -1))
            .orElseThrow(AssertionError::new)
            .body);
  }

  @Test
  void shouldFindByContents() {
    assertEquals("First",
        findFirstAndClose(index.get(term("title", "first"), null, -1))
            .orElseThrow(AssertionError::new)
            .title);

    assertEquals(new Integer(3),
        findFirstAndClose(index.get(term("body", "horses"), null, -1))
            .orElseThrow(AssertionError::new)
            .id);
  }

  @Test
  void shouldNotFindDeleted() {
    assertEquals(new Integer(3),
        findFirstAndClose(index.get(term("body", "horses"), null, -1))
            .orElseThrow(AssertionError::new)
            .id);

    index.delete(3);
    index.refreshBlocking();

    assertEquals(0, index.count(term("body", "horses")));
  }

  private <K extends Serializable, V> Specification<K, V> term(String field, String value) {
    return new RawLuceneSpecification<>(new TermQuery(new Term(field, value)));
  }

  private class TestObject {

    private Integer id;
    private String title;
    private String body;

    TestObject(Integer id, String title, String body) {
      this.id = id;
      this.title = title;
      this.body = body;
    }

  }

  private class RawLuceneSpecification<K extends Serializable, V>
      implements LuceneSpecification<K, V> {

    private org.apache.lucene.search.Query query;

    RawLuceneSpecification(org.apache.lucene.search.Query query) {
      this.query = query;
    }

    @Override
    public org.apache.lucene.search.Query luceneQuery() {
      return query;
    }

    @Override
    public boolean test(K k, V v) {
      throw new UnsupportedOperationException();
    }
  }

}
