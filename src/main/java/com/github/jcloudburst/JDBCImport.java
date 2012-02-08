package com.github.jcloudburst;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import javax.xml.bind.JAXB;

public class JDBCImport {
  private ConfigurationType config;

  public JDBCImport(ConfigurationType configuration) throws IllegalArgumentException {
    config = configuration;
  }

  public void execute(ImportListener listener) throws SQLException, IOException {
    Connection connection = DriverManager.getConnection(config.getJdbc().getUrl(),
        config.getJdbc().getUsername(), config.getJdbc().getPassword());

    ImportContext context = new ImportContext();
    try {
      if (config.getExcel() != null) {
        for (ExcelSource source : config.getExcel()) {
          ExcelFileSource sourceReader = new ExcelFileSource(source);
          context.newExcelSource(source.getFile(), source.getExcelSheet());

          importData(sourceReader, connection, listener, context);
        }
      }

      if (config.getCsv() != null) {
        for (DelimitedSource source : config.getCsv()) {
          DelimitedFileReader sourceReader = new DelimitedFileReader(source);
          context.newDelimitedSource(source.getFile());

          importData(sourceReader, connection, listener, context);
        }
      }
    } catch (SQLException e) {
      String err = null;
      if (context.getWorksheet() == null) {
        err = String.format("Error in row %d of file %s", context.getSourceRowCount(), context.getFile());
      } else {
        err = String.format("Error in row %d of worksheet %s of file %s", context.getSourceRowCount(), context.getWorksheet(),
            context.getFile());
      }

      throw new SQLException(err);
    }
  }

  protected void importData(SourceReader sourceReader, Connection connection, ImportListener listener, ImportContext context)
      throws IOException, SQLException {
    ColumnMapper mapper = new ColumnMapper(config.getMapping().getColumn());
    SourceFileHandler sourceHandler = new SourceFileHandler(sourceReader, config, mapper);
    RowHandler rowHandler = new RowHandler(config, context, connection);

    while (sourceHandler.hasNextRow()) {
      sourceHandler.fillRow(rowHandler);
      rowHandler.nextRow();
      context.incrementRows();
      listener.rowsProcessed(context.getTotalRowCount());

      if (context.getTotalRowCount() % 1000 == 0) {
        rowHandler.commitBatch();
      }
    }

    rowHandler.commitBatch();
    rowHandler.close();
  }

  public static void main(String[] args) throws Exception {
    String configFilePath = "./src/test/resources/testrun.xml";
    if (args.length > 1) {
      configFilePath = args[0];
    }

    File configFile = new File(configFilePath);
    if (!configFile.isFile()) {
      System.err.println("Config file does not exist: " + configFilePath);
      System.exit(1);
    }

    ConfigurationType config = JAXB.unmarshal(configFile, ConfigurationType.class);
    if (Boolean.TRUE.equals(config.getJdbc().isPasswordPrompt())) {
      System.out.println("Enter password for " + config.getJdbc().getUsername() + ": ");
      Scanner scanner = new Scanner(System.in);
      String password = scanner.next();
      scanner.close();

      config.getJdbc().setPassword(password);
    }

    JDBCImport importer = new JDBCImport(config);
    importer.execute(new ImportListener() {
      @Override
      public void totalRowsToBeProcessed(int count) {
      }

      @Override
      public void rowsProcessed(int count) {
        System.out.println(count);
      }
    });
  }
}
