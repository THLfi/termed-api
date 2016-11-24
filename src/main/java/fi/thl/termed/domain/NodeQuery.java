package fi.thl.termed.domain;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.util.json.MultimapTypeAdapterFactory;

public class NodeQuery {

  public Select select = new Select();
  public Where where = new Where();
  public Recurse recurse = new Recurse();
  public List<String> sort = new ArrayList<>();
  public Integer max = 50;

  // for toString
  private transient Gson gson = new GsonBuilder().setPrettyPrinting()
      .registerTypeAdapterFactory(new MultimapTypeAdapterFactory()).create();

  @Override
  public String toString() {
    return gson.toJson(this);
  }

  public static class Select {

    // by default the following are not selected
    public boolean type;
    public boolean graph;
    public boolean audit;

    // empty list means "select all"
    public List<String> properties = new ArrayList<>();
    public List<String> references = new ArrayList<>();
    public List<String> referrers = new ArrayList<>();

  }

  public static class Where {

    public Multimap<String, String> properties = LinkedHashMultimap.create();
    public Multimap<String, UUID> references = LinkedHashMultimap.create();

  }

  public static class Recurse {

    public Map<String, Integer> references = new LinkedHashMap<>();
    public Map<String, Integer> referrers = new LinkedHashMap<>();

  }

}