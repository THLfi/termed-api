package fi.thl.termed.service.impl;

import com.google.common.base.Function;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.spesification.sql.SchemeByCode;
import fi.thl.termed.spesification.sql.SchemeByUri;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;

import static com.google.common.collect.Iterables.getFirst;
import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

/**
 * Tries to resolve scheme, class and attribute IDs.
 */
public class SchemeIdResolver implements Function<Scheme, Scheme> {

  private Dao<UUID, Scheme> schemeDao;

  public SchemeIdResolver(Dao<UUID, Scheme> schemeDao) {
    this.schemeDao = schemeDao;
  }

  @Override
  public Scheme apply(Scheme scheme) {
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
        textAttribute.setDomain(new Class(new ClassId(cls)));
        textAttribute.setRegex(coalesce(textAttribute.getRegex(),
                                        RegularExpressions.ALL));
      }

      for (ReferenceAttribute referenceAttribute : cls.getReferenceAttributes()) {
        referenceAttribute.setDomain(new Class(new ClassId(cls)));

        Class range = coalesce(referenceAttribute.getRange(), new Class(new ClassId(cls)));
        Scheme rangeScheme = coalesce(range.getScheme(), scheme);
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

    return scheme;
  }

  private UUID resolveSchemeIdForCode(String code) {
    return code != null ? getFirst(schemeDao.getKeys(new SchemeByCode(code)), null) : null;
  }

  private UUID resolveSchemeIdForUri(String uri) {
    return uri != null ? getFirst(schemeDao.getKeys(new SchemeByUri(uri)), null) : null;
  }

}
