package fi.thl.termed.repository;

import org.springframework.security.core.userdetails.UserDetailsService;

import fi.thl.termed.domain.User;

public abstract class UserRepository extends AbstractRepository<String, User>
    implements UserDetailsService {

}
