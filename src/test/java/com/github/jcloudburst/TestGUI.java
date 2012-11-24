package com.github.jcloudburst;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JFrame;

import com.github.jcloudburst.ui.MainWindow;

public class TestGUI {
  public static void main(String[] args) throws Exception {
    String jdbc = "jdbc:h2:mem:test";
    Connection c = DriverManager.getConnection(jdbc);
    c.createStatement().executeUpdate("create table test (a varchar(100), b double)");

    ConfigurationType config = new ConfigurationType();
    config.setJdbc(new JDBCType());
    config.getJdbc().setUrl(jdbc);
    
    config.setTable("test");
    config.setAppend(false);
    config.setFailOnMissingColumn(true);
    DelimitedSource src = new DelimitedSource();
    src.setFile(TestGUI.class.getClassLoader().getResource("Test1.csv").getFile());
    src.setHasHeaderRow(true);
    src.setSeparatorChar(",");
    config.getCsv().add(src);
    
    config.setMapping(new ColumnsType());
    
    ColumnMapType map = new ColumnMapType();
    map.setDbColumn("a");
    map.setFileColName("char");
    config.getMapping().getColumn().add(map);
    
    map = new ColumnMapType();
    map.setDbColumn("b");
    map.setFileColIndex(1);
    config.getMapping().getColumn().add(map);

    MainWindow window = new MainWindow(config);
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
