package fi.thl.termed.util.csv;

public enum CsvDelimiter {

  COMMA(','),
  SEMICOLON(';'),
  TAB('\t');

  private char value;

  CsvDelimiter(char value) {
    this.value = value;
  }

  public char value() {
    return value;
  }

}
