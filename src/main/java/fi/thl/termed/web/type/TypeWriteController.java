package fi.thl.termed.web.type;

import static fi.thl.termed.util.collect.SetUtils.toImmutableSet;
import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.apache.jena.ext.com.google.common.collect.Sets.difference;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.transaction.TransactionUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
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
  private Service2<TypeId, Type> typeService;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

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
      @RequestBody Stream<Type> types,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    typeService.save(
        types.map(type -> Type.builder()
            .id(type.getId(), graphId)
            .copyOptionalsFrom(type).build()),
        saveMode(mode), opts(sync), user);
  }

  @PutJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void replace(
      @PathVariable("graphId") UUID graphId,
      @RequestBody Stream<Type> types,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {

    try (Stream<TypeId> typeIdStream = typeService
        .keys(new Query<>(new TypesByGraphId(graphId)), user)) {

      List<Type> typeList = types.map(
          type -> Type.builder()
              .id(type.getId(), graphId)
              .copyOptionalsFrom(type).build())
          .collect(Collectors.toList());

      Set<TypeId> oldTypes = typeIdStream.collect(toImmutableSet());
      Set<TypeId> newTypes = typeList.stream().map(Type::identifier).collect(toImmutableSet());

      try {
        TransactionUtils.runInTransaction(transactionManager, () -> {
          typeService.save(typeList.stream(), saveMode(mode), opts(sync), user);
          typeService.delete(difference(oldTypes, newTypes).stream(), opts(sync), user);
          return null;
        });
      } catch (RuntimeException | Error e) {
        eventBus.post(new InvalidateCachesEvent());
      }
    }
  }

  @PutJsonMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Type save(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @RequestBody Type type,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    return typeService.get(typeService.save(
        Type.builder()
            .id(id, graphId)
            .copyOptionalsFrom(type)
            .build(), saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    typeService.delete(
        typeService.keys(new Query<>(new TypesByGraphId(graphId)), user), opts(sync), user);
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
