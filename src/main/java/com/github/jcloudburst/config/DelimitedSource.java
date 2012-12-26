package com.github.jcloudburst.config;

import java.io.File;

public class DelimitedSource implements Cloneable {
  public final File file;
  public final String separator;
  public final boolean hasHeaderRow;

  public DelimitedSource() {
    this(null, null, false);
  }

  protected DelimitedSource(File file, String separatorChar, boolean hasHeaderRow) {
    this.file = file;
    this.separator = separatorChar;
    this.hasHeaderRow = hasHeaderRow;
  }

  public DelimitedSource withFile(File file) {
    return new DelimitedSource(file, separator, hasHeaderRow);
  }

  public DelimitedSource withSeparator(String separator) {
    return new DelimitedSource(file, separator, hasHeaderRow);
  }

  public DelimitedSource withHasHeaderRow(boolean hasHeaderRow) {
    return new DelimitedSource(file, separator, hasHeaderRow);
  }

  public boolean isValid() {
    return file != null && file.exists() && separator != null;
  }

  @Override
  public String toString() {
    return String.format("Delimited[file=%s,separator=%s,hasHeader=%b]", file, separator, hasHeaderRow);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    result = prime * result + (hasHeaderRow ? 1231 : 1237);
    result = prime * result + ((separator == null) ? 0 : separator.hashCode());
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
    if (!(obj instanceof DelimitedSource)) {
      return false;
    }
    DelimitedSource other = (DelimitedSource) obj;
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
    if (separator == null) {
      if (other.separator != null) {
        return false;
      }
    } else if (!separator.equals(other.separator)) {
      return false;
    }
    return true;
  }

  @Override
  public DelimitedSource clone() {
    try {
      return (DelimitedSource) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("unexpected clone not supported", e);
    }
  }
}
