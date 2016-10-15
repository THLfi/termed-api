package fi.thl.termed.web;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.User;

public interface ResourceController {

  List<Resource> get(String query, List<String> orderBy, int max, boolean bypassIndex,
                     User currentUser);

  List<Resource> get(UUID schemeId, String query, List<String> orderBy, int max,
                     boolean bypassIndex, User currentUser);

  List<Resource> get(UUID schemeId, String typeId, String query, List<String> orderBy, int max,
                     boolean bypassIndex,
                     User currentUser);

  Resource get(UUID schemeId, String typeId, UUID id, User currentUser);

  void post(UUID schemeId, String typeId, List<Resource> resources, User currentUser);

  Resource post(UUID schemeId, String typeId, Resource resource, User currentUser);

  void post(UUID schemeId, List<Resource> resources, User currentUser);

  Resource post(UUID schemeId, Resource resource, User currentUser);

  void post(List<Resource> resources, User currentUser);

  Resource post(Resource resource, User currentUser);

  Resource put(UUID schemeId, String typeId, UUID id, Resource resources, User currentUser);

  void delete(UUID schemeId, String typeId, UUID id, User currentUser);

}
