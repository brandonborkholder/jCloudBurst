package com.github.jcloudburst;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public interface ImportDataSource {
  boolean hasNextRow();

  void fillRow(RowHandler handler) throws SQLException, IOException, ParseException;
}
