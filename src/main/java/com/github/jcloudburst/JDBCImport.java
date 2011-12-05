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

    int[] rowRefs = new int[2];
    if (config.getExcel() != null) {
      for (String file : config.getExcel().getFile()) {
        for (String worksheet : config.getExcel().getExcelSheet()) {
          ColumnMapper mapper = new ColumnMapper(config.getMapping().getColumn());
          ImportDataSource source = new ExcelFileSource(new File(file), worksheet, config, mapper);
          RowHandler handler = new RowHandler(config, new File(file), connection);

          try {
            importData(source, connection, listener, handler, rowRefs);
          } catch (SQLException e) {
            throw new SQLException("Error in row " + rowRefs[1] + " of worksheet " + worksheet + " of file " + file, e);
          }
        }
      }
    } else if (config.getCsv() != null) {
      for (String file : config.getCsv().getFile()) {
        ColumnMapper mapper = new ColumnMapper(config.getMapping().getColumn());
        ImportDataSource source = new DelimitedFileHandler(new File(file), config, mapper);
        RowHandler handler = new RowHandler(config, new File(file), connection);

        try {
          importData(source, connection, listener, handler, rowRefs);
        } catch (SQLException e) {
          throw new SQLException("Error in row " + rowRefs[1] + " of file " + file, e);
        }
      }
    }
  }

  protected void importData(ImportDataSource source, Connection connection, ImportListener listener, RowHandler handler, int[] rowRefs)
      throws IOException, SQLException {

    rowRefs[1] = 0;
    while (source.hasNextRow()) {
      source.fillRow(handler);
      handler.nextRow();
      rowRefs[0]++;
      rowRefs[1]++;
      listener.rowsProcessed(rowRefs[0]);

      if (rowRefs[1] % 1000 == 0) {
        handler.commitBatch();
      }
    }

    handler.commitBatch();
    handler.close();
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
