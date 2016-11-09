package fi.thl.termed.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

public final class StringUtils {

  private StringUtils() {
  }

  public static String normalize(String str) {
    return Normalizer
        .normalize(str, Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "");
  }

  public static List<String> split(String query, String regex) {
    return Arrays.asList(query.split(regex));
  }

}
