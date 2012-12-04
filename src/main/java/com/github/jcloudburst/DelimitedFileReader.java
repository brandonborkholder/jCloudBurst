package com.github.jcloudburst;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.github.jcloudburst.config.DelimitedSource;

public class DelimitedFileReader implements SourceReader {
  private List<String> names;

  private char separator;

  private CSVReader reader;

  private String[] rowData;

  public DelimitedFileReader(DelimitedSource source) throws IOException {
    if (source.separator == null || source.separator.isEmpty()) {
      separator = ',';
    } else {
      separator = source.separator.charAt(0);
    }

    reader = new CSVReader(new FileReader(source.file), separator);

    if (source.hasHeaderRow) {
      String[] row = reader.readNext();
      names = Collections.unmodifiableList(Arrays.asList(row));
    } else {
      names = null;
    }
  }

  @Override
  public List<String> getHeader() {
    return names;
  }

  @Override
  public String getValue(int column) {
    if (column >= rowData.length) {
      return null;
    } else {
      return rowData[column];
    }
  }

  @Override
  public boolean next() throws IOException {
    rowData = reader.readNext();
    return rowData != null;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
