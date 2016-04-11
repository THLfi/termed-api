package fi.thl.termed.util;

import com.google.common.base.Predicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public final class JsonPredicates {

  private JsonPredicates() {
  }

  public static Predicate<JsonElement> stringMatches(final String regex) {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return element.isJsonPrimitive() &&
               element.getAsString().matches(regex);
      }
    };
  }

  public static Predicate<JsonElement> stringDoesNotMatch(
      final String regex) {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !stringMatches(regex).apply(element);
      }
    };
  }

  public static Predicate<JsonElement> isNull() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return JsonNull.INSTANCE.equals(element);
      }
    };
  }

  public static Predicate<JsonElement> notNull() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !isNull().apply(element);
      }
    };
  }

  public static Predicate<JsonElement> stringIsEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return element.isJsonPrimitive() &&
               element.getAsString().isEmpty();
      }
    };
  }

  public static Predicate<JsonElement> stringNotEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !stringIsEmpty().apply(element);
      }
    };
  }

  public static Predicate<JsonElement> arrayIsEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return element.isJsonArray() &&
               element.getAsJsonArray().size() == 0;
      }
    };
  }

  public static Predicate<JsonElement> arrayNotEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !arrayIsEmpty().apply(element);
      }
    };
  }

  public static Predicate<JsonElement> objectIsEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return element.isJsonObject() &&
               element.getAsJsonObject().entrySet().size() == 0;
      }
    };
  }

  public static Predicate<JsonElement> objectNotEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !objectIsEmpty().apply(element);
      }
    };
  }

  public static Predicate<JsonElement> isEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return stringIsEmpty().apply(element) ||
               arrayIsEmpty().apply(element) ||
               objectIsEmpty().apply(element);
      }
    };
  }

  public static Predicate<JsonElement> notEmpty() {
    return new Predicate<JsonElement>() {
      @Override
      public boolean apply(JsonElement element) {
        return !isEmpty().apply(element);
      }
    };
  }

}
