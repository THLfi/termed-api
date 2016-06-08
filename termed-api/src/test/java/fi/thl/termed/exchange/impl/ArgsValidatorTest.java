package fi.thl.termed.exchange.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArgsValidatorTest {

  @Test
  public void shouldValidateArgs() {
    Map<String, Class> requiredArgs = ImmutableMap.<String, Class>of("name", String.class,
                                                                     "age", Integer.class);
    Map<String, Object> args = ImmutableMap.<String, Object>of("name", "John", "age", 44);
    assertTrue(ArgsValidator.validate(args, requiredArgs));
  }

  @Test
  public void shouldRejectWithMissingArgs() {
    Map<String, Class> requiredArgs = ImmutableMap.<String, Class>of("name", String.class,
                                                                     "age", Integer.class);
    Map<String, Object> args = ImmutableMap.<String, Object>of("name", "John");
    assertFalse(ArgsValidator.validate(args, requiredArgs));
  }

  @Test
  public void shouldRejectWithNullArgValues() {
    Map<String, Class> requiredArgs = ImmutableMap.<String, Class>of("name", String.class,
                                                                     "age", Integer.class);
    Map<String, Object> args = Maps.newHashMap();
    args.put("name", "John");
    args.put("age", null);
    assertFalse(ArgsValidator.validate(args, requiredArgs));
  }

  @Test
  public void shouldRejectArgsOfWrongType() {
    Map<String, Class> requiredArgs = ImmutableMap.<String, Class>of("age", Integer.class);
    Map<String, Object> args = ImmutableMap.<String, Object>of("age", "1");
    assertFalse(ArgsValidator.validate(args, requiredArgs));
  }

  @Test
  public void shouldRejectArgsOfWrongName() {
    Map<String, Class> requiredArgs = ImmutableMap.<String, Class>of("name", String.class);
    Map<String, Object> args = ImmutableMap.<String, Object>of("firstName", "John");
    assertFalse(ArgsValidator.validate(args, requiredArgs));
  }

}