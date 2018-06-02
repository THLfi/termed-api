package fi.thl.termed.web.user;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.BadRequestException;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserWriteController {

  @Autowired
  private Service2<String, User> userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody Stream<User> userData,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User currentUser) {
    userService.save(userData.map(user ->
        new User(user.getUsername(),
            passwordEncoder.encode(user.getPassword()),
            user.getAppRole(),
            user.getGraphRoles())), saveMode(mode), opts(sync), currentUser);
  }

  @PostJsonMapping(params = "batch!=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody User userData,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "updatePassword", defaultValue = "true") boolean updatePassword,
      @AuthenticationPrincipal User currentUser) {

    User user = new User(userData.getUsername(),
        updatePassword ?
            passwordEncoder.encode(userData.getPassword()) :
            userService.get(userData.getUsername(), currentUser)
                .orElseThrow(BadRequestException::new)
                .getPassword(),
        userData.getAppRole(),
        userData.getGraphRoles());

    userService.save(user, saveMode(mode), opts(sync), currentUser);
  }

  @DeleteMapping("/{username}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("username") String username,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    userService.delete(username, opts(sync), user);
  }

  @DeleteMapping(params = "username")
  @ResponseStatus(NO_CONTENT)
  public void deleteByRequestParam(@RequestParam("username") String username,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    userService.delete(username, opts(sync), user);
  }

}
