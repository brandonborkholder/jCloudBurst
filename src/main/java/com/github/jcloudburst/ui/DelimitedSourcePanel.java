package com.github.jcloudburst.ui;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import com.github.jcloudburst.config.DelimitedSource;

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
    table.initFrom(config.getDelimitedSources());
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    config.clearDelimitedSources();
    for (DelimitedSource src : table.getSourcesList()) {
      config.add(src);
    }
  }
}
