package fi.thl.termed.service.node.select;

import static java.util.stream.Collectors.toMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jparsercombinator.Parser;

public final class Selects {

  private static Parser<List<Select>> parser = new SelectParser();

  private Selects() {
  }

  public static Set<Select> parse(String select) {
    return new HashSet<>(parser.apply(select));
  }

  public static Map<String, Integer> selectReferences(Set<Select> selects) {
    return selects.stream()
        .filter(s -> s instanceof SelectReference)
        .map(s -> (SelectReference) s)
        .collect(toMap(
            SelectReference::getAttributeId,
            SelectReference::getDepth));
  }

  public static Map<String, Integer> selectReferrers(Set<Select> selects) {
    return selects.stream()
        .filter(s -> s instanceof SelectReferrer)
        .map(s -> (SelectReferrer) s)
        .collect(toMap(
            SelectReferrer::getAttributeId,
            SelectReferrer::getDepth));
  }

}
