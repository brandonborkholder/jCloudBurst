package com.github.jcloudburst;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RowHandler {
  protected PreparedStatement stmt;

  protected SQLType[] types;

  protected int[] sqlTypes;

  protected List<ColumnMapType> maps;

  protected File file;

  protected enum SQLType {
    String,

    Date,

    Numeric,
  }

  public RowHandler(ConfigurationType config, File file, Connection connection) throws SQLException {
    this.file = file;
    maps = config.getMapping().getColumn();
    types = new SQLType[maps.size()];
    sqlTypes = new int[maps.size()];

    String query = buildPreparedStatement(config);
    stmt = connection.prepareStatement(query);

    Statement typesStatement = connection.createStatement();
    typesStatement.setMaxRows(1);
    query = "SELECT " + getDbColumnsList(config) + " FROM " + config.getTable();
    ResultSet set = typesStatement.executeQuery(query);

    ResultSetMetaData metaData = set.getMetaData();
    int colCount = metaData.getColumnCount();
    for (int col = 0; col < colCount; col++) {
      int colType = metaData.getColumnType(col + 1);
      sqlTypes[col] = colType;
      types[col] = mapSQLTypes(colType);
    }

    typesStatement.close();
  }

  protected SQLType getSQLType(int columnId) {
    return types[columnId];
  }

  protected SQLType mapSQLTypes(int columnType) {
    switch (columnType) {
    case Types.DATE:
    case Types.TIME:
    case Types.TIMESTAMP:
      return SQLType.Date;

    case Types.BIGINT:
    case Types.BIT:
    case Types.DECIMAL:
    case Types.DOUBLE:
    case Types.FLOAT:
    case Types.INTEGER:
    case Types.NUMERIC:
    case Types.REAL:
    case Types.SMALLINT:
    case Types.TINYINT:
      return SQLType.Numeric;

    case Types.CHAR:
    case Types.CLOB:
    case Types.LONGNVARCHAR:
    case Types.LONGVARCHAR:
    case Types.NCHAR:
    case Types.NCLOB:
    case Types.NVARCHAR:
    case Types.VARCHAR:
      return SQLType.String;

    default:
      return null;
    }
  }

  protected String getDbColumnsList(ConfigurationType config) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;

    for (ColumnMapType colMap : config.getMapping().getColumn()) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      builder.append(colMap.getDbColumn());
    }

    return builder.toString();
  }

  protected String buildPreparedStatement(ConfigurationType config) {
    StringBuilder builder = new StringBuilder();
    builder.append("INSERT INTO ");
    builder.append(config.getTable());
    builder.append(" ( ");
    builder.append(getDbColumnsList(config));
    builder.append(" ) VALUES ( ");

    for (int i = 0; i < maps.size(); i++) {
      if (i == 0) {
        builder.append("?");
      } else {
        builder.append(", ?");
      }
    }

    builder.append(" )");
    return builder.toString();
  }

  public void setValue(int columnId, String value) throws SQLException {
    setDbValue(columnId, value);
  }

  public void setValue(int columnId, Date value) throws SQLException {
    setDbValue(columnId, value);
  }

  protected void setDbValue(int dbColumnIndex, String value) throws SQLException {
    if (value == null || value.isEmpty()) {
      stmt.setNull(dbColumnIndex + 1, sqlTypes[dbColumnIndex]);
      return;
    }

    try {
      value = value.trim();
      SQLType type = types[dbColumnIndex];
      if (type == SQLType.Date) {
        DateFormat format = new SimpleDateFormat(maps.get(dbColumnIndex).getFormat());
        Date date = format.parse(value);
        stmt.setDate(dbColumnIndex + 1, new java.sql.Date(date.getTime()));
      } else if (type == SQLType.Numeric) {
        stmt.setDouble(dbColumnIndex + 1, Double.parseDouble(value));
      } else {
        stmt.setString(dbColumnIndex + 1, value);
      }
    } catch (ParseException e) {
      // swallow for now
      setDbValue(dbColumnIndex, (String) null);
    }
  }

  protected void setDbValue(int dbColumnIndex, Date value) throws SQLException {
    if (value == null) {
      stmt.setNull(dbColumnIndex + 1, sqlTypes[dbColumnIndex]);
      return;
    }

    SQLType type = types[dbColumnIndex];
    if (type == SQLType.Date) {
      stmt.setDate(dbColumnIndex + 1, new java.sql.Date(value.getTime()));
    } else {
      throw new IllegalArgumentException("Cannot accept date value");
    }
  }

  protected void fillVariableValues() throws SQLException {
    int colId = 0;
    for (ColumnMapType map : maps) {
      if (map.getVariable() != null) {
        if (map.getVariable().equalsIgnoreCase("$file")) {
          setDbValue(colId, file.getName());
        }
      }
      colId++;
    }
  }

  protected void fillFixedValues() throws SQLException {
    int colId = 0;
    for (ColumnMapType map : maps) {
      if (map.getFixedValue() != null) {
        setDbValue(colId, map.getFixedValue());
      }
      colId++;
    }
  }

  public void nextRow() throws SQLException {
    fillVariableValues();
    fillFixedValues();
    stmt.execute();
  }

  public void close() throws SQLException {
    stmt.close();
  }
}
