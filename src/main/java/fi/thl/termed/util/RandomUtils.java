package fi.thl.termed.util;

import com.google.common.base.Preconditions;
import java.util.Random;

public final class RandomUtils {

  private static Random random = new Random();
  private static char[] alphanumerics =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

  private RandomUtils() {
  }

  public static String randomAlphanumericString(int length) {
    Preconditions.checkArgument(length > 0);
    char[] result = new char[length];
    for (int i = 0; i < length; i++) {
      result[i] = alphanumerics[random.nextInt(alphanumerics.length)];
    }
    return new String(result);
  }

}
