package fi.thl.termed.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.Application;
import fi.thl.termed.domain.AppRole;
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

  @javax.annotation.Resource
  private PasswordEncoder passwordEncoder;

  private Scheme personScheme;
  private Class personClass;
  private Class groupClass;

  private User user;
  private Date date;

  @Before
  public void setUp() {
    personScheme = new Scheme(UUID.randomUUID());

    personClass = new Class(personScheme, "Person");
    personClass.setTextAttributes(newArrayList(
        new TextAttribute("firstName", FOAF + "firstName"),
        new TextAttribute("lastName", FOAF + "lastName")));
    personClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute(personClass, "knows")));

    groupClass = new Class(personScheme, "Group");
    groupClass.setTextAttributes(newArrayList(new TextAttribute("name")));
    groupClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute(personClass, "member")));

    personScheme.setClasses(newArrayList(personClass, groupClass));

    schemeRepository.save(personScheme);

    date = new Date();
    user = new User("test", passwordEncoder.encode(UUIDs.randomUUIDString()), AppRole.ADMIN);
    userRepository.save(user);
  }

  @Test
  public void shouldSaveResource() {
    Resource bob = new Resource(personScheme, personClass, UUID.randomUUID());
    bob.setCreatedDate(date);
    bob.setCreatedBy(user.getUsername());
    bob.setLastModifiedDate(date);
    bob.setLastModifiedBy(user.getUsername());
    bob.addProperty("firstName", "", "Bob");
    resourceRepository.save(bob);

    Resource tim = new Resource(personScheme, personClass, UUID.randomUUID());
    tim.setCreatedDate(date);
    tim.setCreatedBy(user.getUsername());
    tim.setLastModifiedDate(date);
    tim.setLastModifiedBy(user.getUsername());
    tim.addProperty("firstName", "", "Tim");
    tim.addReference("knows", bob);
    resourceRepository.save(tim);

    Resource adminGroup = new Resource(personScheme, groupClass, UUID.randomUUID());
    adminGroup.setCreatedDate(date);
    adminGroup.setCreatedBy(user.getUsername());
    adminGroup.setLastModifiedDate(date);
    adminGroup.setLastModifiedBy(user.getUsername());
    adminGroup.addProperty("name", "", "Admins");
    adminGroup.addReference("member", tim);
    adminGroup.addReference("member", bob);
    resourceRepository.save(adminGroup);

    assertEquals(bob.getId(), resourceRepository.get(new ResourceId(bob)).getId());

    List<Resource> timFriends = newArrayList(
        resourceRepository.get(new ResourceId(tim)).getReferences().get("knows"));
    assertEquals(bob.getId(), timFriends.get(0).getId());

    List<Resource> admins = newArrayList(
        resourceRepository.get(new ResourceId(adminGroup)).getReferences().get("member"));
    assertEquals(tim.getId(), admins.get(0).getId());
    assertEquals(bob.getId(), admins.get(1).getId());
  }

}
