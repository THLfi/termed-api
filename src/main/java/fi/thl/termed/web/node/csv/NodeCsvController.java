package fi.thl.termed.web.node.csv;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.json.JsonUtils;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

@RestController
@RequestMapping("/api/graphs/{graphId}/nodes")
public class NodeCsvController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Gson gson;

  @PostMapping(consumes = "text/csv;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void post(@PathVariable("graphId") UUID graphId,
                   @AuthenticationPrincipal User currentUser,
                   HttpServletRequest request) throws IOException {
    List<String[]> rows = new CSVReader(request.getReader()).readAll();
    List<Node> nodes = Lists.newArrayList();
    for (Map<String, String> row : TableUtils.toMapped(rows)) {
      nodes.add(gson.fromJson(JsonUtils.unflatten(row), Node.class));
    }
    nodes.forEach(r -> r.setType(new TypeId(r.getTypeId(), graphId)));
    nodeService.save(nodes, currentUser);
  }

  @GetMapping(produces = "text/csv;charset=UTF-8")
  public void get(@PathVariable("graphId") UUID graphId,
                  @AuthenticationPrincipal User currentUser,
                  HttpServletResponse response) throws IOException {
    List<Node> nodes = nodeService.get(
        new Query<>(new NodesByGraphId(graphId), LUCENE), currentUser);
    List<Map<String, String>> rows = Lists.newArrayList();
    nodes.forEach(r -> rows.add(JsonUtils.flatten(gson.toJsonTree(r, Node.class))));
    new CSVWriter(response.getWriter()).writeAll(TableUtils.toTable(rows));
  }

}
