package com.github.jcloudburst.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.jcloudburst.ExcelSource;

@SuppressWarnings("serial")
public class ExcelSourceTable extends JTable {
  private ExcelSourceTableModel model;
  private JButton addButton;
  private JButton delButton;

  public ExcelSourceTable() {
    model = new ExcelSourceTableModel();
    setModel(model);

    getColumnModel().getColumn(0).setCellEditor(new FileChooserEditor());
    // XXX TODO

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        getDeleteButton().setEnabled(getSelectedRow() >= 0);
      }
    });
  }

  public JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton("Add");
      addButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          model.addRow();
        }
      });
    }

    return addButton;
  }

  public JButton getDeleteButton() {
    if (delButton == null) {
      delButton = new JButton("Delete");
      delButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int selectedRow = getSelectedRow();
          if (selectedRow >= 0) {
            model.removeRow(selectedRow);
          }
        }
      });
    }

    return delButton;
  }

  public List<ExcelSource> getSourcesList() {
    return Collections.unmodifiableList(model.sourcesList);
  }

  public void initFrom(List<ExcelSource> csv) {
    model.sourcesList.clear();
    model.sourcesList.addAll(csv);
    model.fireTableDataChanged();
  }
}
