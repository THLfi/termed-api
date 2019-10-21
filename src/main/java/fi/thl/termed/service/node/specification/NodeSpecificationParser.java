package fi.thl.termed.service.node.specification;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.ISO_8601_DATE;
import static fi.thl.termed.util.RegularExpressions.UUID;
import static fi.thl.termed.util.query.BoostSpecification.boost;
import static fi.thl.termed.util.query.NotSpecification.not;
import static java.lang.Float.parseFloat;
import static org.jparsercombinator.ParserCombinators.newRef;
import static org.jparsercombinator.ParserCombinators.regex;
import static org.jparsercombinator.ParserCombinators.regexMatchResult;
import static org.jparsercombinator.ParserCombinators.skip;
import static org.jparsercombinator.ParserCombinators.string;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import java.time.LocalDateTime;
import org.jparsercombinator.ParseException;
import org.jparsercombinator.Parser;
import org.jparsercombinator.ParserCombinator;
import org.jparsercombinator.ParserCombinatorReference;

public class NodeSpecificationParser implements Parser<Specification<NodeId, Node>> {

  private Parser<Specification<NodeId, Node>> parser;

  public NodeSpecificationParser() {
    ParserCombinatorReference<Specification<NodeId, Node>> queryParser = newRef();
    ParserCombinatorReference<Specification<NodeId, Node>> termParser = newRef();
    ParserCombinatorReference<Specification<NodeId, Node>> factorParser = newRef();
    ParserCombinatorReference<Specification<NodeId, Node>> attributeValueParser = newRef();

    ParserCombinator<Specification<NodeId, Node>> idParser =
        regexMatchResult("(node\\.id|nodeId|id):(" + UUID + ")")
            .map(m -> new NodesById(UUIDs.fromString(m.group(2))));
    ParserCombinator<Specification<NodeId, Node>> codeParser =
        regexMatchResult("code:(" + CODE + ")")
            .map(m -> new NodesByCode(m.group(1)));
    ParserCombinator<Specification<NodeId, Node>> urnUuidParser =
        regexMatchResult("urn:uuid:(" + UUID + ")")
            .map(m -> new NodesById(UUIDs.fromString(m.group(1))));
    ParserCombinator<Specification<NodeId, Node>> uriParser =
        regexMatchResult("uri:([^\\s]+)")
            .map(m -> new NodesByUri(m.group(1)));
    ParserCombinator<Specification<NodeId, Node>> numberParser =
        regexMatchResult("(n|number):([0-9]*)")
            .map(m -> new NodesByNumber(Long.parseLong(m.group(2))));

    ParserCombinator<Specification<NodeId, Node>> createdDateParser =
        regexMatchResult("createdDate:"
            + "\\[(\\*|" + ISO_8601_DATE + ") TO (\\*|" + ISO_8601_DATE + ")\\]")
            .map(m -> new NodesByCreatedDate(parseDate(m.group(1)), parseDate(m.group(9))));
    ParserCombinator<Specification<NodeId, Node>> lastModifiedDateParser =
        regexMatchResult("lastModifiedDate:"
            + "\\[(\\*|" + ISO_8601_DATE + ") TO (\\*|" + ISO_8601_DATE + ")\\]")
            .map(m -> new NodesByLastModifiedDate(parseDate(m.group(1)), parseDate(m.group(9))));

    ParserCombinator<Specification<NodeId, Node>> graphIdParser =
        regexMatchResult("(type\\.graph\\.id|graph\\.id|graphId):(" + UUID + ")")
            .map(m -> new NodesByGraphId(UUIDs.fromString(m.group(2))));
    ParserCombinator<Specification<NodeId, Node>> graphCodeParser =
        regexMatchResult("(type\\.graph\\.code|graph\\.code|graphCode):(" + CODE + ")")
            .map(m -> new NodesByGraphCode(m.group(2)));
    ParserCombinator<Specification<NodeId, Node>> graphUriParser =
        regexMatchResult("(type\\.graph\\.uri|graph\\.uri|graphUri):([^\\s]+)")
            .map(m -> new NodesByGraphUri(m.group(2)));
    ParserCombinator<Specification<NodeId, Node>> typeIdParser =
        regexMatchResult("(type\\.id|typeId):(" + CODE + ")")
            .map(m -> new NodesByTypeId(m.group(2)));
    ParserCombinator<Specification<NodeId, Node>> typeUriParser =
        regexMatchResult("(type\\.uri|typeUri):([^\\s]+)")
            .map(m -> new NodesByTypeUri(m.group(2)));

    ParserCombinator<Specification<NodeId, Node>> propertyStringQuotedParser =
        regexMatchResult(
            "(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?\\.string:\"([^\"]*)\"")
            .map(m -> new NodesByPropertyStringPhrase(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyStringRangeParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?\\.string:"
            + "\\[(\\*|([^\\s]*)) TO (\\*|([^\\]]*))\\]").map(m ->
            new NodesByPropertyStringRange(m.group(2), m.group(4), m.group(6), m.group(8)));
    ParserCombinator<Specification<NodeId, Node>> propertyStringPrefixParser =
        regexMatchResult(
            "(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?\\.string:([^\\s]*)\\*")
            .map(m -> new NodesByPropertyStringPrefix(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyStringParser =
        regexMatchResult(
            "(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?\\.string:([^\\s\\)\\*\\^]*)")
            .map(m -> new NodesByPropertyString(m.group(2), m.group(4), m.group(5)));

    ParserCombinator<Specification<NodeId, Node>> propertyQuotedParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:\"([^\"]*)\"")
            .map(m -> new NodesByPropertyPhrase(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyPrefixParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:([^\\s]*)\\*")
            .map(m -> new NodesByPropertyPrefix(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:([^\\s\\)\\*\\^]*)")
            .map(m -> new NodesByProperty(m.group(2), m.group(4), m.group(5)));

    ParserCombinator<Specification<NodeId, Node>> referenceParser =
        regexMatchResult("(references|refs|r)\\.(" + CODE + ")\\.id:(" + UUID + ")")
            .map(m -> new NodesByReference(m.group(2), UUIDs.fromString(m.group(3))));
    ParserCombinator<Specification<NodeId, Node>> referenceNullParser =
        regexMatchResult("(references|refs|r)\\.(" + CODE + ")\\.id:null")
            .map(m -> new NodesWithoutReferences(m.group(2)));
    ParserCombinator<Specification<NodeId, Node>> referencePathParser =
        regexMatchResult("(references|refs|r)\\.(" + CODE + ")\\.").next(attributeValueParser)
            .map(m -> new NodesByReferencePath(m.first.group(2), m.second));

    attributeValueParser.setCombinator(
        idParser
            .or(codeParser)
            .or(uriParser)
            .or(numberParser)
            .or(urnUuidParser)
            .or(createdDateParser)
            .or(lastModifiedDateParser)
            .or(graphIdParser)
            .or(graphCodeParser)
            .or(graphUriParser)
            .or(typeIdParser)
            .or(typeUriParser)
            .or(propertyStringQuotedParser)
            .or(propertyStringRangeParser)
            .or(propertyStringPrefixParser)
            .or(propertyStringParser)
            .or(propertyQuotedParser)
            .or(propertyPrefixParser)
            .or(propertyParser)
            .or(referenceParser)
            .or(referenceNullParser)
            .or(referencePathParser));

    queryParser.setCombinator(termParser.many(regex(" OR ")).map(OrSpecification::or));
    termParser.setCombinator(factorParser.many(string(" AND ")).map(AndSpecification::and));
    factorParser.setCombinator(string("NOT ").optional()
        .next(attributeValueParser.or(skip(string("(")).next(queryParser).skip(string(")"))))
        .map(r -> r.first.isPresent() ? not(r.second) : r.second)
        .next(regexMatchResult("\\^([0-9]+)").map(m -> parseFloat(m.group(1))).optional())
        .map(p -> p.second.isPresent() ? boost(p.first, p.second.get()) : p.first));

    parser = queryParser.end();
  }

  private LocalDateTime parseDate(String date) {
    return !date.equals("*") ? LocalDateTime.parse(date) : null;
  }

  @Override
  public Specification<NodeId, Node> apply(String specification) {
    try {
      return parser.apply(specification);
    } catch (ParseException e) {
      throw new NodeSpecificationParseException("Failed to parse node query", e);
    }
  }

}
