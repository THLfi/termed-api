package fi.thl.termed.service.node.util;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import graphql.schema.Coercing;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import java.time.LocalDateTime;

final class GraphQLTypes {

  static final GraphQLObjectType graphIdGraphQLType = newObject()
      .name("GraphId")
      .field(newFieldDefinition().name("id").type(GraphQLString))
      .build();

  static final GraphQLObjectType typeIdGraphQLType = newObject()
      .name("TypeId")
      .field(newFieldDefinition().name("id").type(GraphQLString))
      .field(newFieldDefinition().name("graph").type(graphIdGraphQLType))
      .build();

  static final GraphQLObjectType strictLangValueGraphQLType = GraphQLObjectType.newObject()
      .name("StrictLangValue")
      .field(newFieldDefinition().name("lang").type(GraphQLString))
      .field(newFieldDefinition().name("value").type(GraphQLString))
      .field(newFieldDefinition().name("regex").type(GraphQLString)).build();

  static final GraphQLScalarType dateScalar = new GraphQLScalarType("Date",
      "A custom scalar that handles java.time.LocalDateTime",
      new Coercing<LocalDateTime, String>() {
        @Override
        public String serialize(Object dataFetcherResult) {
          return dataFetcherResult.toString();
        }

        @Override
        public LocalDateTime parseValue(Object input) {
          return LocalDateTime.parse((String) input);
        }

        @Override
        public LocalDateTime parseLiteral(Object input) {
          return LocalDateTime.parse((String) input);
        }
      });

  private GraphQLTypes() {
  }

}
