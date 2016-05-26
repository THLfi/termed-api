package fi.thl.termed.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.Application;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class ResourceRepositoryTest {

  private static final String FOAF = "http://xmlns.com/foaf/0.1/";

  @javax.annotation.Resource
  private Repository<UUID, Scheme> schemeRepository;

  @javax.annotation.Resource
  private Repository<ResourceId, Resource> resourceRepository;

  @javax.annotation.Resource
  private Repository<String, User> userRepository;

  private Scheme personScheme;
  private Class personClass;
  private Class groupClass;

  private User user;
  private Date date;

  @Before
  public void setUp() {
    personClass = new Class("Person");
    personClass.setTextAttributes(newArrayList(
        new TextAttribute("firstName", FOAF + "firstName", "^\\w*$"),
        new TextAttribute("lastName", FOAF + "lastName", "^\\w*$")));
    personClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute("knows", personClass)));

    groupClass = new Class("Group");
    groupClass.setTextAttributes(newArrayList(new TextAttribute("name")));
    groupClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute("member", personClass)));

    personScheme = new Scheme(UUID.randomUUID());
    personScheme.setClasses(newArrayList(personClass, groupClass));

    schemeRepository.save(personScheme);

    date = new Date();
    user = new User("test", new BCryptPasswordEncoder().encode(UUIDs.randomUUIDString()), "ADMIN");
    userRepository.save(user);
  }

  @Test
  public void shouldSaveResource() {
    Resource bob = new Resource(personScheme, personClass, UUID.randomUUID());
    bob.addProperty("firstName", "", "Bob", "^\\w*$");
    ResourceId bobId = new ResourceId(personScheme.getId(), personClass.getId(), bob.getId());

    resourceRepository.save(bob);

    Resource tim = new Resource(personScheme, personClass, UUID.randomUUID());
    tim.addProperty("firstName", "", "Tim", "^\\w*$");
    tim.addReference("knows", bob);
    ResourceId timId = new ResourceId(personScheme.getId(), personClass.getId(), tim.getId());

    resourceRepository.save(tim);

    Resource adminGroup = new Resource(personScheme, personClass, UUID.randomUUID());
    adminGroup.addProperty("name", "", "Admins");
    adminGroup.addReference("member", tim);
    adminGroup.addReference("member", bob);
    ResourceId adminGroupId = new ResourceId(personScheme.getId(),
                                             groupClass.getId(),
                                             adminGroup.getId());

    resourceRepository.save(adminGroup);

    assertEquals(bob.getId(), resourceRepository.get(bobId).getId());

    List<Resource> timFriends = newArrayList(
        resourceRepository.get(timId).getReferences().get("knows"));
    assertEquals(bob.getId(), timFriends.get(0).getId());

    List<Resource> admins = newArrayList(
        resourceRepository.get(adminGroupId).getReferences().get("member"));
    assertEquals(tim.getId(), admins.get(0).getId());
    assertEquals(bob.getId(), admins.get(1).getId());
  }

}
