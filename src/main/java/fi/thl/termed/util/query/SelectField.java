package fi.thl.termed.util.query;

public final class SelectField extends AbstractSelectQualifiedField {

  public SelectField(String field) {
    super(field);
  }

  public SelectField(String qualifier, String field) {
    super(qualifier, field);
  }

}
