package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.collect.Either.left;
import static fi.thl.termed.util.collect.Either.right;
import static java.util.stream.Collectors.joining;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Either;
import graphql.ExecutionResult;
import graphql.GraphQLError;

public final class GraphQLUtils {

  private GraphQLUtils() {
  }

  public static String toGraphQlTypeName(TypeId typeId) {
    return ("_" + UUIDs.toString(typeId.getGraphId()) + "_" + typeId.getId()).replace('-', '_');
  }

  public static Either<String, Object> getErrorsOrData(ExecutionResult result) {
    if (!result.getErrors().isEmpty()) {
      return left(result.getErrors().stream()
          .map(GraphQLError::getMessage)
          .collect(joining(", ")));
    } else {
      return right(result.getData());
    }
  }

}
