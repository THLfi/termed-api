package fi.thl.termed.permission.specification;

import com.google.common.base.Objects;

import org.junit.Test;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.common.AbstractSpecification;

import static org.junit.Assert.assertEquals;

public class SpecificationPermissionEvaluatorTest {

  @Test
  public void shouldFindCorrectEvaluatorForSpecification() {
    SpecificationPermissionEvaluator<String, TestPerson> evaluator =
        new SpecificationPermissionEvaluator<String, TestPerson>();

    NameSpecPermissionEvaluator nameSpecEvaluator = new NameSpecPermissionEvaluator();
    AgeSpecPermissionEvaluator ageSpecEvaluator = new AgeSpecPermissionEvaluator();

    assertEquals(0, nameSpecEvaluator.evaluated);
    assertEquals(0, ageSpecEvaluator.evaluated);

    evaluator.registerEvaluator(NameSpec.class, nameSpecEvaluator);
    evaluator.registerEvaluator(AgeSpec.class, ageSpecEvaluator);

    evaluator.hasPermission(null, new NameSpec("test"), Permission.READ);

    assertEquals(1, nameSpecEvaluator.evaluated);
    assertEquals(0, ageSpecEvaluator.evaluated);

    evaluator.hasPermission(null, new AgeSpec(12), Permission.READ);

    assertEquals(1, nameSpecEvaluator.evaluated);
    assertEquals(1, ageSpecEvaluator.evaluated);
  }

  @Test(expected = RuntimeException.class)
  public void shouldFailIfEvaluatorForTypeIsNotFound() {
    SpecificationPermissionEvaluator<String, TestPerson> evaluator =
        new SpecificationPermissionEvaluator<String, TestPerson>();

    evaluator.registerEvaluator(NameSpec.class, new NameSpecPermissionEvaluator());

    // OK
    evaluator.hasPermission(null, new NameSpec("test"), Permission.READ);

    // Throws Exception as AgeSpecPermissionEvaluator is not registered
    evaluator.hasPermission(null, new AgeSpec(12), Permission.READ);
  }

  private class NameSpecPermissionEvaluator implements PermissionEvaluator<NameSpec, Void> {

    private int evaluated = 0;

    @Override
    public boolean hasPermission(User user, NameSpec key, Permission permission) {
      evaluated++;
      return true;
    }

    @Override
    public boolean hasPermission(User user, Void value, Permission permission) {
      throw new UnsupportedOperationException();
    }
  }

  private class AgeSpecPermissionEvaluator implements PermissionEvaluator<AgeSpec, Void> {

    private int evaluated = 0;

    @Override
    public boolean hasPermission(User user, AgeSpec key, Permission permission) {
      evaluated++;
      return true;
    }

    @Override
    public boolean hasPermission(User user, Void value, Permission permission) {
      throw new UnsupportedOperationException();
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