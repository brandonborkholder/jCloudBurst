package com.github.jcloudburst.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class FileSourceChooser extends ConfigStepPanel {
  private JTabbedPane tabs;

  public FileSourceChooser() {
    super("Source");
    tabs = new JTabbedPane();

    DelimitedSourcePanel delimitedSource = new DelimitedSourcePanel();
    tabs.addTab("Delimited", delimitedSource);

    ExcelSourcePanel excelSource = new ExcelSourcePanel();
    tabs.addTab("Excel", excelSource);

    setLayout(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }

  @Override
  protected String getExplanationText() {
    return "Select the source files, which may be a combination of Excel and delimited plain text. However, " +
        "all source files must have exactly the same format (including header, if present).";
  }

  @Override
  protected void flushConfigurationToUI() throws IllegalStateException {
    for (Component c : tabs.getComponents()) {
      ((ConfigStepPanel) c).loadConfiguration(config);
    }
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    for (Component c : tabs.getComponents()) {
      ((ConfigStepPanel) c).saveToConfiguration(config);
    }
  }
}
