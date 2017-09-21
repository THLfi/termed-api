package fi.thl.termed.util.service;

public class WriteOptions {

  private boolean sync;

  public WriteOptions() {
    this(false);
  }

  public WriteOptions(boolean sync) {
    this.sync = sync;
  }

  public static WriteOptions opts(boolean sync) {
    return new WriteOptions(sync);
  }

  public static WriteOptions defaultOpts() {
    return new WriteOptions();
  }

  public boolean isSync() {
    return sync;
  }

}
