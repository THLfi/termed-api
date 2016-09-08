package fi.thl.termed.service.resource;

import com.google.common.collect.Multimap;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.sql.ReferenceAttributesByClassId;
import fi.thl.termed.spesification.resource.ResourceByCode;
import fi.thl.termed.spesification.resource.ResourceByUri;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.util.StrictLangValue;
import fi.thl.termed.util.UUIDs;

/**
 * Make sure that 1) each text attribute value (property) has a correct regex 2) each reference
 * attribute value (reference) has a correct range and id
 */
public class AttributeResolvingResourceService extends ForwardingService<ResourceId, Resource> {

  private Dao<TextAttributeId, TextAttribute> textAttributeDao;
  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;
  private Dao<ResourceId, Resource> resourceDao;

  public AttributeResolvingResourceService(
      Service<ResourceId, Resource> delegate,
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ResourceId, Resource> resourceDao) {
    super(delegate);
    this.textAttributeDao = textAttributeDao;
    this.referenceAttributeDao = referenceAttributeDao;
    this.resourceDao = resourceDao;
  }

  @Override
  public void save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resolveAttributes(resource);
    }
    super.save(resources, currentUser);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    resolveAttributes(resource);
    super.save(resource, currentUser);
  }

  private void resolveAttributes(Resource resource) {
    ClassId typeId = new ClassId(resource);

    resolveTextAttributes(
        textAttributeDao.getValues(new TextAttributesByClassId(typeId)),
        resource.getProperties());

    resolveReferenceAttributes(
        referenceAttributeDao.getValues(new ReferenceAttributesByClassId(typeId)),
        resource.getReferences());
  }

  private void resolveTextAttributes(List<TextAttribute> textAttributes,
                                     Multimap<String, StrictLangValue> properties) {
    for (TextAttribute textAttribute : textAttributes) {
      for (StrictLangValue value : properties.get(textAttribute.getId())) {
        value.setRegex(textAttribute.getRegex());
      }
    }
  }

  private void resolveReferenceAttributes(List<ReferenceAttribute> referenceAttributes,
                                          Multimap<String, Resource> references) {
    for (ReferenceAttribute referenceAttribute : referenceAttributes) {
      for (Resource value : references.values()) {
        value.setScheme(new Scheme(referenceAttribute.getRangeSchemeId()));
        value.setType(new Class(new ClassId(referenceAttribute.getRangeSchemeId(),
                                            referenceAttribute.getRangeId())));
        resolveId(value);
      }
    }
  }

  private void resolveId(Resource resource) {
    UUID schemeId = resource.getSchemeId();
    String typeId = resource.getTypeId();
    UUID id = resource.getId();

    String code = resource.getCode();
    String uri = resource.getUri();

    if (id == null && code != null) {
      id = resolveIdForCode(schemeId, typeId, code);
    }
    if (id == null && uri != null) {
      id = resolveIdForUri(schemeId, uri);
    }
    if (id == null && code != null) {
      id = UUIDs.nameUUIDFromString(schemeId + "-" + typeId + "-" + code);
    }
    if (id == null && uri != null) {
      id = UUIDs.nameUUIDFromString(schemeId + "-" + uri);
    }
    if (id == null) {
      throw new NullPointerException("Can't resolve id for resource");
    }

    resource.setId(id);
  }

  private UUID resolveIdForCode(UUID schemeId, String typeId, String code) {
    if (code != null) {
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByCode(schemeId, typeId, code));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

  private UUID resolveIdForUri(UUID schemeId, String uri) {
    if (uri != null) {
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByUri(schemeId, uri));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

}
