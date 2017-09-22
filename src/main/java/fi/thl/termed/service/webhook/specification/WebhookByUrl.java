package fi.thl.termed.service.webhook.specification;

import fi.thl.termed.domain.Webhook;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class WebhookByUrl extends AbstractSqlSpecification<UUID, Webhook> {

  private URI url;

  public WebhookByUrl(URI url) {
    this.url = url;
  }

  @Override
  public boolean test(UUID key, Webhook value) {
    return Objects.equals(value.getUrl(), url);
  }

  @Override
  public String sqlQueryTemplate() {
    return "url = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{url.toString()};
  }

}