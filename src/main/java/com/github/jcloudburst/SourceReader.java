package com.github.jcloudburst;

import java.io.IOException;
import java.util.List;

public interface SourceReader {
  boolean next() throws IOException;

  List<String> getHeader() throws IOException;

  String getValue(int column) throws IOException;

  void close() throws IOException;
}
