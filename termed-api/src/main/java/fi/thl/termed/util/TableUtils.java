package fi.thl.termed.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

public final class TableUtils {

  private TableUtils() {
  }

  public static List<Map<String, String>> toMapped(List<String[]> rows) {
    Iterator<String[]> rowIterator = rows.iterator();
    List<Map<String, String>> mapped = Lists.newArrayList();

    if (!rowIterator.hasNext()) {
      return mapped;
    }

    String[] headers = rowIterator.next();

    while (rowIterator.hasNext()) {
      String[] row = rowIterator.next();
      Map<String, String> next = Maps.newLinkedHashMap();
      for (int i = 0; i < min(headers.length, row.length); i++) {
        next.put(headers[i], row[i]);
      }
      mapped.add(next);
    }

    return mapped;
  }

  public static List<String[]> toTable(List<Map<String, String>> mappedLines) {
    List<String> colNames = Lists.newArrayList();
    for (Map<String, String> mappedLine : mappedLines) {
      colNames.addAll(mappedLine.keySet());
    }
    return toTable(colNames, mappedLines);
  }

  public static List<String[]> toTable(List<String> colNames,
                                       List<Map<String, String>> mappedLines) {
    Map<String, String> colKeyLabels = Maps.newLinkedHashMap();
    for (String colName : colNames) {
      colKeyLabels.put(colName, colName);
    }
    return toTable(colKeyLabels, mappedLines);
  }

  public static List<String[]> toTable(Map<String, String> colKeyLabels,
                                       List<Map<String, String>> mappedLines) {
    String[] colKeys = toArray(colKeyLabels.keySet());
    String[] colLabels = getValues(colKeys, colKeyLabels);

    List<String[]> rows = Lists.newArrayList();

    rows.add(colLabels);

    for (Map<String, String> line : mappedLines) {
      rows.add(getValues(colKeys, line));
    }

    return rows;
  }

  private static String[] getValues(String[] keys, Map<String, String> map) {
    List<String> values = Lists.newArrayList();
    for (String key : keys) {
      values.add(map.get(key));
    }
    return toArray(values);
  }

  private static String[] toArray(Collection<String> collection) {
    return collection.toArray(new String[collection.size()]);
  }

}
