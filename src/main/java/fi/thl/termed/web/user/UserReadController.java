package fi.thl.termed.web.user;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserReadController {

  @Autowired
  private Service2<String, User> userService;

  @GetJsonMapping
  public Stream<User> get(@AuthenticationPrincipal User currentUser) {
    return userService.values(new Query<>(new MatchAll<>()), currentUser)
        .map(u -> new User(u.getUsername(), "", u.getAppRole(), u.getGraphRoles()));
  }

  @GetJsonMapping("/{username}")
  public User get(@PathVariable("username") String username,
      @AuthenticationPrincipal User currentUser) {
    User user = userService.get(username, currentUser).orElseThrow(NotFoundException::new);
    return new User(user.getUsername(), "", user.getAppRole(), user.getGraphRoles());
  }

  @GetJsonMapping(params = "username")
  public User getByRequestParam(
      @RequestParam("username") String username,
      @AuthenticationPrincipal User currentUser) {
    User user = userService.get(username, currentUser).orElseThrow(NotFoundException::new);
    return new User(user.getUsername(), "", user.getAppRole(), user.getGraphRoles());
  }

}
