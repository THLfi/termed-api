package fi.thl.termed.service.scheme.internal;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

public class InitializingSchemeService extends ForwardingService<SchemeId, Scheme> {

  public InitializingSchemeService(Service<SchemeId, Scheme> delegate) {
    super(delegate);
  }

  @Override
  public List<SchemeId> save(List<Scheme> schemes, User currentUser) {
    schemes.forEach(this::initialize);
    return super.save(schemes, currentUser);
  }

  @Override
  public SchemeId save(Scheme scheme, User currentUser) {
    initialize(scheme);
    return super.save(scheme, currentUser);
  }

  private void initialize(Scheme scheme) {
    if (scheme.getId() == null) {
      scheme.setId(coalesce(UUIDs.nameUUIDFromString(scheme.getCode()),
                            UUIDs.nameUUIDFromString(scheme.getUri()),
                            randomUUID()));
    }
  }

}
