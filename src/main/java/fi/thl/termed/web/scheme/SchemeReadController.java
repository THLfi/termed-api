package fi.thl.termed.web.scheme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

/**
 * SchemeService published as a JSON/REST service.
 */
@RestController
@RequestMapping("/api/schemes")
public class SchemeReadController {

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @GetJsonMapping
  public List<Scheme> getSchemes(@AuthenticationPrincipal User user) {
    return schemeService.get(new Query<>(new MatchAll<>()), user);
  }

  @GetJsonMapping("/{schemeId}")
  public Scheme getScheme(@PathVariable("schemeId") UUID schemeId,
                          @AuthenticationPrincipal User user) {
    return schemeService.get(new SchemeId(schemeId), user).orElseThrow(NotFoundException::new);
  }

}
