package fi.thl.termed.service.webhook.internal;

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.Webhook;
import fi.thl.termed.domain.event.NodeEvent;
import fi.thl.termed.domain.event.WebEvent;
import fi.thl.termed.util.FutureUtils;
import fi.thl.termed.util.service.Service;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeEventPostingService {

  private Logger log = LoggerFactory.getLogger(getClass());
  private User eventBroadcaster = new User("httpEventBroadcaster", "", AppRole.SUPERUSER);

  private Service<UUID, Webhook> webhookService;
  private Gson gson;

  private CloseableHttpAsyncClient httpClient;
  private FutureCallback<HttpResponse> errorCallback = new ErrorLoggingCallback();

  public NodeEventPostingService(Service<UUID, Webhook> webhookService, Gson gson) {
    this.gson = gson;
    this.webhookService = webhookService;
    this.httpClient = HttpAsyncClients.createMinimal();
    httpClient.start();
  }

  @Subscribe
  public void subscribe(NodeEvent nodeEvent) {
    webhookService.getValues(eventBroadcaster).forEach(hook -> {
      HttpPost request = new HttpPost(hook.getUrl());
      request.addHeader("Content-Type", "application/json");
      request.setEntity(new StringEntity(gson.toJson(new WebEvent(nodeEvent)), UTF_8));

      Future<HttpResponse> future = httpClient.execute(request, errorCallback);

      if (nodeEvent.isSync()) {
        FutureUtils.waitFor(future, 1, TimeUnit.MINUTES,
            e -> log.warn("{} {} {}", hook, e.getClass(), e.getMessage()));
      }
    });
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
