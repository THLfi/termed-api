package fi.thl.termed.web.external.node.rdf;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByPropertyPrefix;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.util.StringUtils.tokenize;

@RestController
@RequestMapping("/api/graphs/{graphId}")
public class NodeRdfReadController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  private Specification<NodeId, Node> toPrefixQuery(List<TextAttribute> attrs, String q) {
    OrSpecification<NodeId, Node> spec = new OrSpecification<>();
    tokenize(q).forEach(t -> attrs.forEach(a -> spec.or(new NodesByPropertyPrefix(a.getId(), t))));
    return spec;
  }

  @GetRdfMapping("/nodes")
  public Model get(@PathVariable("graphId") UUID graphId,
                   @RequestParam(value = "query", required = false, defaultValue = "") String query,
                   @AuthenticationPrincipal User user) {
    log.info("Exporting RDF-model {} (user: {})", graphId, user.getUsername());
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    OrSpecification<NodeId, Node> spec = new OrSpecification<>();

    typeService.get(new TypesByGraphId(graphId), user).forEach(type -> {
      AndSpecification<NodeId, Node> typeSpec = new AndSpecification<>();
      typeSpec.and(new NodesByTypeId(type.getId()));
      typeSpec.and(new NodesByGraphId(type.getGraphId()));
      if (!query.isEmpty()) {
        typeSpec.and(toPrefixQuery(type.getTextAttributes(), query));
      }
      spec.or(typeSpec);
    });

    List<Node> nodes = nodeService.get(spec, user);
    List<Type> types = typeService.get(new TypesByGraphId(graphId), user);

    return new JenaRdfModel(new NodesToRdfModel(
        types, nodeId -> nodeService.get(nodeId, user)).apply(nodes)).getModel();
  }

  @GetRdfMapping("/types/{typeId}/nodes/{id}")
  public Model get(@PathVariable("graphId") UUID graphId,
                   @PathVariable("typeId") String typeId,
                   @PathVariable("id") UUID id,
                   @AuthenticationPrincipal User user) {
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Node> node = nodeService.get(new NodeId(id, typeId, graphId), user)
        .map(Collections::singletonList).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.get(new TypesByGraphId(graphId), user);

    return new JenaRdfModel(new NodesToRdfModel(
        types, nodeId -> nodeService.get(nodeId, user)).apply(node)).getModel();
  }

}
