package fi.thl.termed.util.csv;

public enum CsvLineBreak {

  CRLF("\r\n"),
  LF("\n");

  private String value;

  CsvLineBreak(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

}
