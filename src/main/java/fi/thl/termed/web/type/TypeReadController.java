package fi.thl.termed.web.type;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TypeReadController {

  @Autowired
  private Service2<GraphId, Graph> graphService;

  @Autowired
  private Service2<TypeId, Type> typeService;

  @GetJsonMapping("/types")
  public Stream<Type> getTypes(@AuthenticationPrincipal User currentUser) {
    return typeService.values(new Query<>(new MatchAll<>()), currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/types")
  public Stream<Type> getTypes(
      @PathVariable("graphId") UUID graphId,
      @AuthenticationPrincipal User currentUser) {
    graphService.get(GraphId.of(graphId), currentUser).orElseThrow(NotFoundException::new);
    return typeService.values(new Query<>(new TypesByGraphId(graphId)), currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}")
  public Type getType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/textAttributes")
  public Stream<TextAttribute> getTextAttributes(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new).getTextAttributes().stream();
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/textAttributes/{attributeId}")
  public TextAttribute getTextAttribute(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new)
        .getTextAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/referenceAttributes")
  public Stream<ReferenceAttribute> getReferenceAttributes(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new).getReferenceAttributes().stream();
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/referenceAttributes/{attributeId}")
  public ReferenceAttribute getReferenceAttribute(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("attributeId") String attributeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new)
        .getReferenceAttributes().stream()
        .filter(attr -> Objects.equals(attr.getId(), attributeId))
        .findFirst().orElseThrow(NotFoundException::new);
  }

}
