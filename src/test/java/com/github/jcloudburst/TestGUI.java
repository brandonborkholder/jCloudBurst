package com.github.jcloudburst;

import java.awt.Dimension;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JFrame;

import com.github.jcloudburst.config.ColumnSource;
import com.github.jcloudburst.config.DelimitedSource;
import com.github.jcloudburst.config.ImportConfig;
import com.github.jcloudburst.ui.MainWindow;

public class TestGUI {
  public static void main(String[] args) throws Exception {
    String jdbc = "jdbc:h2:mem:test";
    Connection c = DriverManager.getConnection(jdbc);
    c.createStatement().executeUpdate("create table test (a_column varchar(100), b_column double)");

    ImportConfig config = new ImportConfig();
    config.setJdbcUrl(jdbc);

    config.setTable("TEST.PUBLIC.TEST");
    config.setTruncateTable(true);
    config.setFailOnMissingColumn(true);
    DelimitedSource src = new DelimitedSource()
        .withFile(new File(TestGUI.class.getClassLoader().getResource("Test1.csv").getFile()))
        .withHasHeaderRow(true)
        .withSeparator(",");
    config.add(src);

    config.setColumnSource("a_column", new ColumnSource().withFileFieldName("A column"));
    config.setColumnSource("b_column", new ColumnSource().withFileFieldIndex(1));

    MainWindow window = new MainWindow(config);
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
