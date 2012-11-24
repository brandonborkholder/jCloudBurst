package com.github.jcloudburst.ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import com.github.jcloudburst.ConfigurationType;

@SuppressWarnings("serial")
public abstract class ConfigStepPanel extends JPanel {
  protected ConfigurationType config;

  public ConfigStepPanel(String name) {
    setName(name);
  }

  public void loadConfiguration(ConfigurationType config) throws IllegalStateException {
    this.config = config;
    flushConfigurationToUI();
  }

  public void saveToConfiguration(ConfigurationType config) throws IllegalStateException {
    this.config = config;
    flushUIToConfiguration();
  }

  protected abstract void flushUIToConfiguration() throws IllegalStateException;

  protected abstract void flushConfigurationToUI() throws IllegalStateException;
  
  protected void setBackgroundTaskStatus(String status) {
    System.out.println(new Date() + ": " + status);
  }

  protected void verifyNotEmpty(String name, Object value) throws IllegalStateException {
    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
      throw new IllegalStateException(name + " must not be empty");
    }
  }

  protected Connection getConnection() throws SQLException {
    return DriverManager.getConnection(config.getJdbc().getUrl(), config.getJdbc().getUsername(), config.getJdbc().getPassword());
  }

  public static class ConfigPartialState {
    private final Map<String, String> state = new TreeMap<>();

    protected void add(String key, String value) {
      state.put(key, value);
    }

    public boolean equals(ConfigPartialState other) {
      return state.equals(other.state);
    }

    @Override
    public String toString() {
      return state.toString();
    }
  }
}
