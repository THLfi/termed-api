package fi.thl.termed.service.node.select;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static org.jparsercombinator.ParserCombinators.regex;
import static org.jparsercombinator.ParserCombinators.regexMatchResult;

import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.List;
import org.jparsercombinator.Parser;
import org.jparsercombinator.ParserCombinator;

public class SelectParser implements Parser<List<Select>> {

  private Parser<List<Select>> parser;

  public SelectParser() {
    ParserCombinator<Select> selectAll =
        regex("\\*").map(m -> new SelectAll());

    ParserCombinator<Select> selectIdParser =
        regex("(node\\.id|nodeId|id)").map(m -> new SelectId());
    ParserCombinator<Select> selectCodeParser =
        regex("code").map(m -> new SelectCode());
    ParserCombinator<Select> selectUriParser =
        regex("uri").map(m -> new SelectUri());
    ParserCombinator<Select> selectNumberParser =
        regex("(n|number)").map(m -> new SelectNumber());
    ParserCombinator<Select> selectCreatedByParser =
        regex("createdBy").map(m -> new SelectCreatedBy());
    ParserCombinator<Select> selectCreatedDateParser =
        regex("createdDate").map(m -> new SelectCreatedDate());
    ParserCombinator<Select> selectLastModifiedByParser =
        regex("lastModifiedBy").map(m -> new SelectLastModifiedBy());
    ParserCombinator<Select> selectLastModifiedDateParser =
        regex("lastModifiedDate").map(m -> new SelectLastModifiedDate());
    ParserCombinator<Select> selectTypeParser =
        regex("type").map(m -> new SelectType());

    ParserCombinator<Select> selectAllProperties =
        regex("(properties|props|p)\\.\\*")
            .map(m -> new SelectAllProperties());
    ParserCombinator<Select> selectProperty =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")")
            .map(m -> new SelectProperty(m.group(2)));
    ParserCombinator<Select> selectAllReferences =
        regex("(references|refs|r)\\.\\*")
            .map(m -> new SelectAllReferences());
    ParserCombinator<Select> selectReference =
        regexMatchResult("(references|refs|r)\\.(" + CODE + ")(:([0-9]+))?")
            .map(m -> new SelectReference(m.group(2), parseIntOrNullToOne(m.group(4))));
    ParserCombinator<Select> selectAllReferrers =
        regex("(referrers|refrs)\\.\\*")
            .map(m -> new SelectAllReferrers());
    ParserCombinator<Select> selectReferrer =
        regexMatchResult("(referrers|refrs)\\.(" + CODE + ")(:([0-9]+))?")
            .map(m -> new SelectReferrer(m.group(2), parseIntOrNullToOne(m.group(4))));

    ParserCombinator<Select> primitiveSelectParser =
        selectAll
            .or(selectIdParser)
            .or(selectCodeParser)
            .or(selectUriParser)
            .or(selectNumberParser)
            .or(selectCreatedByParser)
            .or(selectCreatedDateParser)
            .or(selectLastModifiedByParser)
            .or(selectLastModifiedDateParser)
            .or(selectTypeParser)
            .or(selectAllProperties)
            .or(selectProperty)
            .or(selectAllReferences)
            .or(selectReference)
            .or(selectAllReferrers)
            .or(selectReferrer);

    this.parser = primitiveSelectParser.many(regex("\\s*,\\s*")).end();
  }

  private Integer parseIntOrNullToOne(String s) {
    return s != null ? Integer.parseInt(s) : 1;
  }

  @Override
  public List<Select> apply(String select) {
    return parser.apply(select);
  }

}
