package fi.thl.termed.web.admin;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeAdminController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetJsonMapping(path = "/nodes", params = "bypassIndex=true")
  public Stream<Node> dumpAllNodesFromDb(@AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER || user.getAppRole() == AppRole.ADMIN) {
      log.warn("Dumping all nodes from the database (user: {})", user.getUsername());
      return nodeService.values(new Query<>(new MatchAll<>()), user);
    } else {
      throw new AccessDeniedException("");
    }
  }

}
