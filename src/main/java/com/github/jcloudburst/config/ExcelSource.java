package com.github.jcloudburst.config;

import java.io.File;

public class ExcelSource implements Cloneable {
  public final File file;
  public final String excelSheet;
  public final boolean hasHeaderRow;

  public ExcelSource() {
    this(null, null, false);
  }

  protected ExcelSource(File file, String excelSheet, boolean hasHeaderRow) {
    this.file = file;
    this.excelSheet = excelSheet;
    this.hasHeaderRow = hasHeaderRow;
  }

  public ExcelSource withFile(File file) {
    return new ExcelSource(file, excelSheet, hasHeaderRow);
  }

  public ExcelSource withExcelSheet(String excelSheet) {
    return new ExcelSource(file, excelSheet, hasHeaderRow);
  }

  public ExcelSource withHasHeaderRow(boolean hasHeaderRow) {
    return new ExcelSource(file, excelSheet, hasHeaderRow);
  }

  public boolean isValid() {
    return file != null && file.exists() && excelSheet != null;
  }

  @Override
  public String toString() {
    return String.format("Excel[file=%s,sheet=%s,hasHeader=%b]", file, excelSheet, hasHeaderRow);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((excelSheet == null) ? 0 : excelSheet.hashCode());
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    result = prime * result + (hasHeaderRow ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExcelSource)) {
      return false;
    }
    ExcelSource other = (ExcelSource) obj;
    if (excelSheet == null) {
      if (other.excelSheet != null) {
        return false;
      }
    } else if (!excelSheet.equals(other.excelSheet)) {
      return false;
    }
    if (file == null) {
      if (other.file != null) {
        return false;
      }
    } else if (!file.equals(other.file)) {
      return false;
    }
    if (hasHeaderRow != other.hasHeaderRow) {
      return false;
    }
    return true;
  }

  @Override
  public ExcelSource clone() {
    try {
      return (ExcelSource) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("unexpected clone not supported", e);
    }
  }
}
