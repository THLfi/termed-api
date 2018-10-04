package fi.thl.termed.web.admin;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.MultimapUtils.renameKey;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.transaction.TransactionUtils;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TypeAdminController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/textAttributes/{attributeId}", params = "newId", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void changeTextAttributeId(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("attributeId") String attributeId,
      @RequestParam("newId") String newAttributeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {

    if (user.getAppRole() != AppRole.SUPERUSER && user.getAppRole() != AppRole.ADMIN) {
      throw new AccessDeniedException("");
    }

    if (attributeId.equals(newAttributeId)) {
      return;
    }

    log.warn("Changing text attribute id from {} to {} (user: {})",
        attributeId, newAttributeId, user.getUsername());

    TransactionUtils.runInTransaction(transactionManager, () -> {
      Type type = typeService.get(TypeId.of(typeId, graphId), user)
          .orElseThrow(NotFoundException::new);
      TextAttribute textAttribute = type.getTextAttributes().stream()
          .filter(a -> a.getId().equals(attributeId))
          .findFirst()
          .orElseThrow(NotFoundException::new);

      Type typeWithNewAttrIdAdded = Type.builderFromCopyOf(type)
          .textAttributes(ImmutableList.<TextAttribute>builder()
              .addAll(type.getTextAttributes().stream()
                  .map(a -> !a.getId().equals(attributeId) ? a
                      : TextAttribute.builder()
                          .id(newAttributeId, a.getDomain())
                          .regex(a.getRegex()).build())
                  .collect(toImmutableList()))
              .add(TextAttribute.builder()
                  .id(textAttribute.getId(), textAttribute.getDomain())
                  .regex(textAttribute.getRegex())
                  .build())
              .build()).build();

      typeService.save(typeWithNewAttrIdAdded, SaveMode.UPDATE, opts(sync), user);

      try (Stream<Node> stream = nodeService.values(new Query<>(
          and(new NodesByGraphId(graphId),
              new NodesByTypeId(typeId))), user)) {

        Stream<Node> nodesWithRenamedTextAttribute = stream
            .filter(n -> n.getProperties().containsKey(attributeId))
            .map(n -> Node.builderFromCopyOf(n)
                .properties(renameKey(n.getProperties(), attributeId, newAttributeId))
                .build());

        nodeService.save(nodesWithRenamedTextAttribute, SaveMode.UPDATE, opts(sync), user);
      }

      Type typeWithOldAttrIdRemoved = Type.builderFromCopyOf(typeWithNewAttrIdAdded)
          .textAttributes(typeWithNewAttrIdAdded.getTextAttributes().stream()
              .filter(a -> !a.getId().equals(attributeId))
              .collect(toImmutableList()))
          .build();

      typeService.save(typeWithOldAttrIdRemoved, SaveMode.UPDATE, opts(sync), user);

      Type typeWithNewAttrFullMetadata =
          Type.builderFromCopyOf(typeWithOldAttrIdRemoved)
              .textAttributes(typeWithOldAttrIdRemoved.getTextAttributes().stream()
                  .map(a -> !a.getId().equals(newAttributeId) ? a :
                      TextAttribute.builder()
                          .id(a.getId(), a.getDomain())
                          .regex(a.getRegex())
                          .copyOptionalsFrom(textAttribute)
                          .build())
                  .collect(toImmutableList()))
              .build();

      typeService.save(typeWithNewAttrFullMetadata, SaveMode.UPDATE, opts(sync), user);

      return null;
    }, (error) -> {
      eventBus.post(new InvalidateCachesEvent());
      eventBus.post(new ReindexEvent<>(() -> nodeService.keys(new Query<>(
          and(new NodesByGraphId(graphId),
              new NodesByTypeId(typeId))), user)));
    });
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/referenceAttributes/{attributeId}", params = "newId", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void changeReferenceAttributeId(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("attributeId") String attributeId,
      @RequestParam("newId") String newAttributeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {

    if (user.getAppRole() != AppRole.SUPERUSER && user.getAppRole() != AppRole.ADMIN) {
      throw new AccessDeniedException("");
    }

    if (attributeId.equals(newAttributeId)) {
      return;
    }

    log.warn("Changing reference attribute id from {} to {} (user: {})",
        attributeId, newAttributeId, user.getUsername());

    TransactionUtils.runInTransaction(transactionManager, () -> {
      Type type = typeService.get(TypeId.of(typeId, graphId), user)
          .orElseThrow(NotFoundException::new);
      ReferenceAttribute referenceAttribute = type.getReferenceAttributes().stream()
          .filter(a -> a.getId().equals(attributeId))
          .findFirst()
          .orElseThrow(NotFoundException::new);

      Type typeWithNewAttrAdded = Type.builderFromCopyOf(type)
          .referenceAttributes(ImmutableList.<ReferenceAttribute>builder()
              .addAll(type.getReferenceAttributes().stream()
                  .map(a -> !a.getId().equals(attributeId) ? a
                      : ReferenceAttribute.builder()
                          .id(newAttributeId, a.getDomain())
                          .range(a.getRange()).build())
                  .collect(toImmutableList()))
              .add(ReferenceAttribute.builder()
                  .id(referenceAttribute.getId(), referenceAttribute.getDomain())
                  .range(referenceAttribute.getRange())
                  .build())
              .build()).build();

      typeService.save(typeWithNewAttrAdded, SaveMode.UPDATE, opts(sync), user);

      try (Stream<Node> stream = nodeService.values(new Query<>(
          and(new NodesByGraphId(graphId),
              new NodesByTypeId(typeId))), user)) {

        Stream<Node> nodesWithRenamedReferenceAttribute = stream
            .filter(n -> n.getReferences().containsKey(attributeId))
            .map(n -> Node.builderFromCopyOf(n)
                .references(renameKey(n.getReferences(), attributeId, newAttributeId))
                .build());

        nodeService.save(nodesWithRenamedReferenceAttribute, SaveMode.UPDATE, opts(sync), user);
      }

      Type typeWithOldAttrRemoved =
          Type.builderFromCopyOf(typeWithNewAttrAdded)
              .referenceAttributes(typeWithNewAttrAdded.getReferenceAttributes().stream()
                  .filter(a -> !a.getId().equals(attributeId))
                  .collect(toImmutableList()))
              .build();

      typeService.save(typeWithOldAttrRemoved, SaveMode.UPDATE, opts(sync), user);

      Type typeWithNewAttrFullMetadata =
          Type.builderFromCopyOf(typeWithOldAttrRemoved)
              .referenceAttributes(typeWithOldAttrRemoved.getReferenceAttributes().stream()
                  .map(a -> !a.getId().equals(newAttributeId) ? a : ReferenceAttribute.builder()
                      .id(a.getId(), a.getDomain())
                      .range(a.getRange())
                      .copyOptionalsFrom(referenceAttribute)
                      .build())
                  .collect(toImmutableList()))
              .build();

      typeService.save(typeWithNewAttrFullMetadata, SaveMode.UPDATE, opts(sync), user);

      return null;
    }, (error) -> {
      eventBus.post(new InvalidateCachesEvent());
      eventBus.post(new ReindexEvent<>(() -> nodeService.keys(new Query<>(
          and(new NodesByGraphId(graphId),
              new NodesByTypeId(typeId))), user)));
    });
  }

}
