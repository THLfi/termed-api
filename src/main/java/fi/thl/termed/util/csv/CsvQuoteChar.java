package fi.thl.termed.util.csv;

public enum CsvQuoteChar {

  DOUBLE_QUOTE('\"'),
  SINGLE_QUOTE('\'');

  private char value;

  CsvQuoteChar(char value) {
    this.value = value;
  }

  public char value() {
    return value;
  }

}
