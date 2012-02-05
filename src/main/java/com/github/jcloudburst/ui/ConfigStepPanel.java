package com.github.jcloudburst.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.github.jcloudburst.ConfigurationType;

@SuppressWarnings("serial")
public abstract class ConfigStepPanel extends JPanel {
  public abstract void initFromConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException;

  public abstract void addConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException;
}
