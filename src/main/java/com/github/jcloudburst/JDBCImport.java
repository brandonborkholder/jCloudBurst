package com.github.jcloudburst;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.github.jcloudburst.config.DelimitedSource;
import com.github.jcloudburst.config.ExcelSource;
import com.github.jcloudburst.config.ImportConfig;

public class JDBCImport {
  private ImportConfig config;

  public JDBCImport(ImportConfig configuration) throws IllegalArgumentException {
    config = configuration;
  }

  public void execute(ImportListener listener) throws SQLException, IOException {
    ImportContext context = new ImportContext();

    try (Connection c = DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUsername(), config.getJdbcPassword())) {
      for (ExcelSource source : config.getExcelSources()) {
        listener.setCurrentSource(String.format("sheet %s in %s", source.excelSheet, source.file.getName()));
        ExcelFileSource sourceReader = new ExcelFileSource(source);
        context.newExcelSource(source.file, source.excelSheet);

        importData(sourceReader, c, listener, context);
      }

      for (DelimitedSource source : config.getDelimitedSources()) {
        listener.setCurrentSource(source.file.getName());
        DelimitedFileReader sourceReader = new DelimitedFileReader(source);
        context.newDelimitedSource(source.file);

        importData(sourceReader, c, listener, context);
      }
    } catch (SQLException e) {
      String err = null;
      if (context.getWorksheet() == null) {
        err = String.format("Error in row %d of file %s", context.getSourceRowCount(), context.getFile());
      } else {
        err = String.format("Error in row %d of worksheet %s of file %s", context.getSourceRowCount(), context.getWorksheet(),
            context.getFile());
      }

      throw new SQLException(err, e);
    }
  }

  protected void importData(SourceReader sourceReader, Connection c, ImportListener listener, ImportContext context)
      throws IOException, SQLException {
    ColumnMapper mapper = new ColumnMapper(config);
    SourceFileHandler sourceHandler = new SourceFileHandler(sourceReader, config, mapper);
    RowHandler rowHandler = new RowHandler(config, context, c);

    while (sourceHandler.hasNextRow()) {
      sourceHandler.fillRow(rowHandler);
      rowHandler.nextRow();
      context.incrementRows();
      listener.totalRowsProcessed(context.getTotalRowCount());

      if (context.getTotalRowCount() % 1000 == 0) {
        rowHandler.commitBatch();
      }
    }

    rowHandler.commitBatch();
    rowHandler.close();
  }
}
