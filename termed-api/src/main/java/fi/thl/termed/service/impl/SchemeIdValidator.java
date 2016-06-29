package fi.thl.termed.service.impl;

import com.google.common.base.Function;

import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.util.ErrorCode;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates that all required scheme, class and attribute IDs are in place.
 */
public class SchemeIdValidator implements Function<Scheme, Scheme> {

  @Override
  public Scheme apply(Scheme scheme) {
    checkNotNull(scheme.getId(), ErrorCode.SCHEME_ID_MISSING);
    checkArgument(!scheme.getId().equals(UUIDs.nilUuid()), ErrorCode.SCHEME_ID_ILLEGAL);

    for (Class cls : scheme.getClasses()) {
      validateClass(scheme, cls);
    }
    return scheme;
  }

  private void validateClass(Scheme scheme, Class cls) {
    UUID schemeId = cls.getSchemeId();
    String id = cls.getId();

    checkNotNull(schemeId, ErrorCode.CLASS_SCHEME_ID_MISSING);
    checkArgument(schemeId.equals(scheme.getId()), ErrorCode.CLASS_SCHEME_ID_ILLEGAL);
    checkNotNull(id, ErrorCode.CLASS_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE), ErrorCode.CLASS_ID_INVALID);

    for (TextAttribute attribute : cls.getTextAttributes()) {
      validateTextAttribute(cls, attribute);
    }
    for (ReferenceAttribute attribute : cls.getReferenceAttributes()) {
      validateReferenceAttribute(cls, attribute);
    }
  }

  private void validateTextAttribute(Class cls, TextAttribute attribute) {
    UUID schemeId = attribute.getDomainSchemeId();
    String id = attribute.getId();

    checkNotNull(schemeId, ErrorCode.TEXT_ATTRIBUTE_SCHEME_ID_MISSING);
    checkArgument(schemeId.equals(cls.getSchemeId()), ErrorCode.TEXT_ATTRIBUTE_SCHEME_ID_ILLEGAL);
    checkNotNull(id, ErrorCode.TEXT_ATTRIBUTE_ID_MISSING);
    checkNotNull(id.matches(RegularExpressions.CODE), ErrorCode.TEXT_ATTRIBUTE_ID_INVALID);
  }

  private void validateReferenceAttribute(Class cls, ReferenceAttribute attribute) {
    checkNotNull(attribute.getDomainSchemeId());
    checkArgument(attribute.getDomainSchemeId().equals(cls.getSchemeId()));
    checkNotNull(attribute.getId());

    checkNotNull(attribute.getRangeSchemeId());
    checkNotNull(attribute.getRangeId());
  }

}
