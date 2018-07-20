package fi.thl.termed.util.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Maps;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

public class SecureDaoTest {

  private User dummyUser = new User("testUser", "testUser", AppRole.USER);

  @Test
  public void shouldTreatSecretReadDataAsNonexistent() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("secret_greeting", "Hi!");

    Dao2<String, String> secureDao = new AuthorizedDao2<>(
        new MemoryBasedSystemDao2<>(data),
        (user, key, permission) -> !key.startsWith("secret_"));

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertFalse(secureDao.exists("secret_greeting", dummyUser));
    assertFalse(secureDao.exists("nonexistent_greeting", dummyUser));

    assertEquals(data.get("greeting"), secureDao.get("greeting", dummyUser).get());
    assertEquals(Optional.empty(), secureDao.get("secret_greeting", dummyUser));
    assertEquals(Optional.empty(), secureDao.get("nonexistent_greeting", dummyUser));
  }

  @Test
  public void shouldIgnoreModificationsOnWriteProtectedData() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("locked_greeting", "Good day");

    Dao2<String, String> secureDao = new AuthorizedDao2<>(
        new MemoryBasedSystemDao2<>(data),
        (user, key, permission) -> !(key.startsWith("locked_") && permission != Permission.READ));

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertTrue(secureDao.exists("locked_greeting", dummyUser));

    assertEquals("Hello", secureDao.get("greeting", dummyUser).get());
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());

    secureDao.update("greeting", "Hello updated", dummyUser);
    assertEquals("Hello updated", secureDao.get("greeting", dummyUser).get());

    try {
      secureDao.update("locked_greeting", "Good day updated", dummyUser);
      fail("Expected AccessDeniedException");
    } catch (AccessDeniedException e) {
      assertNotEquals("Good day updated", secureDao.get("locked_greeting", dummyUser));
      assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }

    secureDao.delete("greeting", dummyUser);
    assertFalse(secureDao.exists("greeting", dummyUser));

    try {
      secureDao.delete("locked_greeting", dummyUser);
      fail("Expected AccessDeniedException");
    } catch (AccessDeniedException e) {
      assertTrue(secureDao.exists("locked_greeting", dummyUser));
      assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());
    } catch (Throwable t) {
      fail("Unexpected error: " + t);
    }
  }

}
