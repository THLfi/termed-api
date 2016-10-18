package fi.thl.termed.service.property;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.service.property.internal.JdbcPropertyDao;
import fi.thl.termed.service.property.internal.JdbcPropertyPropertyValueDao;
import fi.thl.termed.service.property.internal.PropertyRepository;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.dao.WriteLoggingDao;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

import static fi.thl.termed.util.dao.AuthorizedDao.ReportLevel.THROW;

@Configuration
public class PropertyServiceConfiguration {

  @Bean
  public Service<String, Property> propertyService(
      DataSource dataSource, PlatformTransactionManager transactionManager) {

    SystemDao<String, Property> propertyDao =
        new CachedSystemDao<>(new JdbcPropertyDao(dataSource));
    SystemDao<PropertyValueId<String>, LangValue> propertyPropertyDao =
        new CachedSystemDao<>(new JdbcPropertyPropertyValueDao(dataSource));

    PermissionEvaluator<String> propertyEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER || p == Permission.READ;
    PermissionEvaluator<PropertyValueId<String>> propertyPropertyEvaluator =
        (u, o, p) -> propertyEvaluator.hasPermission(u, o.getSubjectId(), p);

    Service<String, Property> service =
        new PropertyRepository(
            new WriteLoggingDao<>(
                new AuthorizedDao<>(propertyDao, propertyEvaluator, THROW),
                getClass().getPackage().getName() + ".PropertyDao"),
            new WriteLoggingDao<>(
                new AuthorizedDao<>(propertyPropertyDao, propertyPropertyEvaluator, THROW),
                getClass().getPackage().getName() + ".PropertyPropertyDao"));

    return new TransactionalService<>(service, transactionManager);
  }

}
