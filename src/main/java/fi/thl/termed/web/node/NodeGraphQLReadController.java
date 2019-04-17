package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.service.node.util.GraphQLUtils.toGraphQlTypeName;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.Queries.matchAll;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.util.GraphQLUtils;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.node.util.TypeToGraphQLType;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.BadRequestException;
import fi.thl.termed.util.spring.exception.NotFoundException;
import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.idl.SchemaPrinter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostMapping(value = "/graphs/{graphId}/types/{typeId}/nodes/graphql", produces = APPLICATION_JSON_UTF8_VALUE)
  public Object queryNodesOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestBody String graphQLQueryString,
      @AuthenticationPrincipal User user) {

    TypeId domainId = TypeId.of(typeId, graphId);
    Type domain = typeService.get(domainId, user).orElseThrow(NotFoundException::new);

    ImmutableList<Graph> allGraphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    ImmutableList<Type> allTypes = toImmutableListAndClose(typeService.values(matchAll(), user));

    Map<String, GraphQLType> graphQLTypesByName = allTypes.stream()
        .map(new TypeToGraphQLType(allTypes,
            new IndexedReferenceLoader(nodeService, user),
            new IndexedReferrerLoader(nodeService, user)))
        .collect(toMap(GraphQLType::getName, t -> t));

    GraphQLObjectType queryGraphQLType = GraphQLObjectType.newObject()
        .name("Query")
        .field(newFieldDefinition()
            .name("nodes")
            .type(list(graphQLTypesByName.get(toGraphQlTypeName(domainId))))
            .argument(asList(
                newArgument().name("where").type(GraphQLString).defaultValue("").build(),
                newArgument().name("sort").type(list(GraphQLString)).defaultValue(of()).build(),
                newArgument().name("max").type(GraphQLInt).defaultValue(-1).build()))
            .dataFetcher(env -> {
              String where = env.getArgument("where");
              List<String> sort = env.getArgument("sort");
              Integer max = env.getArgument("max");

              Specification<NodeId, Node> nodeSpecification = where.isEmpty() ?
                  and(NodesByGraphId.of(graphId), NodesByTypeId.of(typeId)) :
                  specifyByQuery(allGraphs, allTypes, domain, where);

              return toImmutableListAndClose(nodeService.values(
                  new Query<>(nodeSpecification, sort, max), user));
            }))
        .build();

    GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
        .query(queryGraphQLType)
        .additionalTypes(ImmutableSet.copyOf(graphQLTypesByName.values()))
        .build();

    if (log.isTraceEnabled()) {
      log.trace("GraphQLSchema: {}", new SchemaPrinter().print(graphQLSchema));
    }

    return GraphQLUtils.getErrorsOrData(
        GraphQL.newGraphQL(graphQLSchema).build().execute(graphQLQueryString))
        .mapLeft(errorMessage -> {
          throw new BadRequestException(errorMessage);
        })
        .getRight();
  }

}
