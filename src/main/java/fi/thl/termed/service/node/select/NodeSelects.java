package fi.thl.termed.service.node.select;

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.spring.exception.BadRequestException;
import java.util.List;
import org.jparsercombinator.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeSelects {

  private static final Logger log = LoggerFactory.getLogger(NodeSelects.class);

  private static final NodeSelectParser PARSER = new NodeSelectParser();

  private NodeSelects() {
  }

  public static List<Select> parse(List<String> selects) {
    try {
      return PARSER.apply(String.join(",", selects));
    } catch (ParseException e) {
      log.warn("Parsing select failed: {}", e.getMessage());
      throw new BadRequestException(e);
    }
  }

  public static List<Select> qualify(List<Type> allTypes, List<Type> domainTypes,
      List<Select> selects) {
    return ImmutableList.copyOf(
        new NodeSelectQualifier(allTypes).apply(
            ImmutableSet.copyOf(domainTypes),
            ImmutableSet.copyOf(selects)));
  }

  public static ImmutableMap<Tuple2<TypeId, String>, Integer> toReferenceSelectsWithDepths(
      List<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectTypeQualifiedReference)
        .map(s -> (SelectTypeQualifiedReference) s)
        .collect(toMap(
            s -> Tuple.of(s.getTypeId(), s.getField()),
            SelectTypeQualifiedReference::getDepth,
            Integer::max)));
  }

  public static ImmutableMap<Tuple2<TypeId, String>, Integer> toReferrerSelectsWithDepths(
      List<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectTypeQualifiedReferrer)
        .map(s -> (SelectTypeQualifiedReferrer) s)
        .collect(toMap(
            s -> Tuple.of(s.getTypeId(), s.getField()),
            SelectTypeQualifiedReferrer::getDepth,
            Integer::max)));
  }

}
