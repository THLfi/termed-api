package fi.thl.termed.web.system.webhook;

import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.User;
import fi.thl.termed.domain.Webhook;
import fi.thl.termed.service.webhook.specification.WebhookByUrl;
import fi.thl.termed.util.service.Service;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hooks")
public class WebhookWriteController {

  @Autowired
  private Service<UUID, Webhook> webhookService;

  @PostMapping(params = "url")
  public UUID post(@RequestParam("url") URI url, @AuthenticationPrincipal User user) {
    try (Stream<Webhook> hooks = webhookService.getValueStream(new WebhookByUrl(url), user)) {
      return hooks.findFirst()
          .map(Webhook::getId)
          .orElseGet(() ->
              webhookService.save(new Webhook(randomUUID(), url), UPSERT, defaultOpts(), user));
    }
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
    webhookService.delete(id, defaultOpts(), user);
  }

  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public void delete(@AuthenticationPrincipal User user) {
    webhookService.delete(webhookService.getKeyStream(user).collect(toList()), defaultOpts(), user);
  }

}
