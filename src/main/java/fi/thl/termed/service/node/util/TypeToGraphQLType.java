package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.service.node.util.GraphQLTypes.dateScalar;
import static fi.thl.termed.service.node.util.GraphQLTypes.strictLangValueGraphQLType;
import static fi.thl.termed.service.node.util.GraphQLTypes.typeIdGraphQLType;
import static fi.thl.termed.service.node.util.GraphQLUtils.toGraphQlTypeName;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Converts simple type definition to data loading GraphQL type.
 */
public class TypeToGraphQLType implements Function<Type, GraphQLType> {

  private final BiFunction<Node, String, ImmutableList<Node>> referenceProvider;
  private final BiFunction<Node, String, ImmutableList<Node>> referrerProvider;
  private final Map<TypeId, List<ReferenceAttribute>> referenceAttributesByRange;

  public TypeToGraphQLType(
      List<Type> allTypes,
      BiFunction<Node, String, ImmutableList<Node>> referenceProvider,
      BiFunction<Node, String, ImmutableList<Node>> referrerProvider) {
    this.referenceProvider = referenceProvider;
    this.referrerProvider = referrerProvider;
    this.referenceAttributesByRange = allTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .collect(groupingBy(ReferenceAttribute::getRange));
  }

  @Override
  public GraphQLType apply(Type type) {
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
        .field(buildReferencesField(type))
        .field(buildReferrersField(type))
        .build();
  }

  private GraphQLFieldDefinition buildPropertiesField(Type type) {
    return newFieldDefinition()
        .name("properties")
        .type(newObject()
            .name(toGraphQlTypeName(type.identifier()) + "_Properties")
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

  private GraphQLFieldDefinition buildReferencesField(Type type) {
    return newFieldDefinition()
        .name("references")
        .type(newObject()
            .name(toGraphQlTypeName(type.identifier()) + "_References")
            .fields(type.getReferenceAttributes().stream()
                .map(this::buildReferenceField)
                .collect(toImmutableList())))
        // pass env.getSource (Node) to downstream (i.e. not the default of Node.references)
        .dataFetcher(DataFetchingEnvironment::getSource)
        .build();
  }

  private GraphQLFieldDefinition buildReferenceField(ReferenceAttribute attr) {
    return newFieldDefinition()
        .name(attr.getId().replace('-', '_'))
        .type(list(GraphQLTypeReference.typeRef(toGraphQlTypeName(attr.getRange()))))
        // here env.getSource returns the full Node, not just 'references' multimap
        // (see dataFetcher defined for references field)
        .dataFetcher(env -> referenceProvider.apply(env.getSource(), attr.getId()))
        .build();
  }

  private GraphQLFieldDefinition buildReferrersField(Type type) {
    List<ReferenceAttribute> referringAttributes =
        referenceAttributesByRange.getOrDefault(type.identifier(), ImmutableList.of());

    return newFieldDefinition()
        .name("referrers")
        .type(newObject()
            .name(toGraphQlTypeName(type.identifier()) + "_Referrers")
            .fields(referringAttributes.stream()
                .map(this::buildReferrerField)
                .collect(toImmutableList())))
        // pass env.getSource (Node) to downstream (i.e. not the default of Node.referrers)
        .dataFetcher(DataFetchingEnvironment::getSource)
        .build();
  }

  private GraphQLFieldDefinition buildReferrerField(ReferenceAttribute attr) {
    return newFieldDefinition()
        .name(attr.getId().replace('-', '_'))
        .type(list(GraphQLTypeReference.typeRef(toGraphQlTypeName(attr.getDomain()))))
        // here env.getSource returns the full Node, not just 'referrers' multimap
        // (see dataFetcher defined for referrers field)
        .dataFetcher(env -> referrerProvider.apply(env.getSource(), attr.getId()))
        .build();
  }

}
