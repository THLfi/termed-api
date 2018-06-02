package fi.thl.termed.service.property;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.service.property.internal.JdbcPropertyDao;
import fi.thl.termed.service.property.internal.JdbcPropertyPropertyDao;
import fi.thl.termed.service.property.internal.PropertyRepository;
import fi.thl.termed.util.dao.AuthorizedDao2;
import fi.thl.termed.util.dao.SystemDao2;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.TransactionalService2;
import fi.thl.termed.util.service.WriteLoggingService2;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PropertyServiceConfiguration {

  @Autowired
  private EventBus eventBus;

  @Bean
  public Service2<String, Property> propertyService(
      DataSource dataSource, PlatformTransactionManager transactionManager) {

    SystemDao2<String, Property> propertyDao =
        register(eventBus, cache(new JdbcPropertyDao(dataSource)));
    SystemDao2<PropertyValueId<String>, LangValue> propertyPropertyDao =
        register(eventBus, cache(new JdbcPropertyPropertyDao(dataSource)));

    PermissionEvaluator<String> propertyEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER || p == Permission.READ;
    PermissionEvaluator<PropertyValueId<String>> propertyPropertyEvaluator =
        (u, o, p) -> propertyEvaluator.hasPermission(u, o.getSubjectId(), p);

    Service2<String, Property> service =
        new PropertyRepository(
            new AuthorizedDao2<>(propertyDao, propertyEvaluator),
            new AuthorizedDao2<>(propertyPropertyDao, propertyPropertyEvaluator));

    service = new WriteLoggingService2<>(service, getClass().getPackage().getName() + ".Service");

    return new TransactionalService2<>(service, transactionManager);
  }

}
