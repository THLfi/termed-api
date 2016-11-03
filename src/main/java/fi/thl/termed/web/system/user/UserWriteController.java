package fi.thl.termed.web.system.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/users")
public class UserWriteController {

  @Autowired
  private Service<String, User> userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody List<User> userData, @AuthenticationPrincipal User currentUser) {
    for (User userDatum : userData) {
      userService.save(new User(userDatum.getUsername(),
                                passwordEncoder.encode(userDatum.getPassword()),
                                userDatum.getAppRole(),
                                userDatum.getGraphRoles()),
                       currentUser);
    }
  }

  @PostJsonMapping(params = "batch!=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody User userData, @AuthenticationPrincipal User currentUser) {
    userService.save(new User(userData.getUsername(),
                              passwordEncoder.encode(userData.getPassword()),
                              userData.getAppRole(),
                              userData.getGraphRoles()),
                     currentUser);
  }

  @DeleteMapping("/{username}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("username") String username,
                     @AuthenticationPrincipal User user) {
    userService.delete(username, user);
  }

}
