package fi.thl.termed.util.service;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;

public class WriteOptions {

  private final Boolean sync;
  private final Long revision;

  public WriteOptions(Boolean sync, Long revision) {
    this.sync = requireNonNull(sync);
    this.revision = revision;
  }

  public static WriteOptions opts(Boolean sync, Long revision) {
    return new WriteOptions(sync, revision);
  }

  public static WriteOptions opts(Boolean sync) {
    return new WriteOptions(sync, null);
  }

  public static WriteOptions defaultOpts() {
    return new WriteOptions(false, null);
  }

  public Boolean isSync() {
    return sync;
  }

  public Optional<Long> getRevision() {
    return ofNullable(revision);
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
        Objects.equals(revision, that.revision);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sync, revision);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("sync", sync)
        .add("revision", revision)
        .toString();
  }

}
