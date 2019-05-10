package fi.thl.termed.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

  private static final DateTimeFormatter LUCENE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private DateUtils() {
  }

  public static LocalDateTime parseLuceneDateString(String str) {
    return LocalDateTime.from(LUCENE_FORMATTER.parse(str));
  }

  public static String formatLuceneDateString(LocalDateTime date) {
    return LUCENE_FORMATTER.format(date);
  }

}
