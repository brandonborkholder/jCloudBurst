package com.github.jcloudburst;

public interface ImportListener {
  void totalRowsToBeProcessed(int count);
  
  void rowsProcessed(int count);
}
