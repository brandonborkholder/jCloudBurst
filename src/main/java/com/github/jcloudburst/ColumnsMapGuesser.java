package com.github.jcloudburst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ColumnsMapGuesser {
  private List<String> fieldNames;
  private List<String> columnNames;

  public void addSourceFields(List<String> fieldNames) {
    this.fieldNames = new ArrayList<String>(fieldNames);
  }

  public void addDbColumns(List<String> columnNames) {
    this.columnNames = new ArrayList<String>(columnNames);
  }

  private void clean(List<String> list) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, clean(list.get(i)));
    }
  }

  private String clean(String string) {
    return string.toLowerCase().replaceAll("[^a-z0-9]", "");
  }

  public Map<Integer, Integer> guessMapping() {
    clean(fieldNames);
    clean(columnNames);

    Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
    for (int c = 0; c < columnNames.size(); c++) {
      for (int f = 0; f < fieldNames.size(); f++) {
        if (columnNames.get(c).equals(fieldNames.get(f))) {
          map.put(c, f);
        }
      }
    }

    return map;
  }
}
