package fi.thl.termed.web.node.json;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByTextAttributeValuePrefix;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.MatchNone;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;
import static fi.thl.termed.util.specification.Query.Engine.SQL;

@RestController
@RequestMapping("/api")
public class NodeReadController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @GetJsonMapping("/nodes")
  public List<Node> get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    Specification<NodeId, Node> specification =
        query.isEmpty() ? nodesBy(typeIds(currentUser))
                        : nodesBy(textAttributeIds(currentUser), tokenize(query));
    return nodeService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/nodes")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    Specification<NodeId, Node> specification =
        query.isEmpty() ? nodesBy(typeIds(graphId, currentUser))
                        : nodesBy(textAttributeIds(graphId, currentUser), tokenize(query));
    return nodeService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {

    TypeId type = new TypeId(typeId, graphId);
    Specification<NodeId, Node> specification =
        query.isEmpty() ? nodesBy(typeId(type, currentUser))
                        : nodesBy(textAttributeIds(type, currentUser), tokenize(query));
    return nodeService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  public Node get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    return nodeService.get(new NodeId(id, typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new);
  }

  private Query<NodeId, Node> query(
      Specification<NodeId, Node> specification, List<String> orderBy, int max,
      boolean bypassIndex) {
    return new Query<>(specification, orderBy, max, bypassIndex ? SQL : LUCENE);
  }

  private Specification<NodeId, Node> nodesBy(TypeId typeId) {
    return typeId != null ? new NodesByTypeId(typeId)
                          : new MatchNone<>();
  }

  private Specification<NodeId, Node> nodesBy(List<TypeId> typeIds) {
    List<Specification<NodeId, Node>> specifications = Lists.newArrayList();
    for (TypeId typeId : typeIds) {
      specifications.add(new NodesByTypeId(typeId));
    }
    return new OrSpecification<>(specifications);
  }

  private Specification<NodeId, Node> nodesBy(List<TextAttributeId> textAttributeIds,
                                                  List<String> prefixQueries) {
    List<Specification<NodeId, Node>> specifications = Lists.newArrayList();
    for (TextAttributeId attributeId : textAttributeIds) {
      for (String prefixQuery : prefixQueries) {
        specifications.add(new NodesByTextAttributeValuePrefix(attributeId, prefixQuery));
      }
    }
    return new OrSpecification<>(specifications);
  }


  private List<TypeId> typeIds(UUID graphId, User user) {
    return typeService.getKeys(new Query<>(new TypesByGraphId(graphId)), user);
  }

  private List<TextAttributeId> textAttributeIds(UUID graphId, User user) {
    return typeService.get(new Query<>(new TypesByGraphId(graphId)), user).stream()
        .flatMap(cls -> cls.getTextAttributes().stream())
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<TypeId> typeIds(User user) {
    return typeService.getKeys(new Query<>(new MatchAll<>()), user);
  }

  private TypeId typeId(TypeId typeId, User user) {
    return typeService.get(typeId, user).map(TypeId::new).orElse(null);
  }

  private List<TextAttributeId> textAttributeIds(User user) {
    return typeService.get(new Query<>(new MatchAll<>()), user).stream()
        .flatMap(cls -> cls.getTextAttributes().stream())
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<TextAttributeId> textAttributeIds(TypeId typeId, User user) {
    return typeService.get(typeId, user).orElseThrow(NotFoundException::new)
        .getTextAttributes().stream()
        .map(TextAttributeId::new)
        .collect(Collectors.toList());
  }

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }


}
