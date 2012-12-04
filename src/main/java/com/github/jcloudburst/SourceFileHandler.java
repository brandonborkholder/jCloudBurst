package com.github.jcloudburst;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.github.jcloudburst.config.ImportConfig;

public class SourceFileHandler {
  protected ColumnMapper mapper;
  protected SourceReader reader;

  protected boolean hasNext;

  public SourceFileHandler(SourceReader reader, ImportConfig config, ColumnMapper mapper) throws IOException {
    this.mapper = mapper;
    this.reader = reader;
    
    hasNext = true;

    initializeMapper(config);
  }

  protected void initializeMapper(ImportConfig config) throws IOException {
    List<String> header = reader.getHeader();

    for (int colId = 0; colId < mapper.numColumns(); colId++) {
      if (!mapper.isColumnDefined(colId)) {
        if (header == null) {
          throw new IllegalArgumentException("No header row specified and no file column index specified for column " + colId);
        }

        int fileColIndex = -1;
        String fileColName = mapper.getFileColumnName(colId);

        for (int i = 0; i < header.size(); i++) {
          String value = header.get(i);
          if (value != null) {
            if (value.equalsIgnoreCase(fileColName)) {
              fileColIndex = i;
              break;
            }
          }
        }

        if (fileColIndex < 0) {
          throw new IllegalArgumentException("No file column index specified for column " + colId +
              " and no corresponding column named '" + fileColName + "'");
        } else {
          mapper.mapColToField(colId, fileColIndex);
        }
      }
    }

    advance();
  }

  public boolean hasNextRow() {
    return hasNext;
  }

  protected void advance() throws IOException {
    if (hasNext) {
      hasNext = reader.next();
      if (!hasNext) {
        reader.close();
      }
    }
  }

  public void fillRow(RowHandler handler) throws SQLException, IOException {
    int totalColumns = mapper.numColumns();

    for (int colId = 0; colId < totalColumns; colId++) {
      int fileColIndex = mapper.mapColToField(colId);
      if (fileColIndex >= 0) {
        String value = reader.getValue(fileColIndex);

        // if cell is null
        if (value == null || value.isEmpty()) {
          handler.setValue(colId, (String) null);
        } else {
          handler.setValue(colId, value);
        }
      }
    }

    advance();
  }
}
