package fi.thl.termed.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.UUID;

import fi.thl.termed.Application;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class ClassRepositoryTest {

  @Autowired
  private SchemeRepository schemeRepository;

  @Autowired
  private ClassRepository classRepository;

  @Test
  public void shouldInsertClass() {
    Scheme testScheme = new Scheme(UUID.randomUUID());
    schemeRepository.save(testScheme.getId(), testScheme);

    Class testClass = new Class("TestClass1");
    testClass.setTextAttributes(Lists.newArrayList(new TextAttribute("prefLabel"),
                                                   new TextAttribute("altLabel"),
                                                   new TextAttribute("description")));
    testClass.setReferenceAttributes(Lists.newArrayList(
        new ReferenceAttribute("related", testClass)));

    classRepository.insert(
        singletonMap(new ClassId(testScheme.getId(), testClass.getId()), testClass));

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
    schemeRepository.save(testScheme.getId(), testScheme);

    Class testClass = new Class("TestClass2");
    testClass.setTextAttributes(
        Lists.newArrayList(new TextAttribute("prefLabel"),
                           new TextAttribute("altLabel")));
    testClass.setReferenceAttributes(
        Lists.newArrayList(new ReferenceAttribute("related", testClass),
                           new ReferenceAttribute("broader", testClass)));
    Map<ClassId, Class> classMap = singletonMap(
        new ClassId(testScheme.getId(), testClass.getId()), testClass);

    classRepository.insert(classMap);

    Class updatedTestClass = new Class("TestClass2");
    updatedTestClass.setTextAttributes(
        Lists.newArrayList(new TextAttribute("prefLabel"),
                           new TextAttribute("description")));
    updatedTestClass.setReferenceAttributes(
        Lists.newArrayList(new ReferenceAttribute("related", testClass),
                           new ReferenceAttribute("narrower", testClass)));
    Map<ClassId, Class> updatedClassMap = singletonMap(
        new ClassId(testScheme.getId(), testClass.getId()), updatedTestClass);

    classRepository.update(
        Maps.difference(updatedClassMap, classMap).entriesDiffering());

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