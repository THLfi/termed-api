package fi.thl.termed.util.query;

public interface Select {

  static SelectAll all() {
    return new SelectAll();
  }

  static SelectField field(String field) {
    return new SelectField(field);
  }

}
