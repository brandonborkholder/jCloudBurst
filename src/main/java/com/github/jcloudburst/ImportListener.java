package com.github.jcloudburst;

public interface ImportListener {
  void setCurrentSource(String source);

  void setPercentThroughSource(double percent);
  
  void totalRowsProcessed(long count);
}
