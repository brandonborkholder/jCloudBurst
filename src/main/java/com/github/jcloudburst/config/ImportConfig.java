package com.github.jcloudburst.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ImportConfig {
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

  private Map<String, ColumnSource> columns;

  public ImportConfig() {
    columns = new TreeMap<>();
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

  public ColumnSource getColumnSource(String name) {
    return columns.get(name);
  }

  public List<String> getColumns() {
    return Collections.unmodifiableList(new ArrayList<>(columns.keySet()));
  }

  public void setColumnSource(String name, ColumnSource source) {
    columns.put(name, source);
    colMappingVersion++;
  }

  public void addColumn(String name) {
    columns.put(name, new ColumnSource());
    colMappingVersion++;
  }

  public void removeColumn(String name) {
    columns.remove(name);
    colMappingVersion++;
  }

  public void setColumns(Collection<String> names) {
    columns.clear();
    for (String name : names) {
      addColumn(name);
    }
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
}
