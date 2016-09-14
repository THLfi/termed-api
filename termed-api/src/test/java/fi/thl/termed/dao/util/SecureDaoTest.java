package fi.thl.termed.dao.util;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SecureDaoTest {

  private User dummyUser = new User("testUser", "testUser", AppRole.USER);

  @Test
  public void shouldTreatSecretReadDataAsNonexistent() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("secret_greeting", "Hi!");

    Dao<String, String> secureDao = new SecureDao<String, String>(
        new MemoryBasedSystemDao<String, String>(data),
        new PermissionEvaluator<String>() {
          public boolean hasPermission(User user, String key, Permission permission) {
            return !key.startsWith("secret_");
          }
        }, true);

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertFalse(secureDao.exists("secret_greeting", dummyUser));
    assertFalse(secureDao.exists("nonexistent_greeting", dummyUser));

    assertEquals(data.get("greeting"), secureDao.get("greeting", dummyUser));
    assertNull(secureDao.get("secret_greeting", dummyUser));
    assertNull(secureDao.get("nonexistent_greeting", dummyUser));
  }

  @Test
  public void shouldIgnoreModificationsOnWriteProtectedData() {
    Map<String, String> data = Maps.newHashMap();

    data.put("greeting", "Hello");
    data.put("locked_greeting", "Good day");

    Dao<String, String> secureDao = new SecureDao<String, String>(
        new MemoryBasedSystemDao<String, String>(data),
        new PermissionEvaluator<String>() {
          public boolean hasPermission(User user, String key, Permission permission) {
            return !(key.startsWith("locked_") && permission != Permission.READ);
          }
        }, true);

    assertTrue(secureDao.exists("greeting", dummyUser));
    assertTrue(secureDao.exists("locked_greeting", dummyUser));

    assertEquals("Hello", secureDao.get("greeting", dummyUser));
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser));

    secureDao.update("greeting", "Hello updated", dummyUser);
    secureDao.update("locked_greeting", "Good day updated", dummyUser);

    assertEquals("Hello updated", secureDao.get("greeting", dummyUser));
    assertNotEquals("Good day updated", secureDao.get("locked_greeting", dummyUser));
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser));

    secureDao.delete("greeting", dummyUser);
    secureDao.delete("locked_greeting", dummyUser);

    assertFalse(secureDao.exists("greeting", dummyUser));
    assertTrue(secureDao.exists("locked_greeting", dummyUser));
    assertEquals("Good day", secureDao.get("locked_greeting", dummyUser));

    assertFalse(secureDao.exists("locked_greeting_2", dummyUser));
    secureDao.insert("locked_greeting_2", "Good night", dummyUser);
    assertFalse(secureDao.exists("locked_greeting_2", dummyUser));

    assertFalse(secureDao.exists("greeting_2", dummyUser));
    secureDao.insert("greeting_2", "Good night", dummyUser);
    assertEquals("Good night", secureDao.get("greeting_2", dummyUser));
  }

}