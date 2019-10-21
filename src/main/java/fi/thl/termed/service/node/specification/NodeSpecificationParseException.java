package fi.thl.termed.service.node.specification;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NodeSpecificationParseException extends RuntimeException {

  public NodeSpecificationParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
