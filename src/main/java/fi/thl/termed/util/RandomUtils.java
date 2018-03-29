package fi.thl.termed.util;

import com.google.common.base.Preconditions;
import java.security.SecureRandom;

public final class RandomUtils {

  private static SecureRandom random = new SecureRandom();
  private static char[] alphanumerics =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

  private RandomUtils() {
  }

  public static String randomAlphanumericString(int length) {
    return randomString(alphanumerics, length);
  }

  public static String randomString(char[] symbols, int length) {
    Preconditions.checkArgument(length > 0);
    char[] result = new char[length];
    for (int i = 0; i < length; i++) {
      result[i] = symbols[random.nextInt(symbols.length)];
    }
    return new String(result);
  }

}
