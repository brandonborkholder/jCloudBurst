package com.github.jcloudburst;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;

import javax.xml.bind.JAXB;

public class JDBCImport {
  private ConfigurationType config;

  public JDBCImport(ConfigurationType configuration) throws IllegalArgumentException {
    this.config = configuration;
  }

  public void execute(ImportListener listener) throws SQLException, IOException, ParseException {
    Connection connection = DriverManager.getConnection(config.getJdbcUrl());
    connection.setAutoCommit(false);

    RowHandler handler = new RowHandler(config, connection);
    ImportDataSource source = new ExcelFileSource(config);
    int rows = 0;
    while (source.hasNextRow()) {
      source.fillRow(handler);
      handler.nextRow();
      rows++;
      listener.rowsProcessed(rows);
    }

    connection.commit();
  }

  public static void main(String[] args) throws Exception {
    String configFilePath = "./jdbcimport.xml";
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
