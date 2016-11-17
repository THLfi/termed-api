package fi.thl.termed.util.spring.http;

import org.springframework.http.MediaType;

public final class MediaTypes {

  public static final String TEXT_XML_VALUE = "text/xml;charset=UTF-8";
  public static final String TEXT_CSV_VALUE = "text/csv;charset=UTF-8";

  public static final MediaType TEXT_XML = MediaType.valueOf(TEXT_XML_VALUE);
  public static final MediaType TEXT_CSV = MediaType.valueOf(TEXT_CSV_VALUE);

}
