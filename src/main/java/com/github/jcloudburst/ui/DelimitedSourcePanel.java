package com.github.jcloudburst.ui;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ConfigurationType;

@SuppressWarnings("serial")
public class DelimitedSourcePanel extends ConfigStepPanel {
  private DelimitedSourceTable table;

  public DelimitedSourcePanel() {
    table = new DelimitedSourceTable();

    setLayout(new MigLayout("", "[grow|grow]", "[grow]"));
    add(new JScrollPane(table), "span,grow,wrap");
    add(table.getDeleteButton(), "left");
    add(table.getAddButton(), "right");
  }

  @Override
  public void initFromConfiguration(ConfigurationType config) {
    table.initFrom(config.getCsv());
  }

  @Override
  public void addConfiguration(ConfigurationType config) {
    config.getCsv().addAll(table.getSourcesList());
  }
}
