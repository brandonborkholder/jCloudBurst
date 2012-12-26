package com.github.jcloudburst;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.github.jcloudburst.config.DelimitedSource;

public class DelimitedFileReader implements SourceReader {
  private List<String> names;

  private char separator;

  private long fileBytes;
  private CountingStream counter;
  private CSVReader reader;

  private String[] rowData;

  public DelimitedFileReader(DelimitedSource source) throws IOException {
    if (source.separator == null || source.separator.isEmpty()) {
      separator = ',';
    } else {
      separator = source.separator.charAt(0);
    }

    fileBytes = source.file.length();
    counter = new CountingStream(new FileInputStream(source.file));
    reader = new CSVReader(new InputStreamReader(counter), separator);

    if (source.hasHeaderRow) {
      String[] row = reader.readNext();
      names = Collections.unmodifiableList(Arrays.asList(row));
    } else {
      names = null;
    }
  }

  @Override
  public double getPercentRead() {
    return counter.count / (double) fileBytes;
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

  private static class CountingStream extends InputStream {
    long count;

    InputStream delegate;

    CountingStream(InputStream stream) {
      delegate = stream;
    }

    @Override
    public int read() throws IOException {
      int i = delegate.read();
      if (0 <= i) {
        inc(1);
      }

      return i;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
      return inc(delegate.read(buf, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
      return incLong(delegate.skip(n));
    }

    @Override
    public void reset() throws IOException {
      delegate.reset();
    }

    @Override
    public boolean markSupported() {
      return delegate.markSupported();
    }

    int inc(int i) {
      if (0 < i) {
        count += i;
      }

      return i;
    }

    long incLong(long l) {
      if (0 < l) {
        count += l;
      }

      return l;
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }
  }
}
