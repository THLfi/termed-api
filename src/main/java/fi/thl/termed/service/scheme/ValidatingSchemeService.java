package fi.thl.termed.service.scheme;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.util.ErrorCode;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Does some basic id validations to a scheme. Useful to run after id resolving and before
 * persisting.
 */
public class ValidatingSchemeService extends ForwardingService<UUID, Scheme> {

  public ValidatingSchemeService(Service<UUID, Scheme> delegate) {
    super(delegate);
  }

  @Override
  public List<UUID> save(List<Scheme> schemes, User currentUser) {
    for (Scheme scheme : schemes) {
      validateScheme(scheme);
    }
    return super.save(schemes, currentUser);
  }

  @Override
  public UUID save(Scheme scheme, User currentUser) {
    validateScheme(scheme);
    return super.save(scheme, currentUser);
  }

  private void validateScheme(Scheme scheme) {
    validateSchemeId(scheme.getId());
    validateClasses(scheme, scheme.getClasses());
  }

  private void validateSchemeId(UUID id) {
    checkNotNull(id, ErrorCode.SCHEME_ID_MISSING);
    checkArgument(!id.equals(UUIDs.nilUuid()), ErrorCode.SCHEME_ID_ILLEGAL);
  }

  private void validateClasses(Scheme scheme, List<Class> classes) {
    for (Class cls : classes) {
      validateClass(scheme, cls);
    }
  }

  private void validateClass(Scheme scheme, Class cls) {
    validateClassSchemeId(scheme.getId(), cls.getSchemeId());
    validateClassId(cls.getId());
    validateTextAttributes(cls, cls.getTextAttributes());
    validateReferenceAttributes(cls, cls.getReferenceAttributes());
  }

  private void validateClassSchemeId(UUID expectedSchemeId, UUID schemeId) {
    checkNotNull(schemeId, ErrorCode.CLASS_SCHEME_ID_MISSING);
    checkArgument(!schemeId.equals(UUIDs.nilUuid()), ErrorCode.CLASS_SCHEME_ID_ILLEGAL);
    checkArgument(schemeId.equals(expectedSchemeId), ErrorCode.CLASS_SCHEME_ID_INVALID);
  }

  private void validateClassId(String id) {
    checkNotNull(id, ErrorCode.CLASS_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE), ErrorCode.CLASS_ID_INVALID);
  }

  private void validateTextAttributes(Class cls, List<TextAttribute> attributes) {
    for (TextAttribute attribute : cls.getTextAttributes()) {
      validateTextAttribute(cls, attribute);
    }
  }

  private void validateTextAttribute(Class cls, TextAttribute attribute) {
    validateTextAttributeDomainSchemeId(cls.getSchemeId(), attribute.getDomainSchemeId());
    validateTextAttributeDomainId(cls.getId(), attribute.getDomainId());
    validateTextAttributeId(attribute.getId());
  }

  private void validateTextAttributeDomainSchemeId(UUID expectedSchemeId, UUID schemeId) {
    checkNotNull(schemeId, ErrorCode.TEXT_ATTRIBUTE_DOMAIN_SCHEME_ID_MISSING);
    checkArgument(!schemeId.equals(UUIDs.nilUuid()),
                  ErrorCode.TEXT_ATTRIBUTE_DOMAIN_SCHEME_ID_ILLEGAL);
    checkArgument(schemeId.equals(expectedSchemeId),
                  ErrorCode.TEXT_ATTRIBUTE_DOMAIN_SCHEME_ID_INVALID);
  }

  private void validateTextAttributeDomainId(String expectedId, String id) {
    checkNotNull(id, ErrorCode.TEXT_ATTRIBUTE_DOMAIN_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE), ErrorCode.TEXT_ATTRIBUTE_DOMAIN_ID_ILLEGAL);
    checkArgument(id.equals(expectedId), ErrorCode.TEXT_ATTRIBUTE_DOMAIN_ID_INVALID);
  }

  private void validateTextAttributeId(String id) {
    checkNotNull(id, ErrorCode.TEXT_ATTRIBUTE_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE), ErrorCode.TEXT_ATTRIBUTE_ID_ILLEGAL);
  }

  private void validateReferenceAttributes(Class cls, List<ReferenceAttribute> attributes) {
    for (ReferenceAttribute attribute : attributes) {
      validateReferenceAttribute(cls, attribute);
    }
  }

  private void validateReferenceAttribute(Class cls, ReferenceAttribute attribute) {
    validateReferenceAttributeDomainSchemeId(cls.getSchemeId(), attribute.getDomainSchemeId());
    validateReferenceAttributeDomainId(cls.getId(), attribute.getDomainId());
    validateReferenceAttributeRangeSchemeId(attribute.getRangeSchemeId());
    validateReferenceAttributeRangeId(attribute.getRangeId());
    validateReferenceAttributeId(attribute.getId());
  }

  private void validateReferenceAttributeDomainSchemeId(UUID expectedSchemeId, UUID schemeId) {
    checkNotNull(schemeId, ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_SCHEME_ID_MISSING);
    checkArgument(!schemeId.equals(UUIDs.nilUuid()),
                  ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_SCHEME_ID_ILLEGAL);
    checkArgument(schemeId.equals(expectedSchemeId),
                  ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_SCHEME_ID_INVALID);
  }

  private void validateReferenceAttributeDomainId(String expectedId, String id) {
    checkNotNull(id, ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE),
                  ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_ID_ILLEGAL);
    checkArgument(id.equals(expectedId), ErrorCode.REFERENCE_ATTRIBUTE_DOMAIN_ID_INVALID);
  }

  private void validateReferenceAttributeRangeSchemeId(UUID schemeId) {
    checkNotNull(schemeId, ErrorCode.REFERENCE_ATTRIBUTE_RANGE_SCHEME_ID_MISSING);
    checkArgument(!schemeId.equals(UUIDs.nilUuid()),
                  ErrorCode.REFERENCE_ATTRIBUTE_RANGE_SCHEME_ID_ILLEGAL);
  }

  private void validateReferenceAttributeRangeId(String id) {
    checkNotNull(id, ErrorCode.REFERENCE_ATTRIBUTE_RANGE_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE),
                  ErrorCode.REFERENCE_ATTRIBUTE_RANGE_ID_ILLEGAL);
  }

  private void validateReferenceAttributeId(String id) {
    checkNotNull(id, ErrorCode.REFERENCE_ATTRIBUTE_ID_MISSING);
    checkArgument(id.matches(RegularExpressions.CODE), ErrorCode.REFERENCE_ATTRIBUTE_ID_ILLEGAL);
  }

}
