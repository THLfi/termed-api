package fi.thl.termed.web.type;

import static com.google.common.collect.ImmutableList.copyOf;
import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.Sets.difference;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs/{graphId}/types")
public class TypeWriteController {

  @Autowired
  private Service<TypeId, Type> typeService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Type save(
      @PathVariable("graphId") UUID graphId,
      @RequestBody Type type,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    type = Type.builder().id(type.getId(), graphId).copyOptionalsFrom(type).build();
    return typeService.get(typeService.save(type, saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(
      @PathVariable("graphId") UUID graphId,
      @RequestBody List<Type> types,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    types.replaceAll(type ->
        Type.builder().id(type.getId(), graphId).copyOptionalsFrom(type).build());
    typeService.save(types, saveMode(mode), opts(sync), user);
  }

  @PutJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void replace(
      @PathVariable("graphId") UUID graphId,
      @RequestBody List<Type> types,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    types.replaceAll(type ->
        Type.builder().id(type.getId(), graphId).copyOptionalsFrom(type).build());

    Set<TypeId> oldTypes = ImmutableSet.copyOf(
        typeService.getKeys(new TypesByGraphId(graphId), user));
    Set<TypeId> newTypes = types.stream().map(Type::identifier).collect(toSet());

    typeService.saveAndDelete(types, copyOf(difference(oldTypes, newTypes)), saveMode(mode),
        opts(sync), user);
  }

  @PutJsonMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Type save(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @RequestBody Type type,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    type = Type.builder().id(id, graphId).copyOptionalsFrom(type).build();
    return typeService.get(typeService.save(type, saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    typeService.delete(typeService.getKeys(new TypesByGraphId(graphId), user), opts(sync), user);
  }

  @DeleteMapping(path = "/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    typeService.delete(new TypeId(id, graphId), opts(sync), user);
  }

}
