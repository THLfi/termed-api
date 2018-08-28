package fi.thl.termed.util;

import com.eatthepath.uuid.FastUUID;
import com.google.common.base.Charsets;

import java.util.UUID;

public final class UUIDs {

  private UUIDs() {
  }

  public static UUID fromString(String uuidString) {
    return uuidString != null ? FastUUID.parseUUID(uuidString) : null;
  }

  public static String toString(UUID  uuid) {
    return uuid != null ? FastUUID.toString(uuid) : null;
  }

  public static UUID nameUUIDFromString(String uuidString) {
    return uuidString != null ? UUID.nameUUIDFromBytes(uuidString.getBytes(Charsets.UTF_8)) : null;
  }

  public static UUID nilUuid() {
    return UUID.fromString("00000000-0000-0000-0000-000000000000");
  }

  public static String randomUUIDString() {
    return UUID.randomUUID().toString();
  }

}
