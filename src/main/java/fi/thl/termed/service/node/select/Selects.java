package fi.thl.termed.service.node.select;

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.util.query.Select;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jparsercombinator.Parser;

public final class Selects {

  private static Parser<List<Select>> parser = new SelectParser();

  private Selects() {
  }

  public static Set<Select> parse(String select) {
    return ImmutableSet.copyOf(parser.apply(select));
  }

  public static Map<String, Integer> selectReferences(Set<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectReference)
        .map(s -> (SelectReference) s)
        .collect(toMap(
            SelectReference::getField,
            SelectReference::getDepth)));
  }

  public static Map<String, Integer> selectReferrers(Set<Select> selects) {
    return ImmutableMap.copyOf(selects.stream()
        .filter(s -> s instanceof SelectReferrer)
        .map(s -> (SelectReferrer) s)
        .collect(toMap(
            SelectReferrer::getField,
            SelectReferrer::getDepth)));
  }

}
