package fi.thl.termed.web.admin;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CacheController {

  @Autowired
  private EventBus eventBus;

  @DeleteMapping("/caches")
  @ResponseStatus(NO_CONTENT)
  public void invalidateCaches(@AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      eventBus.post(new InvalidateCachesEvent());
    }  else {
      throw new AccessDeniedException("");
    }
  }

}
