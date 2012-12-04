package com.github.jcloudburst;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JFrame;

import com.github.jcloudburst.config.ColumnSource;
import com.github.jcloudburst.config.DelimitedSource;
import com.github.jcloudburst.config.ImportConfig;
import com.github.jcloudburst.ui.MainWindow;

public class LargeGUITest {
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
        .withFile(writeFile(1000000, "a", "b"))
        .withHasHeaderRow(true)
        .withSeparator(",");
    config.add(src);
    src = new DelimitedSource()
        .withFile(writeFile(1000000, "a", "b"))
        .withHasHeaderRow(true)
        .withSeparator(",");
    config.add(src);
    src = new DelimitedSource()
        .withFile(writeFile(1000000, "a", "b"))
        .withHasHeaderRow(true)
        .withSeparator(",");
    config.add(src);

    config.setColumnSource("a_column", new ColumnSource().withFileFieldIndex(0));
    config.setColumnSource("b_column", new ColumnSource().withFileFieldIndex(1));

    MainWindow window = new MainWindow(config);
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }

  static File writeFile(int rowCount, String... columns) throws IOException {
    File f = File.createTempFile("import", ".csv");

    System.out.println("Writing " + rowCount + " rows to " + f);

    PrintStream out = new PrintStream(f);

    for (int j = 0; j < columns.length; j++) {
      if (j > 0) {
        out.print(",");
      }
      out.print(columns[j]);
    }

    out.println();

    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columns.length; j++) {
        if (j > 0) {
          out.print(",");
        }

        out.print(Math.random() * 10000);
      }

      out.println();
      
      if (i % 100000 == 0) {
        System.out.println("Finished with " + i + " rows ...");
      }
    }

    out.close();

    f.deleteOnExit();

    return f;
  }
}
