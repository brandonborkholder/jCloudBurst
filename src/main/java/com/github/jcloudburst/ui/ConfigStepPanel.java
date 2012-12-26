package com.github.jcloudburst.ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Action;
import javax.swing.JPanel;

import com.github.jcloudburst.config.ImportConfig;

@SuppressWarnings("serial")
public abstract class ConfigStepPanel extends JPanel {
  protected ImportConfig config;

  private List<TaskStatusTextListener> statusListeners;

  public ConfigStepPanel(String name) {
    setName(name);
    statusListeners = new CopyOnWriteArrayList<>();
  }

  public void loadConfiguration(ImportConfig config) throws IllegalStateException {
    this.config = config;
    flushConfigurationToUI();
  }

  public void saveToConfiguration(ImportConfig config) throws IllegalStateException {
    this.config = config;
    flushUIToConfiguration();
  }

  protected abstract void flushUIToConfiguration() throws IllegalStateException;

  protected abstract void flushConfigurationToUI() throws IllegalStateException;

  protected Action getCustomNextAction() {
    return null;
  }

  public void addTaskStatusListener(TaskStatusTextListener l) {
    statusListeners.add(l);
  }

  protected void setBackgroundTaskStatus(String status) {
    for (TaskStatusTextListener l : statusListeners) {
      l.statusChanged(this, status);
    }
  }

  protected void verifyNotEmpty(String name, Object value) throws IllegalStateException {
    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
      throw new IllegalStateException(name + " must not be empty");
    }
  }

  protected Connection getConnection() throws SQLException {
    return DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUsername(), config.getJdbcPassword());
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
