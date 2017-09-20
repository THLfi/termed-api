package fi.thl.termed.util.collect;

import java.util.Objects;

public class Arg {

  private String name;
  private Object value;

  public Arg(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public static Arg arg(String name, Object value) {
    return new Arg(name, value);
  }

  public static Arg[] args(String n0, Object v0) {
    return new Arg[]{arg(n0, v0)};
  }

  public static Arg[] args(String n0, Object v0, String n1, Object v1) {
    return new Arg[]{arg(n0, v0), arg(n1, v1)};
  }

  public static Arg[] args(String n0, Object v0, String n1, Object v1, String n2, Object v2) {
    return new Arg[]{arg(n0, v0), arg(n1, v1), arg(n2, v2)};
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Arg arg = (Arg) o;
    return Objects.equals(name, arg.name) &&
        Objects.equals(value, arg.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

}
