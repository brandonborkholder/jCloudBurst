package anon;

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
import java.util.ListIterator;

import org.example.mappingconfig.ColumnMapType;
import org.example.mappingconfig.ConfigurationType;

public class RowHandler {
  protected PreparedStatement stmt;

  protected SQLType[] types;
  protected int[] sqlTypes;

  protected List<ColumnMapType> maps;

  protected enum SQLType {
    String,

    Date,

    Numeric,
  }

  public RowHandler(ConfigurationType config, Connection connection) throws SQLException {
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

  public boolean doSkipFileColumn(String fileColumnName) {
    return lookupFileColumnIndex(fileColumnName) == -1;
  }

  protected SQLType getSQLType(String fileColumn) {
    return types[lookupFileColumnIndex(fileColumn)];
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

  public void setValue(String fileColumnName, String value) throws SQLException, ParseException {
    setDbValue(lookupFileColumnIndex(fileColumnName), value);
  }

  public void setValue(String fileColumnName, Date value) throws SQLException, ParseException {
    setDbValue(lookupFileColumnIndex(fileColumnName), value);
  }

  protected int lookupFileColumnIndex(String name) {
    ListIterator<ColumnMapType> mapItr = maps.listIterator();
    while (mapItr.hasNext()) {
      ColumnMapType map = mapItr.next();
      if (name.equalsIgnoreCase(map.getFileColName())) {
        return mapItr.previousIndex();
      }
    }

    return -1;
  }

  protected int lookupDbColumnIndex(String name) {
    ListIterator<ColumnMapType> mapItr = maps.listIterator();
    while (mapItr.hasNext()) {
      ColumnMapType map = mapItr.next();
      if (name.equalsIgnoreCase(map.getDbColumn())) {
        return mapItr.previousIndex();
      }
    }

    return -1;
  }

  public void setValue(int fileColumnIndex, String value) throws SQLException, ParseException {
    ListIterator<ColumnMapType> mapItr = maps.listIterator();
    while (mapItr.hasNext()) {
      ColumnMapType map = mapItr.next();
      if (fileColumnIndex == map.getFileColIndex()) {
        setDbValue(mapItr.previousIndex(), value);
      }
    }

    throw new IllegalArgumentException("No such file column index: " + fileColumnIndex);
  }

  protected void setDbValue(int dbColumnIndex, String value) throws SQLException, ParseException {
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

  protected void fillFixedValues() throws SQLException, ParseException {
    for (ColumnMapType map : maps) {
      if (map.getFixedValue() != null) {
        setDbValue(lookupDbColumnIndex(map.getDbColumn()), map.getFixedValue());
      }
    }
  }

  public void nextRow() throws SQLException {
    stmt.execute();
  }
}
