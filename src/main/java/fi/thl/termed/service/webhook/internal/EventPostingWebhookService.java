package fi.thl.termed.service.webhook.internal;

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.WebEvent;
import fi.thl.termed.domain.Webhook;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPostingWebhookService extends ForwardingService<UUID, Webhook> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private CloseableHttpAsyncClient httpClient;
  private FutureCallback<HttpResponse> errorLoggingCallback = new ErrorLoggingCallback();

  private User eventBroadcaster = new User("httpEventBroadcaster", "", AppRole.SUPERUSER);
  private Gson gson;

  public EventPostingWebhookService(Service<UUID, Webhook> delegate, Gson gson) {
    super(delegate);
    this.gson = gson;
    this.httpClient = HttpAsyncClients.createMinimal();
    httpClient.start();
  }

  /**
   * Listen for all events and send them to registered listeners (webhooks) via async http client.
   */
  @Subscribe
  private void subscribeForAllEvents(Object event) throws UnsupportedEncodingException {
    StringEntity body = new StringEntity(
        gson.toJson(new WebEvent(new Date(), event.getClass().getSimpleName(), event)), UTF_8);

    for (Webhook hook : get(eventBroadcaster)) {
      HttpPost request = new HttpPost(hook.getUrl());
      request.addHeader("Content-Type", "application/json");
      request.setEntity(body);
      httpClient.execute(request, errorLoggingCallback);
    }
  }

  /**
   * Logs failed http requests.
   */
  private class ErrorLoggingCallback implements FutureCallback<HttpResponse> {

    @Override
    public void completed(HttpResponse response) {
    }

    @Override
    public void failed(Exception e) {
      log.warn(e.getMessage());
    }

    @Override
    public void cancelled() {
    }

  }

}
