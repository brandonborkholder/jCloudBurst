package com.github.jcloudburst;

import static java.lang.String.format;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.github.jcloudburst.config.DelimitedSource;
import com.github.jcloudburst.config.ExcelSource;
import com.github.jcloudburst.config.ImportConfig;

public class JDBCImport {
  private static final Logger LOGGER = Logger.getLogger(JDBCImport.class);

  private ImportConfig config;

  public JDBCImport(ImportConfig configuration) throws IllegalArgumentException {
    config = configuration;
  }

  public void execute(ImportListener listener) throws SQLException, IOException {
    ImportContext context = new ImportContext();

    try (Connection c = DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUsername(), config.getJdbcPassword())) {
      for (ExcelSource source : config.getExcelSources()) {
        String srcString = format("sheet %s in %s", source.excelSheet, source.file.getName());
        LOGGER.info("Starting " + source);
        listener.setCurrentSource(srcString);

        ExcelFileSource sourceReader = new ExcelFileSource(source);
        context.newExcelSource(source.file, source.excelSheet);

        importData(sourceReader, c, listener, context);
      }

      for (DelimitedSource source : config.getDelimitedSources()) {
        LOGGER.info("Starting " + source);
        listener.setCurrentSource(source.file.getName());
        DelimitedFileReader sourceReader = new DelimitedFileReader(source);
        context.newDelimitedSource(source.file);

        importData(sourceReader, c, listener, context);
      }

      LOGGER.info("Import completed with " + context.getTotalRowCount() + " rows imported");
    } catch (SQLException e) {
      String err = null;
      if (context.getWorksheet() == null) {
        err = String.format("Error in row %d of file %s", context.getSourceRowCount(), context.getFile());
      } else {
        err = String.format("Error in row %d of worksheet %s of file %s", context.getSourceRowCount(), context.getWorksheet(),
            context.getFile());
      }

      LOGGER.error(err, e);
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
      context.incrementRowCounts();
      listener.totalRowsProcessed(context.getTotalRowCount());
      listener.setPercentThroughSource(sourceReader.getPercentRead());

      if (context.getTotalRowCount() % 1000 == 0) {
        LOGGER.debug("Committing latest batch with " + context.getTotalRowCount() + " total rows");
        rowHandler.commitBatch();
      }
    }

    LOGGER.debug("Committing final batch with " + context.getTotalRowCount() + " total rows");
    rowHandler.commitBatch();
    rowHandler.close();
  }
}
