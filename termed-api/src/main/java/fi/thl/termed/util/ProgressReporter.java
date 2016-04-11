package fi.thl.termed.util;

import org.slf4j.Logger;

public class ProgressReporter {

  private Logger log;
  private String message;

  private int total;
  private int processed;
  private int reportInterval;

  private long startTime;

  private int lastUpdateProcessed;
  private long lastUpdateTime;

  public ProgressReporter(Logger log, String message, int reportInterval, int total) {
    this.total = total;
    this.reportInterval = reportInterval;

    this.startTime = System.nanoTime();
    this.lastUpdateTime = startTime;

    this.processed = 0;
    this.lastUpdateProcessed = 0;

    this.message = message;
    this.log = log;
  }

  public void tick() {
    processed++;
    if (processed % reportInterval == 0) {
      report();
    }
  }

  public void report() {
    log.info("{} {}% - {}/{} - {} in {} ms - total time {} seconds",
             message, percentageDone(), processed, total,
             processedSinceLastUpdate(), timePassedSinceLastUpdate(), totalTimePassedInSeconds());

    lastUpdateProcessed = processed;
    lastUpdateTime = System.nanoTime();
  }

  private int processedSinceLastUpdate() {
    return processed - lastUpdateProcessed;
  }

  public int percentageDone() {
    return (processed * 100) / total;
  }

  public long totalTimePassedInSeconds() {
    return (System.nanoTime() - startTime) / 1000000000;
  }

  public long timePassedSinceLastUpdate() {
    return (System.nanoTime() - lastUpdateTime) / 1000000;
  }

}
