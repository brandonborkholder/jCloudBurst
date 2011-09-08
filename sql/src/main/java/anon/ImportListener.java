package anon;

public interface ImportListener {
  void totalRowsToBeProcessed(int count);
  
  void rowsProcessed(int count);
}
