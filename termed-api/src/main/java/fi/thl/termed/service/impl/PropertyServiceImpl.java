package fi.thl.termed.service.impl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

@Transactional
public class PropertyServiceImpl implements Service<String, Property> {

  private Repository<String, Property> propertyRepository;

  public PropertyServiceImpl(Repository<String, Property> propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public void save(List<Property> properties, User currentUser) {
    propertyRepository.save(properties);
  }

  @Override
  @PreAuthorize("hasRole('SUPERUSER')")
  public void save(Property property, User currentUser) {
    propertyRepository.save(property);
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
