package fi.thl.termed.service.node.select;

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Select;
import java.util.List;

public final class Selects {

  private static final SelectParser PARSER = new SelectParser();
  private static final SelectQualifier QUALIFIER = new SelectQualifier();

  private Selects() {
  }

  public static List<Select> parse(List<String> selects) {
    return PARSER.apply(String.join(",", selects));
  }

  public static List<Select> qualify(List<Type> allTypes, List<Type> domainTypes,
      List<Select> selects) {
    return QUALIFIER.apply(allTypes, domainTypes, selects);
  }

  public static ImmutableMap<Tuple2<TypeId, String>, Integer> selectReferences(
      List<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectTypeQualifiedReference)
        .map(s -> (SelectTypeQualifiedReference) s)
        .collect(toMap(
            s -> Tuple.of(s.getTypeId(), s.getField()),
            SelectTypeQualifiedReference::getDepth)));
  }

  public static ImmutableMap<Tuple2<TypeId, String>, Integer> selectReferrers(
      List<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectTypeQualifiedReferrer)
        .map(s -> (SelectTypeQualifiedReferrer) s)
        .collect(toMap(
            s -> Tuple.of(s.getTypeId(), s.getField()),
            SelectTypeQualifiedReferrer::getDepth)));
  }

}
