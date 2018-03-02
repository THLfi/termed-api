package fi.thl.termed;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

  @Autowired
  private Service<String, User> userService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private Logger log = LoggerFactory.getLogger(Security.class);

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .headers().frameOptions().sameOrigin()
        .and()
        .csrf().disable()
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

  @EventListener
  public void logAuditEvents(AuditApplicationEvent event) {
    AuditEvent auditEvent = event.getAuditEvent();
    log.info("{}: {}", auditEvent.getType(), auditEvent.getPrincipal());
  }

}
