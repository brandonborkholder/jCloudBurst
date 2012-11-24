package com.github.jcloudburst.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ConfigurationType;
import com.github.jcloudburst.JDBCType;

@SuppressWarnings("serial")
public class DatabaseConnectionPanel extends ConfigStepPanel {
  private JTextField jdbcUrlField;
  private JTextField jdbcUsernameField;
  private JPasswordField jdbcPasswordField;

  private JButton testButton;

  private JLabel connectionStatusLabel;

  private ConfigPartialState lastVerifiedState;

  public DatabaseConnectionPanel() {
    super("Database");
    jdbcUrlField = new JTextField();
    jdbcUsernameField = new JTextField();
    jdbcPasswordField = new JPasswordField();
    testButton = new JButton("Test Connection");
    connectionStatusLabel = new JLabel();
    connectionStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);

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

    add(testButton, "right");
    add(connectionStatusLabel, "grow");
  }

  @Override
  protected void flushConfigurationToUI() {
    jdbcUrlField.setText(config.getJdbc().getUrl());
    jdbcUsernameField.setText(config.getJdbc().getUsername());
    jdbcPasswordField.setText(config.getJdbc().getPassword());

    setStatus("connection not verified", false);

    if (lastVerifiedState == null || !lastVerifiedState.equals(new ConnectionState(config))) {
      if (canVerifyConnection()) {
        verifyConnection();
      }
    }
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    JDBCType jdbc = config.getJdbc();

    verifyNotEmpty("URL", jdbcUrlField.getText());

    jdbc.setUrl(jdbcUrlField.getText());
    jdbc.setUsername(jdbcUsernameField.getText());
    jdbc.setPassword(new String(jdbcPasswordField.getPassword()));
  }

  private boolean canVerifyConnection() {
    return jdbcUrlField.getText().startsWith("jdbc:");
  }

  private void verifyConnection() {
    final String url = jdbcUrlField.getText();
    final String user = jdbcUsernameField.getText();
    final String pass = new String(jdbcPasswordField.getPassword());

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

        setBackgroundTaskStatus("connecting to database ...");
        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
          Statement statement = connection.createStatement();
          statement.setQueryTimeout(1);
          ResultSet set = statement.executeQuery(query);
          set.next();
        }

        lastVerifiedState = new ConnectionState(url, user, pass);

        return null;
      }

      protected void done() {
        setBackgroundTaskStatus(null);

        String status = String.format("connection to %s (%s) ", url, user.isEmpty() ? "no user" : "as " + user);
        try {
          get();
          status += "is successful!";
          setStatus(status, true);
        } catch (Exception e) {
          e.printStackTrace();
          status += "failed: " + e.getMessage();
          setStatus(status, false);
        }

      }
    }.execute();
  }

  private void setStatus(String status, boolean success) {
    connectionStatusLabel.setText(status);

    if (success) {
      connectionStatusLabel.setForeground(getForeground());
    } else {
      connectionStatusLabel.setForeground(Color.red);
    }
  }

  public static class ConnectionState extends ConfigPartialState {
    public ConnectionState(ConfigurationType c) {
      this(c.getJdbc().getUrl(), c.getJdbc().getUsername(), c.getJdbc().getPassword());
    }

    public ConnectionState(String url, String user, String pass) {
      add("URL", url);
      add("Username", user);
      add("Password", pass);
    }
  }
}
