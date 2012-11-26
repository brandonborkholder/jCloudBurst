package com.github.jcloudburst.ui;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class ExcelSourcePanel extends ConfigStepPanel {
  private ExcelSourceTable table;

  public ExcelSourcePanel() {
    super("Excel");
    table = new ExcelSourceTable();

    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  @Override
  protected void flushConfigurationToUI() throws IllegalStateException {
    table.initFrom(config.getExcel());
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    config.getExcel().clear();
    config.getExcel().addAll(table.getSourcesList());
  }
}
