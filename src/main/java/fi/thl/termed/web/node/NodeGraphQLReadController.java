package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.SetUtils.toImmutableSet;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.query.Queries.matchAll;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.BadRequestException;
import fi.thl.termed.util.spring.exception.NotFoundException;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.Coercing;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.SchemaPrinter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeGraphQLReadController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  private GraphQLObjectType graphIdGraphQLType = newObject()
      .name("GraphId")
      .field(newFieldDefinition().name("id").type(GraphQLString))
      .build();

  private GraphQLObjectType typeIdGraphQLType = newObject()
      .name("TypeId")
      .field(newFieldDefinition().name("id").type(GraphQLString))
      .field(newFieldDefinition().name("graph").type(graphIdGraphQLType))
      .build();

  private GraphQLObjectType strictLangValueGraphQLType = GraphQLObjectType.newObject()
      .name("StrictLangValue")
      .field(newFieldDefinition().name("lang").type(GraphQLString))
      .field(newFieldDefinition().name("value").type(GraphQLString))
      .field(newFieldDefinition().name("regex").type(GraphQLString)).build();

  private GraphQLScalarType dateScalar = new GraphQLScalarType("Date",
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

  @PostMapping(value = "/graphs/{graphId}/types/{typeId}/nodes/graphql", produces = APPLICATION_JSON_UTF8_VALUE)
  public Object query(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestBody String graphQLQueryString,
      @AuthenticationPrincipal User user) {

    TypeId domainId = TypeId.of(typeId, graphId);

    if (!typeService.exists(domainId, user)) {
      throw new NotFoundException();
    }

    List<Type> allTypes = toListAndClose(typeService.values(matchAll(), user));

    Set<GraphQLType> graphQLTypes = allTypes.stream()
        .map(t -> toGraphQLType(t, allTypes, user))
        .collect(toImmutableSet());

    GraphQLObjectType domainGraphQLType = (GraphQLObjectType) graphQLTypes.stream()
        .filter(t -> t.getName().equals(toGraphQlTypeName(domainId)))
        .findFirst()
        .orElseThrow(IllegalStateException::new);

    GraphQLObjectType queryGraphQLType = GraphQLObjectType.newObject()
        .name("Query")
        .field(newFieldDefinition()
            .name("node")
            .type(domainGraphQLType)
            .argument(newArgument().name("id").type(GraphQLString))
            .dataFetcher(env -> nodeService.get(
                NodeId.of(UUIDs.fromString(env.getArgument("id")), domainId), user)
                .orElseThrow(NotFoundException::new)
            ))
        .build();

    GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
        .query(queryGraphQLType)
        .additionalTypes(graphQLTypes)
        .build();

    log.trace("GraphQLSchema: {}", new SchemaPrinter().print(graphQLSchema));

    GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

    ExecutionResult result = graphQL.execute(graphQLQueryString);

    if (result.getErrors().isEmpty()) {
      return result.getData();
    } else {
      throw new BadRequestException(result.getErrors().stream()
          .map(GraphQLError::getMessage)
          .collect(joining(", ")));
    }
  }

  private GraphQLObjectType toGraphQLType(Type type, List<Type> allTypes, User user) {
    return newObject()
        .name(toGraphQlTypeName(type.identifier()))
        .field(newFieldDefinition().name("id").type(GraphQLString))
        .field(newFieldDefinition().name("type").type(typeIdGraphQLType))
        .field(newFieldDefinition().name("code").type(GraphQLString))
        .field(newFieldDefinition().name("uri").type(GraphQLString))
        .field(newFieldDefinition().name("number").type(GraphQLInt))
        .field(newFieldDefinition().name("createdBy").type(GraphQLString))
        .field(newFieldDefinition().name("createdDate").type(dateScalar))
        .field(newFieldDefinition().name("lastModifiedBy").type(GraphQLString))
        .field(newFieldDefinition().name("lastModifiedDate").type(dateScalar))
        .field(buildPropertiesField(type))
        .field(buildReferencesField(type, user))
        .field(buildReferrersField(type, allTypes, user))
        .build();
  }

  private GraphQLFieldDefinition buildPropertiesField(Type type) {
    return newFieldDefinition()
        .name("properties")
        .type(newObject().name(toGraphQlTypeName(type.identifier()) + "_Properties")
            .fields(type.getTextAttributes().stream()
                .map(this::buildPropertyField)
                .collect(toImmutableList())))
        .build();
  }

  private GraphQLFieldDefinition buildPropertyField(TextAttribute attr) {
    return newFieldDefinition()
        .name(attr.getId().replace('-', '_'))
        .type(list(strictLangValueGraphQLType))
        .dataFetcher(env -> {
          ImmutableMultimap<String, StrictLangValue> values = env.getSource();
          return values.get(attr.getId());
        }).build();
  }

  private GraphQLFieldDefinition buildReferencesField(Type type, User user) {
    return newFieldDefinition()
        .name("references")
        .type(newObject().name(toGraphQlTypeName(type.identifier()) + "_References")
            .fields(type.getReferenceAttributes().stream()
                .map(attr -> buildReferenceField(attr, user))
                .collect(toImmutableList())))
        // pass env.getSource (Node) to downstream (i.e. not the default of Node.references)
        .dataFetcher(DataFetchingEnvironment::getSource)
        .build();
  }

  private GraphQLFieldDefinition buildReferenceField(ReferenceAttribute attr, User user) {
    return newFieldDefinition()
        .name(attr.getId().replace('-', '_'))
        .type(list(GraphQLTypeReference.typeRef(toGraphQlTypeName(attr.getRange()))))
        // here env.getSource returns the full Node, not just 'references' multimap
        // (see dataFetcher defined for references field)
        .dataFetcher(env ->
            new IndexedReferenceLoader(nodeService, user).apply(env.getSource(), attr.getId()))
        .build();
  }

  private GraphQLFieldDefinition buildReferrersField(Type type, List<Type> allTypes, User user) {
    Stream<ReferenceAttribute> referringAttributes = allTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .filter(attr -> Objects.equals(type.identifier(), attr.getRange()));

    return newFieldDefinition()
        .name("referrers")
        .type(newObject().name(toGraphQlTypeName(type.identifier()) + "_ReferrersMultimap")
            .fields(referringAttributes
                .map(attr -> buildReferrerField(attr, user))
                .collect(toImmutableList())))
        // pass env.getSource (Node) to downstream (i.e. not the default of Node.referrers)
        .dataFetcher(DataFetchingEnvironment::getSource)
        .build();
  }

  private GraphQLFieldDefinition buildReferrerField(ReferenceAttribute attr, User user) {
    return newFieldDefinition()
        .name(attr.getId().replace('-', '_'))
        .type(list(GraphQLTypeReference.typeRef(toGraphQlTypeName(attr.getDomain()))))
        // here env.getSource returns the full Node, not just 'referrers' multimap
        // (see dataFetcher defined for referrers field)
        .dataFetcher(env ->
            new IndexedReferrerLoader(nodeService, user).apply(env.getSource(), attr.getId()))
        .build();
  }

  private String toGraphQlTypeName(TypeId typeId) {
    return ("_" + UUIDs.toString(typeId.getGraphId()) + "_" + typeId.getId()).replace('-', '_');
  }

}
