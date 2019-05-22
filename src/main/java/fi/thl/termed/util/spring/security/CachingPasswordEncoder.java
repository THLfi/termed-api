package fi.thl.termed.util.spring.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cache positive matches for few seconds to speed up streams of requests with valid credentials.
 *
 * Using this may pose security risks as plain passwords are stored in memory based cache.
 */
public class CachingPasswordEncoder implements PasswordEncoder {

  private final PasswordEncoder delegate;
  private final Cache<Tuple2<CharSequence, String>, Object> validMatchesCache;

  public CachingPasswordEncoder(PasswordEncoder delegate) {
    this.delegate = delegate;
    this.validMatchesCache = CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.SECONDS)
        .build();

    // do cache cleanup on regular intervals to make sure that expired values will be removed
    Executors.newSingleThreadScheduledExecutor()
        .scheduleWithFixedDelay(validMatchesCache::cleanUp, 5, 5, TimeUnit.SECONDS);
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (validMatchesCache.getIfPresent(Tuple.of(rawPassword, encodedPassword)) != null) {
      return true;
    } else if (delegate.matches(rawPassword, encodedPassword)) {
      validMatchesCache.put(Tuple.of(rawPassword, encodedPassword), Boolean.TRUE);
      return true;
    } else {
      return false;
    }
  }

}
