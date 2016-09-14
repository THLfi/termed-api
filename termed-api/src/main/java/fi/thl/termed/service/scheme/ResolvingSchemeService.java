package fi.thl.termed.service.scheme;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.sql.SchemeByCode;
import fi.thl.termed.spesification.sql.SchemeByUri;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;

import static com.google.common.collect.Iterables.getFirst;
import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

/**
 * Resolves scheme URIs and codes for actual ids. Useful to run before persisting a scheme.
 */
public class ResolvingSchemeService extends ForwardingService<UUID, Scheme> {

  private SystemDao<UUID, Scheme> schemeDao;

  public ResolvingSchemeService(Service<UUID, Scheme> delegate,
                                SystemDao<UUID, Scheme> schemeDao) {
    super(delegate);
    this.schemeDao = schemeDao;
  }

  @Override
  public void save(List<Scheme> schemes, User currentUser) {
    for (Scheme scheme : schemes) {
      resolveScheme(scheme);
    }
    super.save(schemes, currentUser);
  }

  @Override
  public void save(Scheme scheme, User currentUser) {
    resolveScheme(scheme);
    super.save(scheme, currentUser);
  }

  private void resolveScheme(Scheme scheme) {
    if (scheme.getId() == null) {
      scheme.setId(coalesce(resolveSchemeIdForCode(scheme.getCode()),
                            resolveSchemeIdForUri(scheme.getUri()),
                            UUIDs.nameUUIDFromString(scheme.getCode()),
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
              resolveSchemeIdForCode(rangeScheme.getCode()),
              resolveSchemeIdForUri(rangeScheme.getUri()),
              UUIDs.nameUUIDFromString(rangeScheme.getCode()),
              UUIDs.nameUUIDFromString(rangeScheme.getUri()),
              scheme.getId())));
        }
        range.setScheme(rangeScheme);
        referenceAttribute.setRange(range);
      }
    }
  }

  private UUID resolveSchemeIdForCode(String code) {
    return code != null ? getFirst(schemeDao.getKeys(new SchemeByCode(code)), null) : null;
  }

  private UUID resolveSchemeIdForUri(String uri) {
    return uri != null ? getFirst(schemeDao.getKeys(new SchemeByUri(uri)), null) : null;
  }

}
