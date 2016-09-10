package fi.thl.termed.repository.impl;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import javax.annotation.Resource;

import fi.thl.termed.Application;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.repository.Repository;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class ClassRepositoryIntegrationTest {

  @Resource
  private Repository<UUID, Scheme> schemeRepository;

  @Resource
  private Repository<ClassId, Class> classRepository;

  @Test
  public void shouldInsertClass() {
    Scheme testScheme = new Scheme(UUID.randomUUID());
    schemeRepository.save(testScheme);

    Class testClass = new Class(testScheme, "TestClass1");
    testClass.setTextAttributes(Lists.newArrayList(new TextAttribute(testClass, "prefLabel"),
                                                   new TextAttribute(testClass, "altLabel"),
                                                   new TextAttribute(testClass, "description")));
    testClass.setReferenceAttributes(Lists.newArrayList(
        new ReferenceAttribute(testClass, testClass, "related")));

    classRepository.save(testClass);

    Class expectedClass = schemeRepository.get(testScheme.getId()).getClasses().get(0);

    assertEquals(testClass.getId(), expectedClass.getId());

    assertEquals(testClass.getTextAttributes().get(0).getId(),
                 expectedClass.getTextAttributes().get(0).getId());
    assertEquals(testClass.getTextAttributes().get(1).getId(),
                 expectedClass.getTextAttributes().get(1).getId());
    assertEquals(testClass.getTextAttributes().get(2).getId(),
                 expectedClass.getTextAttributes().get(2).getId());

    assertEquals(testClass.getReferenceAttributes().get(0).getId(),
                 expectedClass.getReferenceAttributes().get(0).getId());
  }

  @Test
  public void shouldUpdateClass() {
    Scheme testScheme = new Scheme(UUID.randomUUID());
    schemeRepository.save(testScheme);

    Class testClass = new Class(testScheme, "TestClass2");
    testClass.setTextAttributes(
        Lists.newArrayList(new TextAttribute(testClass, "prefLabel"),
                           new TextAttribute(testClass, "altLabel")));
    testClass.setReferenceAttributes(
        Lists.newArrayList(new ReferenceAttribute(testClass, testClass, "related"),
                           new ReferenceAttribute(testClass, testClass, "broader")));

    classRepository.save(testClass);

    Class updatedTestClass = new Class(testScheme, "TestClass2");
    updatedTestClass.setTextAttributes(
        Lists.newArrayList(new TextAttribute(updatedTestClass, "prefLabel"),
                           new TextAttribute(updatedTestClass, "description")));
    updatedTestClass.setReferenceAttributes(
        Lists.newArrayList(new ReferenceAttribute(updatedTestClass, updatedTestClass, "related"),
                           new ReferenceAttribute(updatedTestClass, updatedTestClass, "narrower")));

    classRepository.save(updatedTestClass);

    Class expectedClass = schemeRepository.get(testScheme.getId()).getClasses().get(0);

    assertEquals(testClass.getId(), expectedClass.getId());

    assertEquals(updatedTestClass.getTextAttributes().get(0).getId(),
                 expectedClass.getTextAttributes().get(0).getId());
    assertEquals(updatedTestClass.getTextAttributes().get(1).getId(),
                 expectedClass.getTextAttributes().get(1).getId());

    assertEquals(updatedTestClass.getReferenceAttributes().get(0).getId(),
                 expectedClass.getReferenceAttributes().get(0).getId());
  }

}