package fi.thl.termed.util.dao;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachedSystemDaoTest {

  @Test
  public void shouldCacheGet() {
    @SuppressWarnings("unchecked")
    SystemDao<String, String> dao = mock(SystemDao.class);

    when(dao.get("foo")).thenReturn(Optional.of("bar"));

    SystemDao<String, String> cachedDao = new CachedSystemDao<>(dao);

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    assertEquals(Optional.of("bar"), cachedDao.get("foo"));
    verify(dao, times(1)).get("foo");

    assertEquals(Optional.of("bar"), dao.get("foo"));
    verify(dao, times(2)).get("foo");
  }

}