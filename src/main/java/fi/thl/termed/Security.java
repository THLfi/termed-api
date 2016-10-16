package fi.thl.termed;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import javax.annotation.Resource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Security extends WebSecurityConfigurerAdapter {

  @Resource
  private Service<String, User> userService;
  @Resource
  private PasswordEncoder passwordEncoder;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .httpBasic();
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    UserDetailsService userDetailsService = username -> {
      Optional<User> user = userService.get(
          username, new User("authenticator", "", AppRole.SUPERUSER));
      return user.orElseThrow(() -> new UsernameNotFoundException(""));
    };

    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  }

}
