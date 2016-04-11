package fi.thl.termed.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

public final class ResourceUtils {

  private ResourceUtils() {
  }

  public static String getResourceToString(String resourceName) {
    try {
      return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
