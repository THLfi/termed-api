package fi.thl.termed.repository.lucene;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.UUID;

import fi.thl.termed.Application;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.ResourceIndex;
import fi.thl.termed.repository.ResourceRepository;
import fi.thl.termed.repository.SchemeRepository;
import fi.thl.termed.repository.UserRepository;
import fi.thl.termed.util.UUIDs;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class ResourceIndexTest {

  @Autowired
  private SchemeRepository schemeRepository;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private ResourceIndex resourceIndex;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private Date date;

  @Before
  public void setUp() {
    date = new Date();
    user = new User("test", new BCryptPasswordEncoder().encode(UUIDs.randomUUIDString()), "ADMIN");
    userRepository.save(user);
  }

  @Test
  public void shouldFindResourceById() {
    UUID schemeId = UUID.randomUUID();
    String classId = "Document";

    Class cls = new Class(classId);
    Scheme scheme = new Scheme(schemeId);
    scheme.setClasses(Lists.newArrayList(cls));
    schemeRepository.save(scheme);

    ResourceId resourceId = new ResourceId(schemeId, classId, UUID.randomUUID());
    Resource resource = new Resource(resourceId.getId(), user.getUsername(), date);
    resource.setScheme(scheme);
    resource.setType(cls);

    resourceRepository.save(resource);
    resourceIndex.reindex(resourceId, resourceRepository.get(resourceId));

    // force refresh and wait for completion, normally index updates are visible within one second
    ((LuceneResourceIndex) resourceIndex).refreshBlocking();

    assertEquals(resource.getId(), resourceIndex.query(
        new Query("id:" + resource.getId())).get(0).getId());
  }

  @Test
  public void shouldFindResourceByPropertyValue() {
    UUID schemeId = UUID.randomUUID();
    String classId = "Document";

    Class cls = new Class(classId);
    cls.setTextAttributes(Lists.newArrayList(new TextAttribute("prefLabel")));
    Scheme scheme = new Scheme(schemeId);
    scheme.setClasses(Lists.newArrayList(cls));
    schemeRepository.save(scheme);

    ResourceId resourceId = new ResourceId(schemeId, classId, UUID.randomUUID());
    Resource resource = new Resource(resourceId.getId(), user.getUsername(), date);
    resource.setScheme(scheme);
    resource.setType(cls);
    resource.addProperty("prefLabel", "en", "Example Resource");

    resourceRepository.save(resource);
    resourceIndex.reindex(resourceId, resourceRepository.get(resourceId));

    // force refresh and wait for completion, normally index updates are visible within one second
    ((LuceneResourceIndex) resourceIndex).refreshBlocking();

    assertEquals(resource.getId(), resourceIndex.query(
        new Query("example")).get(0).getId());
    assertEquals(resource.getId(), resourceIndex.query(
        new Query("prefLabel:example")).get(0).getId());
    assertEquals(resource.getId(), resourceIndex.query(
        new Query("prefLabel.en:example")).get(0).getId());
  }

}
