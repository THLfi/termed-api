package fi.thl.termed.repository;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import fi.thl.termed.Application;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.LangValue;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class SchemeRepositoryIntegrationTest {

  @Resource
  private Repository<UUID, Scheme> schemeRepository;

  private User testAdmin = new User("testAdmin", "", AppRole.ADMIN);

  @Test
  public void shouldSaveSchemeWithProperties() {
    Scheme scheme = new Scheme(UUID.randomUUID());
    scheme.setProperties(ImmutableMultimap.of(
        "prefLabel", new LangValue("en", "Scheme label 0"),
        "prefLabel", new LangValue("en", "Scheme label 1"),
        "prefLabel", new LangValue("en", "Scheme label 2")));

    schemeRepository.save(scheme, testAdmin);

    Scheme savedScheme = schemeRepository.get(scheme.getId(), testAdmin).get();

    assertEquals(scheme.getId(), savedScheme.getId());

    List<LangValue> langValues = Lists.newArrayList(savedScheme.getProperties().get("prefLabel"));
    assertEquals(new LangValue("en", "Scheme label 0"), langValues.get(0));
    assertEquals(new LangValue("en", "Scheme label 1"), langValues.get(1));
    assertEquals(new LangValue("en", "Scheme label 2"), langValues.get(2));
  }

  @Test
  public void shouldUpdateSchemeWithProperties() {
    Scheme scheme = new Scheme(UUID.randomUUID());
    scheme.setProperties(ImmutableMultimap.of(
        "prefLabel", new LangValue("en", "Scheme label 0"),
        "prefLabel", new LangValue("en", "Scheme label 1"),
        "prefLabel", new LangValue("en", "Scheme label 2")));

    schemeRepository.save(scheme, testAdmin);

    scheme.setProperties(ImmutableMultimap.of(
        "prefLabel", new LangValue("en", "Scheme label 0 updated"),
        "prefLabel", new LangValue("en", "Scheme label 2")));

    schemeRepository.save(scheme, testAdmin);

    Scheme updated = schemeRepository.get(scheme.getId(), testAdmin).get();

    List<LangValue> langValues = Lists.newArrayList(updated.getProperties().get("prefLabel"));
    assertEquals(2, langValues.size());
    assertEquals(new LangValue("en", "Scheme label 0 updated"), langValues.get(0));
    assertEquals(new LangValue("en", "Scheme label 2"), langValues.get(1));
  }

}
