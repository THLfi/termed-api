package fi.thl.termed.service.impl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

@Transactional
@PreAuthorize("hasRole('SUPERUSER')")
public class UserServiceImpl implements Service<String, User> {

  private Repository<String, User> userRepository;

  public UserServiceImpl(Repository<String, User> userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void save(List<User> properties, User currentUser) {
    userRepository.save(properties);
  }

  @Override
  public void save(User property, User currentUser) {
    userRepository.save(property);
  }

  @Override
  public void delete(String id, User currentUser) {
    userRepository.delete(id);
  }

  @Override
  public List<User> get(User currentUser) {
    return userRepository.get();
  }

  @Override
  public List<User> get(Specification<String, User> specification, User currentUser) {
    return userRepository.get(specification);
  }

  @Override
  public List<User> get(Query query, User currentUser) {
    return get(currentUser);
  }

  @Override
  public User get(String id, User currentUser) {
    return userRepository.get(id);
  }

}
