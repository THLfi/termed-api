package fi.thl.termed.util.service;

public enum SaveMode {

  INSERT, UPDATE, UPSERT;

  public static SaveMode saveMode(String mode) {
    return valueOf(mode.toUpperCase());
  }

}
