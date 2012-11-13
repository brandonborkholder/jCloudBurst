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

    MainWindow window = new MainWindow(config);
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
