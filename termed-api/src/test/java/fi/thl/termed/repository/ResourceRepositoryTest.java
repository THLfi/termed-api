package fi.thl.termed.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private SchemeRepository schemeRepo;

  @Autowired
  private ResourceRepository repo;

  @Autowired
  private UserRepository userRepository;

  private Scheme personScheme;
  private Class personClass;
  private Class groupClass;

  private User user;
  private Date date;

  @Before
  public void setUp() {
    personClass = new Class("Person");
    personClass.setTextAttributes(newArrayList(
        new TextAttribute("firstName"), new TextAttribute("lastName")));
    personClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute("knows", personClass)));

    groupClass = new Class("Group");
    groupClass.setTextAttributes(newArrayList(new TextAttribute("name")));
    groupClass.setReferenceAttributes(newArrayList(
        new ReferenceAttribute("member", personClass)));

    personScheme = new Scheme(UUID.randomUUID());
    personScheme.setClasses(newArrayList(personClass, groupClass));

    schemeRepo.save(personScheme.getId(), personScheme);

    date = new Date();
    user = new User("test", new BCryptPasswordEncoder().encode(UUIDs.randomUUIDString()), "ADMIN");
    userRepository.save(user.getUsername(), user);
  }

  @Test
  public void shouldSaveResource() {
    Resource bob = new Resource(UUID.randomUUID(), user.getUsername(), date);
    bob.setScheme(personScheme);
    bob.setType(personClass);
    bob.addProperty("firstName", "", "Bob");
    ResourceId bobId = new ResourceId(personScheme.getId(), personClass.getId(), bob.getId());

    repo.save(bobId, bob);

    Resource tim = new Resource(UUID.randomUUID(), user.getUsername(), date);
    tim.setScheme(personScheme);
    tim.setType(personClass);
    tim.addProperty("firstName", "", "Tim");
    tim.addReference("knows", bob);
    ResourceId timId = new ResourceId(personScheme.getId(), personClass.getId(), tim.getId());

    repo.save(timId, tim);

    Resource adminGroup = new Resource(UUID.randomUUID(), user.getUsername(), date);
    adminGroup.setScheme(personScheme);
    adminGroup.setType(groupClass);
    adminGroup.addProperty("name", "", "Admins");
    adminGroup.addReference("member", tim);
    adminGroup.addReference("member", bob);
    ResourceId adminGroupId = new ResourceId(personScheme.getId(),
                                             groupClass.getId(),
                                             adminGroup.getId());

    repo.save(adminGroupId, adminGroup);

    assertEquals(bob.getId(), repo.get(bobId).getId());

    List<Resource> timFriends = newArrayList(repo.get(timId).getReferences().get("knows"));
    assertEquals(bob.getId(), timFriends.get(0).getId());

    List<Resource> admins = newArrayList(repo.get(adminGroupId).getReferences().get("member"));
    assertEquals(tim.getId(), admins.get(0).getId());
    assertEquals(bob.getId(), admins.get(1).getId());
  }

}
