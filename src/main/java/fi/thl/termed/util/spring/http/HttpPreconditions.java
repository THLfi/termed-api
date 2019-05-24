package fi.thl.termed.util.spring.http;

import fi.thl.termed.util.spring.exception.BadRequestException;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Optional;
import java.util.function.Supplier;

public final class HttpPreconditions {

  private HttpPreconditions() {
  }

  // if false throws 400

  public static void checkRequestParam(boolean expression) {
    if (!expression) {
      throw new BadRequestException();
    }
  }

  public static void checkRequestParam(boolean expression, String errorMessage) {
    if (!expression) {
      throw new BadRequestException(errorMessage);
    }
  }

  public static void checkRequestParam(boolean expression, Supplier<String> errorMessageSupplier) {
    if (!expression) {
      throw new BadRequestException(errorMessageSupplier.get());
    }
  }

  public static <T> T checkRequestParamNotNull(T value) {
    if (value != null) {
      return value;
    }
    throw new BadRequestException();
  }

  public static <T> T checkRequestParamNotNull(T value, String errorMessage) {
    if (value != null) {
      return value;
    }
    throw new BadRequestException(errorMessage);
  }

  public static <T> T checkRequestParamNotNull(T value, Supplier<String> errorMessageSupplier) {
    if (value != null) {
      return value;
    }
    throw new BadRequestException(errorMessageSupplier.get());
  }

  // if false throws 404

  public static void checkFound(boolean expression) {
    if (!expression) {
      throw new NotFoundException();
    }
  }

  public static void checkFound(boolean expression, String errorMessage) {
    if (!expression) {
      throw new NotFoundException(errorMessage);
    }
  }

  public static void checkFound(boolean expression, Supplier<String> errorMessageSupplier) {
    if (!expression) {
      throw new NotFoundException(errorMessageSupplier.get());
    }
  }

  public static <T> T checkFound(T value) {
    if (value != null) {
      return value;
    }
    throw new NotFoundException();
  }

  public static <T> T checkFound(T value, String errorMessage) {
    if (value != null) {
      return value;
    }
    throw new NotFoundException(errorMessage);
  }

  public static <T> T checkFound(T value, Supplier<String> errorMessageSupplier) {
    if (value != null) {
      return value;
    }
    throw new NotFoundException(errorMessageSupplier.get());
  }

  public static <T> T checkFound(Optional<T> optional) {
    return optional.orElseThrow(NotFoundException::new);
  }

  public static <T> T checkFound(Optional<T> optional, String errorMessage) {
    return optional.orElseThrow(() -> new NotFoundException(errorMessage));
  }

  public static <T> T checkFound(Optional<T> optional, Supplier<String> errorMessageSupplier) {
    return optional.orElseThrow(() -> new NotFoundException(errorMessageSupplier.get()));
  }

}
