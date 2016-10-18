package fi.thl.termed.service.scheme.internal;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

public class ResolvingSchemeService extends ForwardingService<UUID, Scheme> {

  public ResolvingSchemeService(Service<UUID, Scheme> delegate) {
    super(delegate);
  }

  @Override
  public List<UUID> save(List<Scheme> schemes, User currentUser) {
    schemes.forEach(this::resolveScheme);
    return super.save(schemes, currentUser);
  }

  @Override
  public UUID save(Scheme scheme, User currentUser) {
    resolveScheme(scheme);
    return super.save(scheme, currentUser);
  }

  private void resolveScheme(Scheme scheme) {
    if (scheme.getId() == null) {
      scheme.setId(coalesce(UUIDs.nameUUIDFromString(scheme.getCode()),
                            UUIDs.nameUUIDFromString(scheme.getUri()),
                            randomUUID()));
    }

    for (Class cls : scheme.getClasses()) {
      cls.setScheme(new Scheme(scheme.getId()));

      for (TextAttribute textAttribute : cls.getTextAttributes()) {
        textAttribute.setDomain(cls);
        textAttribute.setRegex(coalesce(textAttribute.getRegex(), RegularExpressions.ALL));
      }

      for (ReferenceAttribute referenceAttribute : cls.getReferenceAttributes()) {
        referenceAttribute.setDomain(cls);

        Class range = coalesce(referenceAttribute.getRange(), cls);
        Scheme rangeScheme = coalesce(range.getScheme(), new Scheme(scheme.getId()));
        if (rangeScheme.getId() == null) {
          range.setScheme(new Scheme(coalesce(
              UUIDs.nameUUIDFromString(rangeScheme.getCode()),
              UUIDs.nameUUIDFromString(rangeScheme.getUri()),
              scheme.getId())));
        }
        range.setScheme(rangeScheme);
        referenceAttribute.setRange(range);
      }
    }
  }

}
