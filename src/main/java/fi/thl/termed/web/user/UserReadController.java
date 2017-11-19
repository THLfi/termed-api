package fi.thl.termed.web.user;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
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
  private Service<String, User> userService;

  @GetJsonMapping
  public List<User> get(@AuthenticationPrincipal User currentUser) {
    return userService.getValues(currentUser).stream()
        .map(user -> new User(user.getUsername(), "", user.getAppRole(), user.getGraphRoles()))
        .collect(toList());
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
