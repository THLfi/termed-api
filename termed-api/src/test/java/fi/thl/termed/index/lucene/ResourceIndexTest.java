package fi.thl.termed.index.lucene;

import com.google.common.collect.Lists;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import fi.thl.termed.Application;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.index.Index;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.util.UUIDs;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class ResourceIndexTest {

  @javax.annotation.Resource
  private Repository<UUID, Scheme> schemeRepository;

  @javax.annotation.Resource
  private Repository<ResourceId, Resource> resourceRepository;

  @javax.annotation.Resource
  private Index<ResourceId, Resource> resourceIndex;

  @javax.annotation.Resource
  private Repository<String, User> userRepository;

  @javax.annotation.Resource
  private PasswordEncoder passwordEncoder;

  private User user;
  private Date date;

  @Before
  public void setUp() {
    date = new Date();
    user = new User("test", passwordEncoder.encode(UUIDs.randomUUIDString()), AppRole.ADMIN);
    userRepository.save(user);
  }

  @Test
  public void shouldFindResourceById() {
    Class cls = new Class("Document");
    Scheme scheme = new Scheme(UUID.randomUUID());
    scheme.setClasses(Lists.newArrayList(cls));
    schemeRepository.save(scheme);

    Resource resource = new Resource(scheme, cls, UUID.randomUUID());
    resource.setCreatedBy(user.getUsername());
    resource.setCreatedDate(date);
    resource.setLastModifiedBy(user.getUsername());
    resource.setLastModifiedDate(date);

    resourceRepository.save(resource);
    resourceIndex.reindex(new ResourceId(resource),
                          resourceRepository.get(new ResourceId(resource)));

    // force refresh and wait for completion, normally index updates are visible within one second
    ((LuceneIndex) resourceIndex).refreshBlocking();

    assertEquals(resource.getId(), resourceIndex.query(
        buildQuery("id", resource.getId().toString())).get(0).getId());
  }

  @Test
  public void shouldFindResourceByPropertyValue() {
    Class cls = new Class("Document");
    cls.setTextAttributes(Lists.newArrayList(new TextAttribute("prefLabel")));
    Scheme scheme = new Scheme(UUID.randomUUID());
    scheme.setClasses(Lists.newArrayList(cls));
    schemeRepository.save(scheme);

    Resource resource = new Resource(scheme, cls, UUID.randomUUID());
    resource.setCreatedBy(user.getUsername());
    resource.setCreatedDate(date);
    resource.setLastModifiedBy(user.getUsername());
    resource.setLastModifiedDate(date);
    resource.addProperty("prefLabel", "en", "Example Resource");

    resourceRepository.save(resource);
    resourceIndex.reindex(new ResourceId(resource),
                          resourceRepository.get(new ResourceId(resource)));

    // force refresh and wait for completion, normally index updates are visible within one second
    ((LuceneIndex) resourceIndex).refreshBlocking();

    assertEquals(resource.getId(), resourceIndex.query(
        buildQuery("prefLabel", "example")).get(0).getId());
    assertEquals(resource.getId(), resourceIndex.query(
        buildQuery("prefLabel.en", "example")).get(0).getId());
  }

  private SpecificationQuery<ResourceId, Resource> buildQuery(String field, String value) {
    return new SpecificationQuery<ResourceId, Resource>(
        new RawLuceneSpecification<ResourceId, Resource>(
            new TermQuery(new Term(field, value))));
  }

  private class RawLuceneSpecification<K extends Serializable, V>
      implements LuceneSpecification<K, V> {

    private Query query;

    public RawLuceneSpecification(Query query) {
      this.query = query;
    }

    @Override
    public Query luceneQuery() {
      return query;
    }

    @Override
    public boolean apply(@Nullable Map.Entry<K, V> input) {
      throw new UnsupportedOperationException();
    }
  }

}
