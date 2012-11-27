package com.github.jcloudburst.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ColumnSource {
  public final String dateFormatString;
  public final String fileFieldName;
  public final int fileFieldIndex;
  public final String fixedValue;

  public ColumnSource() {
    this(null, null, -1, null);
  }

  protected ColumnSource(String format, String fileColName, int fileColIndex, String fixedValue) {
    this.dateFormatString = format;
    this.fileFieldName = fileColName;
    this.fileFieldIndex = fileColIndex;
    this.fixedValue = fixedValue;
  }

  public DateFormat getDateFormat() {
    return new SimpleDateFormat(dateFormatString);
  }

  public ColumnSource withDateFormat(String format) {
    return new ColumnSource(format, fileFieldName, fileFieldIndex, format);
  }

  public ColumnSource withFileFieldName(String fileColName) {
    return new ColumnSource(dateFormatString, fileColName, fileFieldIndex, dateFormatString);
  }

  public ColumnSource withFileFieldIndex(int fileColIndex) {
    return new ColumnSource(dateFormatString, fileFieldName, fileColIndex, dateFormatString);
  }

  public ColumnSource withFixedValue(String fixedValue) {
    return new ColumnSource(dateFormatString, fileFieldName, fileFieldIndex, dateFormatString);
  }
}
