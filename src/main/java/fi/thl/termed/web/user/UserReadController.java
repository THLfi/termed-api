package fi.thl.termed.web.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserReadController {

  @Autowired
  private Service<String, User> userService;

  @GetJsonMapping
  public List<User> get(@AuthenticationPrincipal User currentUser) {
    return userService.get(new Query<>(new MatchAll<>()), currentUser).getValues();
  }

  @GetJsonMapping("/{username}")
  public User get(@PathVariable("username") String username,
                  @AuthenticationPrincipal User currentUser) {
    return userService.get(username, currentUser).orElseThrow(NotFoundException::new);
  }

}
