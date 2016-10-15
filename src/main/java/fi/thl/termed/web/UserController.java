package fi.thl.termed.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.util.TrueSpecification;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/api/users")
public class UserController {

  private Service<String, User> userService;
  private PasswordEncoder passwordEncoder;

  public UserController(Service<String, User> userService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
  }

  @RequestMapping(method = GET, produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<User> get(@AuthenticationPrincipal User currentUser) {
    return userService.get(
        new SpecificationQuery<String, User>(new TrueSpecification<String, User>()), currentUser);
  }

  @RequestMapping(method = GET, value = "/{username}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public User get(@PathVariable("username") String username,
                  @AuthenticationPrincipal User currentUser) {
    return userService.get(username, currentUser).orNull();
  }

  @RequestMapping(method = POST, consumes = "application/json;charset=UTF-8", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody List<User> userData, @AuthenticationPrincipal User currentUser) {
    for (User userDatum : userData) {
      userService.save(new User(userDatum.getUsername(),
                                passwordEncoder.encode(userDatum.getPassword()),
                                userDatum.getAppRole(),
                                userDatum.getSchemeRoles()),
                       currentUser);
    }
  }

  @RequestMapping(method = POST, consumes = "application/json;charset=UTF-8", params = "batch!=true")
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody User userData, @AuthenticationPrincipal User currentUser) {
    userService.save(new User(userData.getUsername(),
                              passwordEncoder.encode(userData.getPassword()),
                              userData.getAppRole(),
                              userData.getSchemeRoles()),
                     currentUser);
  }

  @RequestMapping(method = DELETE, value = "/{username}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("username") String username,
                     @AuthenticationPrincipal User user) {
    userService.delete(username, user);
  }

}
