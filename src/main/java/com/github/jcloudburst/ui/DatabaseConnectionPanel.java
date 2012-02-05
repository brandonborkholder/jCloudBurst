package com.github.jcloudburst.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
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

  private JDBCType connectionProps;

  @Override
  public void initFromConfiguration(ConfigurationType config) {
    if (connectionProps != null) {
      jdbcUrlField.setText(config.getJdbc().getUrl());
      jdbcUsernameField.setText(config.getJdbc().getUsername());
      jdbcPasswordField.setText(config.getJdbc().getPassword());
    }
  }

  @Override
  public void addConfiguration(ConfigurationType config) throws SQLException {
    validateConnection(false);
    config.setJdbc(connectionProps);
  }

  public DatabaseConnectionPanel() {
    jdbcUrlField = new JTextField();
    jdbcUsernameField = new JTextField();
    jdbcPasswordField = new JPasswordField();
    testButton = new JButton("Test Connection");

    testButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          validateConnection(true);
        } catch (SQLException ex) {
          // ignore, already informing user
        }
      }
    });

    setLayout(new MigLayout());

    add(new JLabel("URL: "), "");
    add(jdbcUrlField, "grow,wrap");
    add(new JLabel("Username: "), "");
    add(jdbcUsernameField, "grow,wrap");
    add(new JLabel("Password: "), "");
    add(jdbcPasswordField, "grow,wrap");

    add(new JSeparator(), "span,grow,wrap");
    add(testButton, "span,left,wrap");

    setBorder(BorderFactory.createTitledBorder("JDBC Connection"));
  }

  private void validateConnection(final boolean informUserOnError) throws SQLException {
    final Exception[] exceptionRef = new Exception[1];

    final String url = jdbcUrlField.getText();
    final String username = jdbcUsernameField.getText();
    final String password = new String(jdbcPasswordField.getPassword());

    new SwingWorker<JDBCType, Void>() {
      @Override
      protected JDBCType doInBackground() throws Exception {
        if (url == null || url.isEmpty()) {
          throw new IllegalArgumentException("URL must not be empty");
        }

        String query = null;
        if (url.toLowerCase().contains("oracle")) {
          query = "SELECT 1 FROM DUAL";
        } else {
          query = "SELECT 1";
        }

        Connection connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery(query);
        set.next();

        connection.close();

        JDBCType props = new JDBCType();
        props.setUrl(url);
        props.setUsername(username);
        props.setPassword(password);

        return props;
      }

      @Override
      protected void done() {
        try {
          connectionProps = get();
          synchronized (DatabaseConnectionPanel.this) {
            DatabaseConnectionPanel.this.notifyAll();
          }
        } catch (Exception e) {
          if (informUserOnError) {
            JOptionPane.showMessageDialog(DatabaseConnectionPanel.this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
          } else {
            exceptionRef[0] = e;
          }
        }
      }
    }.execute();

    if (!informUserOnError) {
      try {
        synchronized (this) {
          wait();
        }
      } catch (InterruptedException e) {
        // continue
      }

      if (exceptionRef[0] != null) {
        throw new SQLException(exceptionRef[0]);
      }
    }
  }
}
