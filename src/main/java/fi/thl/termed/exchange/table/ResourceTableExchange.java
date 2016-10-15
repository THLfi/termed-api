package fi.thl.termed.exchange.table;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.AbstractExchange;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.json.JsonUtils;
import fi.thl.termed.util.TableUtils;

/**
 * Exports Resources to tabular format by first transforming into JSON then flattening and finally
 * putting into table.
 */
public class ResourceTableExchange extends AbstractExchange<ResourceId, Resource, List<String[]>> {

  private Gson gson;

  public ResourceTableExchange(Service<ResourceId, Resource> service, Gson gson) {
    super(service);
    this.gson = gson;
  }

  @Override
  protected Map<String, Class> requiredArgs() {
    return ImmutableMap.<String, Class>of("schemeId", UUID.class);
  }

  @Override
  protected List<String[]> doExport(List<Resource> values, Map<String, Object> args,
                                    User currentUser) {
    List<Map<String, String>> rows = Lists.newArrayList();
    for (Resource resource : values) {
      rows.add(JsonUtils.flatten(gson.toJsonTree(resource, Resource.class)));
    }
    return TableUtils.toTable(rows);
  }

  @Override
  protected List<Resource> doImport(List<String[]> rows, Map<String, Object> args,
                                    User currentUser) {
    List<Resource> resources = Lists.newArrayList();
    for (Map<String, String> row : TableUtils.toMapped(rows)) {
      resources.add(gson.fromJson(JsonUtils.unflatten(row), Resource.class));
    }
    return resources;
  }

}
