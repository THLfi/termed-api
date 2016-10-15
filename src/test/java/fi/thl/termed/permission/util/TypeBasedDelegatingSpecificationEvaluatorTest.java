package fi.thl.termed.permission.util;

import com.google.common.base.Objects;

import org.junit.Test;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.permission.TypeBasedDelegatingSpecificationEvaluator;
import fi.thl.termed.util.specification.AbstractSpecification;

import static org.junit.Assert.assertEquals;

public class TypeBasedDelegatingSpecificationEvaluatorTest {

  @Test
  public void shouldFindCorrectEvaluatorForSpecification() {
    TypeBasedDelegatingSpecificationEvaluator<String, TestPerson> evaluator =
        new TypeBasedDelegatingSpecificationEvaluator<String, TestPerson>();

    NameSpecPermissionEvaluator nameSpecEvaluator = new NameSpecPermissionEvaluator();
    AgeSpecPermissionEvaluator ageSpecEvaluator = new AgeSpecPermissionEvaluator();

    assertEquals(0, nameSpecEvaluator.evaluatedCount);
    assertEquals(0, ageSpecEvaluator.evaluatedCount);

    evaluator.registerEvaluator(NameSpec.class, nameSpecEvaluator);
    evaluator.registerEvaluator(AgeSpec.class, ageSpecEvaluator);

    evaluator.hasPermission(null, new NameSpec("test"), Permission.READ);

    assertEquals(1, nameSpecEvaluator.evaluatedCount);
    assertEquals(0, ageSpecEvaluator.evaluatedCount);

    evaluator.hasPermission(null, new AgeSpec(12), Permission.READ);

    assertEquals(1, nameSpecEvaluator.evaluatedCount);
    assertEquals(1, ageSpecEvaluator.evaluatedCount);
  }

  @Test(expected = RuntimeException.class)
  public void shouldFailIfEvaluatorForTypeIsNotFound() {
    TypeBasedDelegatingSpecificationEvaluator<String, TestPerson> evaluator =
        new TypeBasedDelegatingSpecificationEvaluator<String, TestPerson>();

    evaluator.registerEvaluator(NameSpec.class, new NameSpecPermissionEvaluator());

    // OK
    evaluator.hasPermission(null, new NameSpec("test"), Permission.READ);

    // Throws Exception as AgeSpecPermissionEvaluator is not registered
    evaluator.hasPermission(null, new AgeSpec(12), Permission.READ);
  }

  private class NameSpecPermissionEvaluator implements PermissionEvaluator<NameSpec> {

    private int evaluatedCount = 0;

    @Override
    public boolean hasPermission(User user, NameSpec key, Permission permission) {
      evaluatedCount++;
      return true;
    }
  }

  private class AgeSpecPermissionEvaluator implements PermissionEvaluator<AgeSpec> {

    private int evaluatedCount = 0;

    @Override
    public boolean hasPermission(User user, AgeSpec key, Permission permission) {
      evaluatedCount++;
      return true;
    }
  }

  private class NameSpec extends AbstractSpecification<String, TestPerson> {

    private String name;

    public NameSpec(String name) {
      this.name = name;
    }

    @Override
    public boolean accept(String key, TestPerson value) {
      return Objects.equal(value.name, name);
    }
  }

  private class AgeSpec extends AbstractSpecification<String, TestPerson> {

    private int age;

    public AgeSpec(int age) {
      this.age = age;
    }

    @Override
    public boolean accept(String key, TestPerson value) {
      return Objects.equal(value.age, age);
    }
  }

  private class TestPerson {

    private String name;
    private int age;

  }

}
