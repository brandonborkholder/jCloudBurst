package com.github.jcloudburst;

import java.io.IOException;
import java.util.List;

public interface SourceReader extends AutoCloseable {
  boolean next() throws IOException;

  List<String> getHeader() throws IOException;
  
  double getPercentRead();

  String getValue(int column) throws IOException;

  void close() throws IOException;
}
