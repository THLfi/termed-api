package fi.thl.termed.util.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Maps;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class SecureDaoTest {

  private User dummyUser = new User("testUser", "testUser", AppRole.USER);

  @Test
  void shouldTreatSecretReadDataAsNonexistent() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("secret_greeting", "Hi!");

    Dao<String, String> secureDao = new AuthorizedDao<>(
        new MemoryBasedSystemDao<>(data),
        (user, key, permission) -> !key.startsWith("secret_"));

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertFalse(secureDao.exists("secret_greeting", dummyUser));
    assertFalse(secureDao.exists("nonexistent_greeting", dummyUser));

    assertEquals(data.get("greeting"), secureDao.get("greeting", dummyUser).get());
    assertEquals(Optional.empty(), secureDao.get("secret_greeting", dummyUser));
    assertEquals(Optional.empty(), secureDao.get("nonexistent_greeting", dummyUser));
  }

  @Test
  void shouldIgnoreModificationsOnWriteProtectedData() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("locked_greeting", "Good day");

    Dao<String, String> secureDao = new AuthorizedDao<>(
        new MemoryBasedSystemDao<>(data),
        (user, key, permission) -> !(key.startsWith("locked_") && permission != Permission.READ));

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertTrue(secureDao.exists("locked_greeting", dummyUser));

    assertEquals("Hello", secureDao.get("greeting", dummyUser).get());
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());

    secureDao.update("greeting", "Hello updated", dummyUser);
    assertEquals("Hello updated", secureDao.get("greeting", dummyUser).get());

    assertThrows(AccessDeniedException.class,
        () -> secureDao.update("locked_greeting", "Good day updated", dummyUser));

    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());

    secureDao.delete("greeting", dummyUser);

    assertFalse(secureDao.exists("greeting", dummyUser));

    assertThrows(AccessDeniedException.class,
        () -> secureDao.delete("locked_greeting", dummyUser));

    assertTrue(secureDao.exists("locked_greeting", dummyUser));
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser).get());
  }

}
