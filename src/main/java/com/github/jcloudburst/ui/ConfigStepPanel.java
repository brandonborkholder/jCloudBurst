package com.github.jcloudburst.ui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.github.jcloudburst.ConfigurationType;

@SuppressWarnings("serial")
public abstract class ConfigStepPanel extends JPanel {
  protected ConfigurationType config;

  public ConfigStepPanel(String step) {
    setName(step);
  }

  public void loadConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException {
    this.config = config;
    flushConfigurationToUI();
  }

  public void saveToConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException {
    this.config = config;
    flushUIToConfiguration();
  }

  protected abstract void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException;

  protected abstract void flushConfigurationToUI() throws SQLException, IOException, IllegalStateException;

  protected void verifyNotEmpty(String name, Object value) throws IllegalStateException {
    if (value != null || (value instanceof String && ((String) value).isEmpty())) {
      throw new IllegalStateException(name + " must not be empty");
    }
  }

  protected Connection getConnection() throws SQLException {
    return DriverManager.getConnection(config.getJdbc().getUrl(), config.getJdbc().getUsername(), config.getJdbc().getPassword());
  }
}
