package fi.thl.termed.service.node.sort;

import fi.thl.termed.util.query.Sort;
import fi.thl.termed.util.query.SortRelevance;
import java.util.Collections;
import java.util.List;
import org.jparsercombinator.Parser;

public final class NodeSorts {

  private static final Parser<List<Sort>> SORT_PARSER = new NodeSortParser();

  public static List<Sort> parse(String sortString) {
    return sortString.isEmpty()
        ? Collections.singletonList(SortRelevance.INSTANCE)
        : SORT_PARSER.apply(sortString);
  }

  public static List<Sort> parse(List<String> sortStrings) {
    return sortStrings.isEmpty()
        ? Collections.singletonList(SortRelevance.INSTANCE)
        : SORT_PARSER.apply(String.join(", ", sortStrings));
  }

}
