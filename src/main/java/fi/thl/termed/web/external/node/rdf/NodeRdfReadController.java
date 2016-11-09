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
import java.util.stream.Collectors;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.StringUtils;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyTextAttributeValuePrefix;
import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyType;
import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

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

  @GetRdfMapping("/nodes")
  public Model get(@PathVariable("graphId") UUID graphId,
                   @RequestParam(value = "query", required = false, defaultValue = "") String query,
                   @AuthenticationPrincipal User user) {
    log.info("Exporting RDF-model {} (user: {})", graphId, user.getUsername());
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (query.isEmpty()) {
      spec = byAnyType(typeService.getKeys(new TypesByGraphId(graphId), user));
    } else {
      spec = byAnyTextAttributeValuePrefix(
          typeService.get(new TypesByGraphId(graphId), user).stream()
              .flatMap(cls -> cls.getTextAttributeIds().stream())
              .collect(Collectors.toList()),
          StringUtils.split(query, "\\s"));
    }

    List<Node> nodes = nodeService.get(new Query<>(spec, LUCENE), user).getValues();
    List<Type> types = typeService.get(new Query<>(new TypesByGraphId(graphId)), user).getValues();

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
    List<Type> types = typeService.get(new Query<>(new TypesByGraphId(graphId)), user).getValues();

    return new JenaRdfModel(new NodesToRdfModel(
        types, nodeId -> nodeService.get(nodeId, user)).apply(node)).getModel();
  }

}
