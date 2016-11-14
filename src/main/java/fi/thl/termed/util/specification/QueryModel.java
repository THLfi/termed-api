package fi.thl.termed.util.specification;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

import fi.thl.termed.util.collect.ListUtils;

/**
 * To be used with e.g. Spring ModelAttribute
 */
public class QueryModel {

  private String query = "";
  private List<String> orderBy = new ArrayList<>();
  private int max = 50;
  private boolean bypassIndex = false;

  public String getQuery() {
    return Strings.nullToEmpty(query);
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public List<String> getOrderBy() {
    return ListUtils.nullToEmpty(orderBy);
  }

  public void setOrderBy(List<String> orderBy) {
    this.orderBy = orderBy;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public boolean isBypassIndex() {
    return bypassIndex;
  }

  public void setBypassIndex(boolean bypassIndex) {
    this.bypassIndex = bypassIndex;
  }

}
