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
      DelimitedFileReader reader = new DelimitedFileReader(src);
      if (src.hasHeaderRow) {
        List<String> header = reader.getHeader();
        headers.put(src, header);
      } else {
        headers.put(src, null);
      }
    }
    
    for (ExcelSource src : config.getExcelSources()) {
      ExcelFileSource reader = new ExcelFileSource(src);
      if (src.hasHeaderRow) {
        List<String> header = reader.getHeader();
        headers.put(src, header);
      } else {
        headers.put(src, null);
      }
    }
  }
  
  public boolean allAligned() {
    int length = -1;
    for (List<String> header : headers.values()) {
      if (header == null) {
        // skip
      } else if (length < 0) {
        length = header.size();
      } else if (header.size() != length) {
        return false;
      }
    }
  }
}
