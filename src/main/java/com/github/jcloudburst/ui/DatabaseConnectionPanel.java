package com.github.jcloudburst.ui;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.JDBCType;

@SuppressWarnings("serial")
public class DatabaseConnectionPanel extends ConfigStepPanel {
  private JTextField jdbcUrlField;
  private JTextField jdbcUsernameField;
  private JPasswordField jdbcPasswordField;

  private JButton testButton;

  public DatabaseConnectionPanel() {
    super("Database");
    jdbcUrlField = new JTextField();
    jdbcUsernameField = new JTextField();
    jdbcPasswordField = new JPasswordField();
    testButton = new JButton("Test Connection");

    testButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        verifyConnection();
      }
    });

    setLayout(new MigLayout("", "[|grow]"));

    add(new JLabel("URL"), "right");
    add(jdbcUrlField, "grow,wrap");
    add(new JLabel("Username"), "right");
    add(jdbcUsernameField, "grow,wrap");
    add(new JLabel("Password"), "right");
    add(jdbcPasswordField, "grow,wrap");

    add(new JSeparator(), "span,grow,wrap");
    add(testButton, "span,right,wrap");
  }

  @Override
  protected void flushConfigurationToUI() {
    jdbcUrlField.setText(config.getJdbc().getUrl());
    jdbcUsernameField.setText(config.getJdbc().getUsername());
    jdbcPasswordField.setText(config.getJdbc().getPassword());
  }

  @Override
  protected void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException {
    JDBCType jdbc = config.getJdbc();
    if (jdbc == null) {
      jdbc = new JDBCType();
      config.setJdbc(jdbc);
    }

    verifyNotEmpty("URL", jdbcUrlField.getText());

    jdbc.setUrl(jdbcUrlField.getText());
    jdbc.setUsername(jdbcUsernameField.getText());
    jdbc.setPassword(new String(jdbcPasswordField.getPassword()));
  }

  private void verifyConnection() {
    final String url = jdbcUrlField.getText();
    final String username = jdbcUsernameField.getText();
    final String password = new String(jdbcPasswordField.getPassword());

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        if (url == null || url.isEmpty()) {
          throw new IllegalArgumentException("URL must not be empty");
        }

        String query = null;
        if (url.toLowerCase().contains("oracle")) {
          query = "SELECT 1 FROM DUAL";
        } else {
          query = "SELECT 1";
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
          Statement statement = connection.createStatement();
          ResultSet set = statement.executeQuery(query);
          set.next();
        }

        return null;
      }

      @Override
      protected void done() {
        try {
          get();
          showMessageDialog(DatabaseConnectionPanel.this, "Connection successful!", "Success!", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
          showMessageDialog(DatabaseConnectionPanel.this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }
}
