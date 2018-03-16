package fi.thl.termed.util;

import com.google.common.eventbus.EventBus;

public final class EventBusUtils {

  private EventBusUtils() {
  }

  public static <T> T register(EventBus eventBus, T object) {
    eventBus.register(object);
    return object;
  }

}
