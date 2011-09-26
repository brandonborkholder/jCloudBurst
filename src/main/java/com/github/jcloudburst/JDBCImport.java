package com.github.jcloudburst;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.bind.JAXB;

public class JDBCImport {
  private ConfigurationType config;

  public JDBCImport(ConfigurationType configuration) throws IllegalArgumentException {
    config = configuration;
  }

  public void execute(ImportListener listener) throws SQLException, IOException {
    Connection connection = DriverManager.getConnection(config.getJdbcUrl());
    int rows = 0;

    for (String file : config.getFile()) {
      for (String worksheet : config.getExcelSheet()) {
        connection.setAutoCommit(false);
        ImportDataSource source = new ExcelFileSource(new File(file), worksheet, config, new ColumnMapper(config.getMapping().getColumn()));
        RowHandler handler = new RowHandler(config, new File(file), connection);

        int worksheetRow = 0;
        try {
          while (source.hasNextRow()) {
            source.fillRow(handler);
            handler.nextRow();
            rows++;
            worksheetRow++;
            listener.rowsProcessed(rows);
          }
        } catch (SQLException e) {
          throw new SQLException("Error in row " + worksheetRow + " of worksheet '" + worksheet + "' of file '" + file, e);
        }

        handler.close();
        connection.commit();
      }
    }
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
