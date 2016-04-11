package fi.thl.termed.util.rdf;

public final class URIs {

  private URIs() {
  }

  public static String localName(String uri) {
    int i = uri.lastIndexOf("#");
    i = i == -1 ? uri.lastIndexOf("/") : i;
    i = i == -1 ? uri.lastIndexOf(":") : i;
    return uri.substring(i + 1);
  }

}
