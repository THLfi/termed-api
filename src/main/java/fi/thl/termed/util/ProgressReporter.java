package fi.thl.termed.util;

import org.slf4j.Logger;

public class ProgressReporter {

  private Logger log;
  private String operationName;

  private int total;
  private int processed;
  private int reportInterval;

  private long startTime;

  private int lastUpdateProcessed;
  private long lastUpdateTime;

  public ProgressReporter(Logger log, String operationName, int reportInterval, int total) {
    this.log = log;
    this.operationName = operationName;
    this.reportInterval = reportInterval;
    this.total = total;

    this.startTime = System.nanoTime();
    this.lastUpdateTime = startTime;
    this.processed = 0;
    this.lastUpdateProcessed = 0;
  }

  public void tick() {
    processed++;
    if (processed % reportInterval == 0) {
      report();
    }
  }

  public void report() {
    log.trace("{} {}% - {}/{} - {} in {} ms - total time {} seconds",
        operationName, percentageDone(), processed, total,
        processedSinceLastUpdate(),
        timeSinceLastUpdate(),
        totalTimeInSeconds());

    lastUpdateProcessed = processed;
    lastUpdateTime = System.nanoTime();
  }

  private int percentageDone() {
    return (processed * 100) / total;
  }

  private int processedSinceLastUpdate() {
    return processed - lastUpdateProcessed;
  }

  private long timeSinceLastUpdate() {
    return (System.nanoTime() - lastUpdateTime) / 1000000;
  }

  private long totalTimeInSeconds() {
    return (System.nanoTime() - startTime) / 1000000000;
  }

}
