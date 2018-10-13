package fi.thl.termed.util.dao;

import static fi.thl.termed.util.dao.CachedSystemDao.cache;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CachedSystemDaoTest {

  @Test
  void shouldCacheGet() {
    @SuppressWarnings("unchecked")
    SystemDao<String, String> dao = mock(SystemDao.class);

    when(dao.get("foo")).thenReturn(Optional.of("bar"));

    SystemDao<String, String> cachedDao = cache(dao);

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    assertEquals(Optional.of("bar"), dao.get("foo"));
    verify(dao, times(2)).get("foo");
  }

  @Test
  void shouldInvalidateCacheOnEvent() {
    @SuppressWarnings("unchecked")
    SystemDao<String, String> dao = mock(SystemDao.class);

    when(dao.get("foo")).thenReturn(Optional.of("bar"));

    EventBus eventBus = new EventBus();

    SystemDao<String, String> cachedDao = cache(dao);
    eventBus.register(cachedDao);

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    eventBus.post(new InvalidateCachesEvent());
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));

    eventBus.post(new InvalidateCachesEvent());
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));

    eventBus.post(new InvalidateCachesEvent());
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));

    verify(dao, times(4)).get("foo");
  }

}