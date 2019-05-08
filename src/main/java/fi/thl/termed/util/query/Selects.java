package fi.thl.termed.util.query;

public final class Selects {

  private Selects() {
  }

  public static SelectAll all() {
    return new SelectAll();
  }

  public static SelectField field(String field) {
    return new SelectField(field);
  }

}
