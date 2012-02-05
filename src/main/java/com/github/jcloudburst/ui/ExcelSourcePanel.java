package com.github.jcloudburst.ui;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ConfigurationType;

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
  public void initFromConfiguration(ConfigurationType config) {
    table.initFrom(config.getExcel());
  }

  @Override
  public void addConfiguration(ConfigurationType config) {
    config.getExcel().addAll(table.getSourcesList());
  }
}
