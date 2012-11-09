package com.github.jcloudburst.ui;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ExcelSourcePanel extends ConfigStepPanel {
  private ExcelSourceTable table;

  public ExcelSourcePanel() {
    table = new ExcelSourceTable();

    setLayout(new MigLayout("", "[grow|grow]", "[grow]"));
    add(new JScrollPane(table), "span,grow,wrap");
    add(table.getDeleteButton(), "left");
    add(table.getAddButton(), "right");
  }

  @Override
  protected void flushConfigurationToUI() throws SQLException, IOException, IllegalStateException {
    table.initFrom(config.getExcel());
  }

  @Override
  protected void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException {
    config.getExcel().clear();
    config.getExcel().addAll(table.getSourcesList());
  }
}
