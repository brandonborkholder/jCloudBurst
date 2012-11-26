package com.github.jcloudburst.ui;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class DelimitedSourcePanel extends ConfigStepPanel {
  private DelimitedSourceTable table;

  public DelimitedSourcePanel() {
    super("CSV");
    table = new DelimitedSourceTable();

    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  @Override
  protected void flushConfigurationToUI() throws IllegalStateException {
    table.initFrom(config.getCsv());
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    config.getCsv().clear();
    config.getCsv().addAll(table.getSourcesList());
  }
}
