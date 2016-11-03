package fi.thl.termed.web.system.type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/graphs/{graphId}/types")
public class TypeWriteController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Type save(
      @PathVariable("graphId") UUID graphId,
      @RequestBody Type type,
      @AuthenticationPrincipal User user) {
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);
    type.setGraph(new GraphId(graphId));
    return typeService.get(typeService.save(type, user), user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(
      @PathVariable("graphId") UUID graphId,
      @RequestBody List<Type> types,
      @AuthenticationPrincipal User currentUser) {
    types.forEach(type -> type.setGraph(new GraphId(graphId)));
    typeService.save(types, currentUser);
  }

  @PutJsonMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Type save(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @RequestBody Type type,
      @AuthenticationPrincipal User user) {
    type.setGraph(new GraphId(graphId));
    type.setId(id);
    return typeService.get(typeService.save(type, user), user).orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(path = "/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user) {
    typeService.delete(new TypeId(id, graphId), user);
  }

}
