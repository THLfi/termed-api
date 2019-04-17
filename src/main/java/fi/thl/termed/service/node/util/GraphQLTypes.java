package fi.thl.termed.service.node.util;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import graphql.schema.Coercing;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import java.util.Date;
import org.joda.time.DateTime;

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
      "A custom scalar that handles java.util.Date", new Coercing<Date, String>() {
    @Override
    public String serialize(Object dataFetcherResult) {
      return new DateTime(dataFetcherResult).toString();
    }

    @Override
    public Date parseValue(Object input) {
      return new DateTime(input).toDate();
    }

    @Override
    public Date parseLiteral(Object input) {
      return new DateTime(input).toDate();
    }
  });

  private GraphQLTypes() {
  }

}
