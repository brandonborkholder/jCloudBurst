package com.github.jcloudburst;

public class ImportContext {
  private String file;
  private String worksheet;
  private int sourceRowCount;
  private int totalRowCount;

  public String getFile() {
    return file;
  }

  public String getWorksheet() {
    return worksheet;
  }

  public int getSourceRowCount() {
    return sourceRowCount;
  }

  public int getTotalRowCount() {
    return totalRowCount;
  }

  public void newExcelSource(String fileName, String worksheet) {
    file = fileName;
    this.worksheet = worksheet;
    sourceRowCount = 0;
  }

  public void newDelimitedSource(String fileName) {
    file = fileName;
    worksheet = null;
    sourceRowCount = 0;
  }

  public void incrementRows() {
    sourceRowCount++;
    totalRowCount++;
  }
}
