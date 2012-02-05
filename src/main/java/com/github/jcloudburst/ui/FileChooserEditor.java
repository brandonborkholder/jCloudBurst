package com.github.jcloudburst.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FileChooserEditor extends AbstractCellEditor implements TableCellEditor {
  private String lastDir = ".";

  private String path;

  @Override
  public Object getCellEditorValue() {
    return path;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    path = (String) value;
    JFileChooser chooser = new JFileChooser(path == null ? lastDir : path);
    chooser.setMultiSelectionEnabled(false);

    int result = chooser.showOpenDialog(table);
    if (result == JFileChooser.APPROVE_OPTION) {
      File chosen = chooser.getSelectedFile();
      path = chosen.toString();
      fireEditingStopped();
    } else {
      fireEditingCanceled();
    }

    return new JPanel();
  }
}
