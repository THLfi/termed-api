package fi.thl.termed.web.system.webhook;

import fi.thl.termed.domain.User;
import fi.thl.termed.domain.Webhook;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hooks")
public class WebhookReadController {

  @Autowired
  private Service<UUID, Webhook> webhookService;

  @GetJsonMapping
  public List<Webhook> get(@AuthenticationPrincipal User user) {
    return webhookService.getValues(user);
  }

  @GetJsonMapping("/{id}")
  public Webhook get(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
    return webhookService.get(id, user).orElseThrow(NotFoundException::new);
  }

}
