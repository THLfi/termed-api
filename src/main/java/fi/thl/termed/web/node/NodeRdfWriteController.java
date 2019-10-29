package fi.thl.termed.web.node;

import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.RdfModelToNodes;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PatchRdfMapping;
import fi.thl.termed.util.spring.annotation.PostRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.HttpPreconditions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
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
  private void post(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "importCodes", defaultValue = "true") boolean importCodes,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @RequestBody Model model,
      @AuthenticationPrincipal User user) {

    HttpPreconditions.checkFound(
        graphService.exists(GraphId.of(graphId), user),
        "Graph not found.");

    log.info("Importing RDF-model {} (user: {})", graphId, user.getUsername());

    List<Type> types = toImmutableListAndClose(
        typeService.values(Queries.query(TypesByGraphId.of(graphId)), user));
    List<Node> nodes = new RdfModelToNodes(types, q -> nodeService.keys(q, user), importCodes)
        .apply(new JenaRdfModel(model));

    nodeService.save(nodes.stream(), saveMode(mode), opts(sync, generateCodes, generateUris), user);
  }

  @PatchRdfMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  private void patch(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "update") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "append", defaultValue = "true") boolean append,
      @RequestParam(name = "lenient", defaultValue = "false") boolean lenient,
      @RequestBody Model model,
      @AuthenticationPrincipal User user) {

    HttpPreconditions.checkRequestParam(
        mode.matches("update|upsert"),
        "Use mode \"update\" or \"upsert \" when patching existing nodes.");

    HttpPreconditions.checkFound(
        graphService.exists(GraphId.of(graphId), user),
        "Graph not found.");

    log.info("Patching RDF-model {} (user: {})", graphId, user.getUsername());

    List<Type> types = toImmutableListAndClose(
        typeService.values(Queries.query(TypesByGraphId.of(graphId)), user));
    List<Node> nodes = new RdfModelToNodes(types, q -> nodeService.keys(q, user), false)
        .apply(new JenaRdfModel(model));

    Stream<Node> patchedNodesStream = nodes.stream()
        .map(patch -> Tuple.of(patch, nodeService.get(patch.identifier(), user)))
        .filter(patchAndBaseNode -> {
          Node patch = patchAndBaseNode._1;
          Optional<Node> optionalBaseNode = patchAndBaseNode._2;

          if (optionalBaseNode.isPresent()) {
            return true;
          } else if (lenient) {
            log.warn("Skipping patch for {} (user: {})", patch.identifier(), user.getUsername());
            return false;
          } else {
            throw new NotFoundException(String.format(
                "Node not found for patch %s (user: %s)", patch.identifier(), user.getUsername()));
          }
        })
        .map(patchAndBaseNode -> {
          Node patch = patchAndBaseNode._1;
          Node baseNode = patchAndBaseNode._2.orElseThrow(IllegalStateException::new);

          Node.Builder result = Node.builderFromCopyOf(baseNode);

          if (append) {
            patch.getProperties().forEach(result::addUniqueProperty);
            patch.getReferences().forEach(result::addUniqueReference);
          } else {
            patch.getProperties().asMap().forEach(result::replaceProperty);
            patch.getReferences().asMap().forEach(result::replaceReference);
          }

          return result.build();
        });

    nodeService.save(patchedNodesStream, saveMode(mode), opts(sync), user);
  }

}
