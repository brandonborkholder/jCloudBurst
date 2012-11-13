package com.github.jcloudburst.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class DelimitedSourcePanel extends ConfigStepPanel {
  private DelimitedSourceTable table;

  public DelimitedSourcePanel() {
    super("CSV");
    table = new DelimitedSourceTable();

    setLayout(new MigLayout("", "[grow|grow]", "[grow]"));
    add(new JScrollPane(table), "span,grow,wrap");
    add(table.getDeleteButton(), "left");
    add(table.getAddButton(), "right");
  }

  @Override
  protected void flushConfigurationToUI() throws SQLException, IOException, IllegalStateException {
    table.initFrom(config.getCsv());
  }

  @Override
  protected void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException {
    config.getCsv().clear();
    config.getCsv().addAll(table.getSourcesList());
  }
}
