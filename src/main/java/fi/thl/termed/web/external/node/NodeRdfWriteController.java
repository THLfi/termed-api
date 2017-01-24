package fi.thl.termed.web.external.node;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.transform.RdfModelToNodes;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/graphs/{graphId}/nodes")
public class NodeRdfWriteController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostRdfMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  private void post(@PathVariable("graphId") UUID graphId,
                    @AuthenticationPrincipal User currentUser,
                    @RequestParam(value = "stream", required = false, defaultValue = "false") boolean stream,
                    @RequestBody Model model) {
    log.info("Importing RDF-model {} (user: {})", graphId, currentUser.getUsername());

    graphService.get(new GraphId(graphId), currentUser).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.get(new TypesByGraphId(graphId), currentUser);
    List<Node> nodes = new RdfModelToNodes(types).apply(new JenaRdfModel(model));

    if (stream) {
      nodes.forEach(node -> nodeService.save(node, currentUser));
    } else {
      nodeService.save(nodes, currentUser);
    }
  }

}
