package fi.thl.termed.util;

import java.text.Normalizer;

public final class StringUtils {

  private StringUtils() {
  }

  public static String normalize(String str) {
    return Normalizer
        .normalize(str, Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "");
  }

}
