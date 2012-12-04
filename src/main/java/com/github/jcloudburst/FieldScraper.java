package com.github.jcloudburst;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jcloudburst.config.DelimitedSource;
import com.github.jcloudburst.config.ExcelSource;
import com.github.jcloudburst.config.ImportConfig;

public class FieldScraper {
  private ImportConfig config;

  private Map<Object, List<String>> headers;

  public FieldScraper(ImportConfig config) {
    this.config = config;
    headers = new HashMap<>();
  }

  public void read() throws IOException {
    for (DelimitedSource src : config.getDelimitedSources()) {
      try (DelimitedFileReader reader = new DelimitedFileReader(src)) {
        if (src.hasHeaderRow) {
          List<String> header = reader.getHeader();
          headers.put(src, header);
        } else {
          headers.put(src, null);
        }
      }
    }

    for (ExcelSource src : config.getExcelSources()) {
      try (ExcelFileSource reader = new ExcelFileSource(src)) {
        if (src.hasHeaderRow) {
          List<String> header = reader.getHeader();
          headers.put(src, header);
        } else {
          headers.put(src, null);
        }
      }
    }
  }

  public List<String> getCanonical() {
    List<String> canonical = null;
    for (List<String> header : headers.values()) {
      if (canonical == null) {
        canonical = header;
      } else if (!isSame(canonical, header)) {
        return null;
      }
    }

    return canonical;
  }

  public String getExplanation() {
    return null;
  }

  protected boolean isSame(List<String> header1, List<String> header2) {
    if ((header1 == null && header2 != null) ||
        (header1 != null && header2 == null)) {
      return false;
    }

    if (header1.size() != header2.size()) {
      return false;
    }

    for (int i = 0; i < header1.size(); i++) {
      String col1 = clean(header1.get(i));
      String col2 = clean(header2.get(i));

      if (!col1.equals(col2)) {
        return false;
      }
    }

    return true;
  }

  protected String clean(String str) {
    str = str == null ? "" : str.trim();
    str = str.replace(" ", "_");
    str = str.toUpperCase();
    return str;
  }
}
