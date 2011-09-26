package com.github.jcloudburst;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ColumnMapper {
  private Map<Integer, Integer> columnToFileIndex = new TreeMap<Integer, Integer>();

  private List<String> fileColumnNames = new ArrayList<String>();

  private List<String> dbColumnNames = new ArrayList<String>();

  private List<String> fixedColumnValues = new ArrayList<String>();

  private List<DateFormat> dateFormats = new ArrayList<DateFormat>();

  private List<String> variables = new ArrayList<String>();

  public ColumnMapper(List<ColumnMapType> columns) {
    for (int i = 0; i < columns.size(); i++) {
      ColumnMapType column = columns.get(i);

      fileColumnNames.add(column.getFileColName());
      dbColumnNames.add(column.getDbColumn());
      fixedColumnValues.add(column.getFixedValue());

      if (column.getFormat() == null || column.getFormat().isEmpty()) {
        dateFormats.add(null);
      } else {
        dateFormats.add(new SimpleDateFormat(column.getFormat()));
      }

      if (column.getVariable() == null || column.getVariable().isEmpty()) {
        variables.add(null);
      } else {
        variables.add(column.getVariable());
      }

      if (column.getFileColIndex() != null) {
        columnToFileIndex.put(i, column.getFileColIndex());
      }
    }
  }

  public String getDbColumnName(int colId) {
    return dbColumnNames.get(colId);
  }

  public String getFileColumnName(int colId) {
    return fileColumnNames.get(colId);
  }

  public boolean isColumnDefined(int colId) {
    return columnToFileIndex.containsKey(colId) || fixedColumnValues.get(colId) != null || variables.get(colId) != null;
  }

  public String getFixedColumnValue(int colId) {
    return fixedColumnValues.get(colId);
  }

  public void setFileColumnIndex(int colId, int fileColumnIndex) {
    columnToFileIndex.put(colId, fileColumnIndex);
  }

  public int getFileColumnIndex(int colId) {
    Integer index = columnToFileIndex.get(colId);
    return index == null ? -1 : index;
  }

  public int numColumns() {
    return dbColumnNames.size();
  }
}
