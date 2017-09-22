package fi.thl.termed.service.node.specification;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.ISO_8601_DATE;
import static fi.thl.termed.util.RegularExpressions.UUID;
import static org.jparsercombinator.ParserCombinators.newRef;
import static org.jparsercombinator.ParserCombinators.regex;
import static org.jparsercombinator.ParserCombinators.regexMatchResult;
import static org.jparsercombinator.ParserCombinators.skip;
import static org.jparsercombinator.ParserCombinators.string;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import java.util.Date;
import org.joda.time.DateTime;
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
            .map(m -> new NodeById(UUIDs.fromString(m.group(2))));
    ParserCombinator<Specification<NodeId, Node>> codeParser =
        regexMatchResult("code:(" + CODE + ")")
            .map(m -> new NodesByCode(m.group(1)));
    ParserCombinator<Specification<NodeId, Node>> urnUuidParser =
        regexMatchResult("urn:uuid:(" + UUID + ")")
            .map(m -> new NodeById(UUIDs.fromString(m.group(1))));
    ParserCombinator<Specification<NodeId, Node>> uriParser =
        regexMatchResult("uri:([^\\s]+)")
            .map(m -> new NodesByUri(m.group(1)));
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
    ParserCombinator<Specification<NodeId, Node>> typeIdParser =
        regexMatchResult("(type\\.id|typeId):(" + CODE + ")")
            .map(m -> new NodesByTypeId(m.group(2)));
    ParserCombinator<Specification<NodeId, Node>> propertyQuotedParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:\"([^\"]*)\"")
            .map(m -> new NodesByPropertyPhrase(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyPrefixParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:([^\\s]*)\\*")
            .map(m -> new NodesByPropertyPrefix(m.group(2), m.group(4), m.group(5)));
    ParserCombinator<Specification<NodeId, Node>> propertyParser =
        regexMatchResult("(properties|props|p)\\.(" + CODE + ")(\\.([a-z]{2}))?:([^\\s\\)\\*]*)")
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
            .or(urnUuidParser)
            .or(createdDateParser)
            .or(lastModifiedDateParser)
            .or(graphIdParser)
            .or(typeIdParser)
            .or(propertyQuotedParser)
            .or(propertyPrefixParser)
            .or(propertyParser)
            .or(referenceParser)
            .or(referenceNullParser)
            .or(referencePathParser));

    queryParser.setCombinator(termParser.many(regex(" OR ")).map(OrSpecification::new));
    termParser.setCombinator(factorParser.many(string(" AND ")).map(AndSpecification::new));
    factorParser.setCombinator(string("NOT ").optional()
        .next(attributeValueParser.or(skip(string("(")).next(queryParser).skip(string(")"))))
        .map(r -> r.first.isPresent() ? new NotSpecification<>(r.second) : r.second));

    parser = queryParser.end();
  }

  private Date parseDate(String date) {
    return !date.equals("*") ? new DateTime(date).toDate() : null;
  }

  @Override
  public Specification<NodeId, Node> apply(String specification) {
    return parser.apply(specification);
  }

}
