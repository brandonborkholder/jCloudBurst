package com.github.jcloudburst;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.jcloudburst.config.ColumnSource;
import com.github.jcloudburst.config.ImportConfig;

public class RowHandler {
  public static final String VAR_FILE = "$file";
  public static final String VAR_LINE = "$line";

  public static final List<String> VARIABLES = Collections.unmodifiableList(Arrays.asList(VAR_FILE, VAR_LINE));

  protected PreparedStatement stmt;

  protected SQLType[] types;

  protected int[] sqlTypes;

  protected ImportConfig config;

  protected ImportContext context;

  protected enum SQLType {
    String,

    Date,

    Numeric,
  }

  public RowHandler(ImportConfig config, ImportContext context, Connection connection) throws SQLException {
    this.context = context;
    this.config = config;

    int numColumns = config.getColumns().size();
    types = new SQLType[numColumns];
    sqlTypes = new int[numColumns];

    String query = buildPreparedStatement();
    stmt = connection.prepareStatement(query);

    Statement typesStatement = connection.createStatement();
    typesStatement.setMaxRows(1);
    query = "SELECT " + getDbColumnsList() + " FROM " + config.getTable();
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

  protected String getDbColumnsList() {
    StringBuilder builder = new StringBuilder();
    boolean first = true;

    for (String column : config.getColumns()) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }

      // XXX how to wrap the columns?
      builder.append(column);
    }

    return builder.toString();
  }

  protected String buildPreparedStatement() {
    StringBuilder builder = new StringBuilder();
    builder.append("INSERT INTO ");
    builder.append(config.getTable());
    builder.append(" ( ");
    builder.append(getDbColumnsList());
    builder.append(" ) VALUES ( ");

    int numColumns = config.getColumns().size();
    for (int i = 0; i < numColumns; i++) {
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
        DateFormat format = new SimpleDateFormat(config.getColumnSources().get(dbColumnIndex).dateFormatString);
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

  protected void fillFixedValues() throws SQLException {
    for (int col = 0; col < config.getNumColumns(); col++) {
      ColumnSource src = config.getColumnSource(col);

      if (src.fixedValue == null) {
        continue;
      }

      switch (src.fixedValue) {
      case VAR_FILE:
        setDbValue(col, context.getFile().getAbsolutePath());
        break;

      case VAR_LINE:
        setDbValue(col, String.valueOf(context.getSourceRowCount()));
        break;

      default:
        setDbValue(col, src.fixedValue);
        break;
      }
    }
  }

  public void nextRow() throws SQLException {
    fillFixedValues();
    stmt.addBatch();
  }

  public void commitBatch() throws SQLException {
    Connection connection = stmt.getConnection();
    connection.setAutoCommit(false);
    stmt.executeBatch();
    connection.commit();
    connection.setAutoCommit(true);
  }

  public void close() throws SQLException {
    stmt.close();
  }
}
