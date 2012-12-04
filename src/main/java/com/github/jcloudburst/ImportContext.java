package com.github.jcloudburst;

import java.io.File;

public class ImportContext {
  private File file;
  private String worksheet;
  private int sourceRowCount;
  private int totalRowCount;

  public File getFile() {
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

  public void newExcelSource(File fileName, String worksheet) {
    file = fileName;
    this.worksheet = worksheet;
    sourceRowCount = 0;
  }

  public void newDelimitedSource(File fileName) {
    file = fileName;
    worksheet = null;
    sourceRowCount = 0;
  }

  public void incrementRows() {
    sourceRowCount++;
    totalRowCount++;
  }
}
