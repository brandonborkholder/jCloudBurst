package com.github.jcloudburst.ui;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import com.github.jcloudburst.config.ExcelSource;

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
    table.initFrom(config.getExcelSources());
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    config.clearExcelSources();
    for (ExcelSource src : table.getSourcesList()) {
      config.add(src);
    }
  }
}
