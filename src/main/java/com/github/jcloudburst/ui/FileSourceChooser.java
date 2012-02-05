package com.github.jcloudburst.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ConfigurationType;

@SuppressWarnings("serial")
public class FileSourceChooser extends ConfigStepPanel {
  private CardLayout switcherLayout;
  private JPanel switcherPanel;
  private ConfigStepPanel activePanel;

  public FileSourceChooser() {
    final DelimitedSourcePanel delimitedSource = new DelimitedSourcePanel();
    switcherPanel.add(delimitedSource, "delimited");

    JButton delimitedButton = new JButton("Delimited Text");
    delimitedButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        activePanel = delimitedSource;
        switcherLayout.show(switcherPanel, "delimited");
      }
    });

    final ExcelSourcePanel excelSource = new ExcelSourcePanel();
    switcherPanel.add(excelSource, "excel");

    JButton excelButton = new JButton("Excel");
    excelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        activePanel = excelSource;
        switcherLayout.show(switcherPanel, "excel");
      }
    });

    setLayout(new MigLayout("", "[grow|grow]", "[grow|]"));

    add(switcherPanel, "span,grow,wrap");
    add(delimitedButton, "right");
    add(excelButton, "left");
  }

  @Override
  public void initFromConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException {
    for (Component c : switcherPanel.getComponents()) {
      ((ConfigStepPanel) c).initFromConfiguration(config);
    }
  }

  @Override
  public void addConfiguration(ConfigurationType config) throws SQLException, IOException, IllegalStateException {
    activePanel.addConfiguration(config);
  }
}