package fi.thl.termed.web.system.type;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TypeReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @GetJsonMapping("/types")
  public List<Type> getTypes(@AuthenticationPrincipal User currentUser) {
    return typeService.getValues(currentUser);
  }

  @GetJsonMapping("/graphs/{graphId}/types")
  public List<Type> getTypes(
      @PathVariable("graphId") UUID graphId,
      @AuthenticationPrincipal User currentUser) {
    graphService.get(GraphId.of(graphId), currentUser).orElseThrow(NotFoundException::new);
    return typeService.getValues(new TypesByGraphId(graphId), currentUser);
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
  public List<TextAttribute> getTextAttributes(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new).getTextAttributes();
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
  public List<ReferenceAttribute> getReferenceAttributes(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    return typeService.get(TypeId.of(typeId, graphId), currentUser)
        .orElseThrow(NotFoundException::new).getReferenceAttributes();
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
