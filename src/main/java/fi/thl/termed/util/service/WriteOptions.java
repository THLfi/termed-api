package fi.thl.termed.util.service;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.Optional;

public final class WriteOptions {

  private final Boolean sync;
  private final Long revision;
  private final Boolean generateCodes;
  private final Boolean generateUris;

  private WriteOptions(Boolean sync, Long revision, Boolean generateCodes, Boolean generateUris) {
    this.sync = requireNonNull(sync);
    this.revision = revision;
    this.generateCodes = requireNonNull(generateCodes);
    this.generateUris = requireNonNull(generateUris);
  }

  public static WriteOptions opts(Boolean sync, Long revision,
      Boolean generateCodes, Boolean generateUris) {
    return new WriteOptions(sync, revision, generateCodes, generateUris);
  }

  public static WriteOptions opts(Boolean sync, Long revision) {
    return new WriteOptions(sync, revision, false, false);
  }

  public static WriteOptions opts(Boolean sync, Boolean generateCodes, Boolean generateUris) {
    return new WriteOptions(sync, null, generateCodes, generateUris);
  }

  public static WriteOptions opts(Boolean sync) {
    return new WriteOptions(sync, null, false, false);
  }

  public static WriteOptions opts(Long revision) {
    return new WriteOptions(false, revision, false, false);
  }

  public static WriteOptions defaultOpts() {
    return new WriteOptions(false, null, false, false);
  }

  public Boolean isSync() {
    return sync;
  }

  public Optional<Long> getRevision() {
    return ofNullable(revision);
  }

  public Boolean isGenerateCodes() {
    return generateCodes;
  }

  public Boolean isGenerateUris() {
    return generateUris;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WriteOptions that = (WriteOptions) o;
    return Objects.equals(sync, that.sync) &&
        Objects.equals(revision, that.revision) &&
        Objects.equals(generateCodes, that.generateCodes) &&
        Objects.equals(generateUris, that.generateUris);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sync, revision, generateCodes, generateUris);
  }

}
