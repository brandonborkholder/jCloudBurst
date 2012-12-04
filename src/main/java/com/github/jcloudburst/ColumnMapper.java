package com.github.jcloudburst;

import java.util.Map;
import java.util.TreeMap;

import com.github.jcloudburst.config.ImportConfig;

public class ColumnMapper {
  private Map<Integer, Integer> columnToFileIndex = new TreeMap<Integer, Integer>();

  private ImportConfig c;

  public ColumnMapper(ImportConfig c) {
    this.c = c;

    for (int i = 0; i < c.getNumColumns(); i++) {
      int field = c.getColumnSource(i).fileFieldIndex;
      columnToFileIndex.put(i, field < 0 ? null : field);
    }
  }

  public String getDbColumnName(int colId) {
    return c.getColumn(colId);
  }

  public String getFileColumnName(int colId) {
    return c.getColumnSource(colId).fileFieldName;
  }

  public boolean isColumnDefined(int colId) {
    return c.getColumnSource(colId).isValid();
  }

  public String getFixedColumnValue(int colId) {
    return c.getColumnSource(colId).fixedValue;
  }

  public void mapColToField(int colId, int fileFieldIndex) {
    columnToFileIndex.put(colId, fileFieldIndex);
  }

  public int mapColToField(int colId) {
    Integer index = columnToFileIndex.get(colId);
    return index == null ? -1 : index;
  }

  public int numColumns() {
    return c.getNumColumns();
  }
}
