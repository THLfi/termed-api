package fi.thl.termed.web.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.gson.Gson;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeSpecifications;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.json.JsonStream;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PatchJsonMapping;
import fi.thl.termed.util.spring.http.HttpPreconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodePatchController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Gson gson;

  @PatchJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ResponseStatus(NO_CONTENT)
  public void patchBatchOfNodesOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "append", defaultValue = "true") boolean append,
      @RequestParam(name = "mode", defaultValue = "update") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "lenient", defaultValue = "false") boolean lenient,
      @AuthenticationPrincipal User user,
      HttpServletRequest request) throws IOException {

    HttpPreconditions.checkRequestParam(mode.matches("update|upsert"),
        "Use mode \"update\" or \"upsert \" when patching existing nodes.");
    HttpPreconditions.checkFound(typeService.exists(TypeId.of(typeId, graphId), user),
        "Type not found.");

    try (InputStream input = request.getInputStream()) {
      TypeId type = TypeId.of(typeId, graphId);

      Stream<Node> patchStream = JsonStream.readArray(gson, Node.class, input)
          .map(patch -> Tuple.of(patch, nodeService.get(NodeId.of(patch.getId(), type), user)))
          .filter(patchAndBaseNode -> !lenient || patchAndBaseNode._2.isPresent())
          .map(patchAndBaseNode -> {
            Node patch = HttpPreconditions.checkRequestParamNotNull(patchAndBaseNode._1,
                "Each node in batch should have and ID.");
            Node baseNode = HttpPreconditions.checkFound(patchAndBaseNode._2,
                () -> "Node " + NodeId.of(patch.getId(), type) + " not found.");

            Node.Builder result = Node.builderFromCopyOf(baseNode);

            patch.getCode().ifPresent(result::code);
            patch.getUri().ifPresent(result::uri);
            if (append) {
              patch.getProperties().forEach(result::addProperty);
              patch.getReferences().forEach(result::addReference);
            } else {
              patch.getProperties().asMap().forEach(result::replaceProperty);
              patch.getReferences().asMap().forEach(result::replaceReference);
            }
            return result.build();
          });

      nodeService.save(patchStream, saveMode(mode), opts(sync), user);
    }
  }

  @PatchJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "where", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ResponseStatus(NO_CONTENT)
  public void patchMatchingNodesOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(name = "append", defaultValue = "true") boolean append,
      @RequestParam(name = "mode", defaultValue = "update") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node patch,
      @AuthenticationPrincipal User user) {

    HttpPreconditions.checkRequestParam(mode.matches("update|upsert"),
        "Use mode \"update\" or \"upsert \" when patching existing nodes.");
    Type domain = HttpPreconditions.checkFound(
        typeService.get(TypeId.of(typeId, graphId), user), "Type not found.");

    Specification<NodeId, Node> spec =
        NodeSpecifications.specifyByQuery(
            StreamUtils.toImmutableListAndClose(graphService.values(Queries.matchAll(), user)),
            StreamUtils.toImmutableListAndClose(typeService.values(Queries.matchAll(), user)),
            domain,
            where);

    try (Stream<Node> baseNodes = nodeService.values(Queries.query(spec), user)) {
      nodeService.save(baseNodes.map(baseNode -> {
        Node.Builder result = Node.builderFromCopyOf(baseNode);

        patch.getCode().ifPresent(result::code);
        patch.getUri().ifPresent(result::uri);

        if (append) {
          patch.getProperties().forEach(result::addProperty);
          patch.getReferences().forEach(result::addReference);
        } else {
          patch.getProperties().asMap().forEach(result::replaceProperty);
          patch.getReferences().asMap().forEach(result::replaceReference);
        }

        return result.build();
      }), saveMode(mode), opts(sync), user);
    }
  }

  @PatchJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node patchOneNodeById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "append", defaultValue = "true") boolean append,
      @RequestParam(name = "mode", defaultValue = "update") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node patch,
      @AuthenticationPrincipal User user) {

    HttpPreconditions.checkRequestParam(mode.matches("update|upsert"),
        "Use mode \"update\" or \"upsert \" when patching existing nodes.");
    HttpPreconditions.checkFound(typeService.exists(TypeId.of(typeId, graphId), user),
        "Type not found.");

    Node.Builder result = Node.builderFromCopyOf(
        HttpPreconditions.checkFound(nodeService.get(NodeId.of(id, typeId, graphId), user)));

    patch.getCode().ifPresent(result::code);
    patch.getUri().ifPresent(result::uri);

    if (append) {
      patch.getProperties().forEach(result::addProperty);
      patch.getReferences().forEach(result::addReference);
    } else {
      patch.getProperties().asMap().forEach(result::replaceProperty);
      patch.getReferences().asMap().forEach(result::replaceReference);
    }

    NodeId nodeId = nodeService.save(result.build(), saveMode(mode), opts(sync), user);

    return HttpPreconditions.checkFound(nodeService.get(nodeId, user), "Patched node not found.");
  }

}
