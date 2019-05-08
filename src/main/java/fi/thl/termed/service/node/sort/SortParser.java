package fi.thl.termed.service.node.sort;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static org.jparsercombinator.ParserCombinators.regex;
import static org.jparsercombinator.ParserCombinators.regexMatchResult;

import com.google.common.base.Strings;
import fi.thl.termed.util.query.Sort;
import java.util.List;
import org.jparsercombinator.Parser;
import org.jparsercombinator.ParserCombinator;

class SortParser implements Parser<List<Sort>> {

  private Parser<List<Sort>> parser;

  SortParser() {
    ParserCombinator<Sort> sortNumberParser =
        regexMatchResult("(number|n)(\\.sortable)?([ +](asc|desc))?")
            .map(m -> Sort.sort("number", "desc".equals(m.group(4))));

    ParserCombinator<Sort> sortCreatedDateParser =
        regexMatchResult("createdDate(\\.sortable)?([ +](asc|desc))?")
            .map(m -> Sort.sort("createdDate", "desc".equals(m.group(3))));

    ParserCombinator<Sort> sortLastModifiedDateParser =
        regexMatchResult("lastModifiedDate(\\.sortable)?([ +](asc|desc))?")
            .map(m -> Sort.sort("lastModifiedDate", "desc".equals(m.group(3))));

    ParserCombinator<Sort> sortProperty =
        regexMatchResult(
            "(properties|props|p)\\.(" + CODE + ")"
                + "((\\.[a-z]{2})\\.sortable|\\.sortable|(\\.[a-z]{2}))?"
                + "([ +](asc|desc))?")
            .map(m -> Sort.sort("properties." + m.group(2) +
                    Strings.nullToEmpty(m.group(4)) +
                    Strings.nullToEmpty(m.group(5)),
                "desc".equals(m.group(7))));

    ParserCombinator<Sort> primitiveSortParser =
        sortNumberParser
            .or(sortCreatedDateParser)
            .or(sortLastModifiedDateParser)
            .or(sortProperty);

    this.parser = primitiveSortParser.many(regex("\\s*,\\s*")).end();
  }

  @Override
  public List<Sort> apply(String sort) {
    return parser.apply(sort);
  }

}
