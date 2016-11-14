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

  public static List<String> tokenize(String str) {
    return split(str, "\\s");
  }

  public static List<String> split(String str, String regex) {
    return Arrays.asList(str.split(regex));
  }

}
