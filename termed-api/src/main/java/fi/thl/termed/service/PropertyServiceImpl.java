package fi.thl.termed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.PropertyRepository;
import fi.thl.termed.repository.spesification.Specification;

@Service
public class PropertyServiceImpl implements PropertyService {

  @Autowired
  private PropertyRepository propertyRepository;

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public int save(List<Property> properties, User currentUser) {
    propertyRepository.save(properties);
    return properties.size();
  }

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public Property save(String id, Property property, User currentUser) {
    property.setId(id);
    return save(property, currentUser);
  }

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public Property save(Property property, User currentUser) {
    propertyRepository.save(property);
    return propertyRepository.get(property.getId());
  }

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public void delete(String id, User currentUser) {
    propertyRepository.delete(id);
  }

  @Override
  public List<Property> get(User currentUser) {
    return propertyRepository.get();
  }

  @Override
  public List<Property> get(Specification<String, Property> specification, User currentUser) {
    return propertyRepository.get(specification);
  }

  @Override
  public List<Property> get(Query query, User currentUser) {
    return get(currentUser);
  }

  @Override
  public Property get(String id, User currentUser) {
    return propertyRepository.get(id);
  }

}
