package fi.thl.termed.web.system.dump;

import static fi.thl.termed.util.collect.StreamUtils.toStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dump")
public class DumpController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Gson gson;

  @GetJsonMapping
  public void dump(@AuthenticationPrincipal User user, HttpServletResponse response)
      throws IOException {

    response.setContentType(APPLICATION_JSON_UTF8_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    try (Writer writer = new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
      writeJson(
          graphService.get(user).iterator(),
          typeService.get(user).iterator(),
          nodeService.get(user).iterator(),
          writer);
    }
  }

  @GetJsonMapping(params = "graphId")
  public void dump(@RequestParam("graphId") List<UUID> ids,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    response.setContentType(APPLICATION_JSON_UTF8_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    try (Writer writer = new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
      writeJson(
          ids.stream().flatMap(id -> toStream(graphService.get(new GraphId(id), user))).iterator(),
          ids.stream().flatMap(id -> typeService.get(new TypesByGraphId(id), user)).iterator(),
          ids.stream().flatMap(id -> nodeService.get(new NodesByGraphId(id), user)).iterator(),
          writer);
    }
  }

  // write outer object manually to to get nicely formatted dump, actual data is written with gson
  private void writeJson(Iterator<Graph> graphs, Iterator<Type> types, Iterator<Node> nodes,
      Writer w) throws IOException {
    JsonWriter jsonWriter = new JsonWriter(w);

    w.write("{\n");
    w.write("  \"graphs\":[\n");
    while (graphs.hasNext()) {
      w.write("    ");
      gson.toJson(graphs.next(), Graph.class, jsonWriter);
      w.write(graphs.hasNext() ? ",\n" : "\n");
    }
    w.write("  ],\n");
    w.write("  \"types\":[\n");
    while (types.hasNext()) {
      w.write("    ");
      gson.toJson(types.next(), Type.class, jsonWriter);
      w.write(types.hasNext() ? ",\n" : "\n");
    }
    w.write("  ],\n");
    w.write("  \"nodes\":[\n");
    while (nodes.hasNext()) {
      w.write("    ");
      gson.toJson(nodes.next(), Node.class, jsonWriter);
      w.write(nodes.hasNext() ? ",\n" : "\n");
    }
    w.write("  ]\n");
    w.write("}");
  }

}
