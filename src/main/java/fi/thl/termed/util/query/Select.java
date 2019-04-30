package fi.thl.termed.util.query;

public interface Select {

  static SelectAll all() {
    return new SelectAll();
  }

  static SelectField field(String field) {
    return new SelectField(field);
  }

  static SelectField qualifiedField(String qualifier, String field) {
    return new SelectField(qualifier, field);
  }

}
