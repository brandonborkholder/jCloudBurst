package com.github.jcloudburst.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ImportConfig implements Cloneable {
  public static final long UNSET_VERSION = Long.MIN_VALUE;

  private long connectionVersion;
  private long tableVersion;
  private long fileSourceVersion;
  private long colMappingVersion;

  private String jdbcUrl;
  private String jdbcUsername;
  private String jdbcPassword;

  private TableRef table;
  private boolean truncateTable;
  private boolean failOnMissingColumn;

  private List<ExcelSource> excelSources;
  private List<DelimitedSource> delimitedSources;

  private List<String> columns;
  private List<ColumnSource> columnSources;

  public ImportConfig() {
    columns = new ArrayList<>();
    columnSources = new ArrayList<>();
    excelSources = new ArrayList<>();
    delimitedSources = new ArrayList<>();
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    connectionVersion++;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public void setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    connectionVersion++;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    connectionVersion++;
  }

  public TableRef getTable() {
    return table;
  }

  public void setTable(String table) {
    setTable(new TableRef(table));
  }

  public void setTable(TableRef table) {
    this.table = table;
    tableVersion++;
  }

  public boolean isTruncateTable() {
    return truncateTable;
  }

  public void setTruncateTable(boolean truncateTable) {
    this.truncateTable = truncateTable;
  }

  public boolean isFailOnMissingColumn() {
    return failOnMissingColumn;
  }

  public void setFailOnMissingColumn(boolean failOnMissingColumn) {
    this.failOnMissingColumn = failOnMissingColumn;
  }

  public void add(ExcelSource source) {
    excelSources.add(source);
    fileSourceVersion++;
  }

  public void remove(ExcelSource source) {
    excelSources.remove(source);
    fileSourceVersion++;
  }

  public void clearExcelSources() {
    excelSources.clear();
    fileSourceVersion++;
  }

  public List<ExcelSource> getExcelSources() {
    return Collections.unmodifiableList(excelSources);
  }

  public void add(DelimitedSource source) {
    delimitedSources.add(source);
    fileSourceVersion++;
  }

  public void remove(DelimitedSource source) {
    delimitedSources.remove(source);
    fileSourceVersion++;
  }

  public void clearDelimitedSources() {
    delimitedSources.clear();
    fileSourceVersion++;
  }

  public List<DelimitedSource> getDelimitedSources() {
    return Collections.unmodifiableList(delimitedSources);
  }

  public ColumnSource getColumnSource(int index) {
    return columnSources.get(index);
  }

  public ColumnSource getColumnSource(String name) {
    int idx = columns.indexOf(name);
    if (idx < 0) {
      return null;
    } else {
      return getColumnSource(idx);
    }
  }

  public int getNumColumns() {
    return columns.size();
  }

  public String getColumn(int index) {
    return columns.get(index);
  }

  public List<String> getColumns() {
    return Collections.unmodifiableList(columns);
  }

  public void setColumns(Collection<String> names) {
    columns.clear();
    columnSources.clear();

    columns.addAll(names);
    columnSources.addAll(Arrays.asList(new ColumnSource[names.size()]));

    colMappingVersion++;
  }

  public List<ColumnSource> getColumnSources() {
    return Collections.unmodifiableList(columnSources);
  }

  public void setColumnSource(String name, ColumnSource source) {
    int idx = columns.indexOf(name);
    if (idx < 0) {
      idx = columns.size();
      columns.add(name);
      columnSources.add(new ColumnSource());
    }

    setColumnSource(idx, source);
  }

  public void setColumnSource(int index, ColumnSource source) {
    columnSources.set(index, source);
    colMappingVersion++;
  }

  public void clearColumnSources() {
    for (int i = 0; i < columnSources.size(); i++) {
      columnSources.set(i, new ColumnSource());
    }

    colMappingVersion++;
  }

  public long getConnectionVersion() {
    return connectionVersion;
  }

  public long getTableVersion() {
    return tableVersion;
  }

  public long getFileSourceVersion() {
    return fileSourceVersion;
  }

  public long getColMappingVersion() {
    return colMappingVersion;
  }

  public ImportConfig clone() {
    try {
      ImportConfig c = (ImportConfig) super.clone();
      c.excelSources = new ArrayList<>(excelSources);
      c.delimitedSources = new ArrayList<>(delimitedSources);
      c.columns = new ArrayList<>(columns);
      c.columnSources = new ArrayList<>(columnSources);

      return c;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }
}
