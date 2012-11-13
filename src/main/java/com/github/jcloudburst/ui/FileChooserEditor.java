package com.github.jcloudburst.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FileChooserEditor extends AbstractCellEditor implements TableCellEditor {
  private static String lastDir = ".";

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
      lastDir = chosen.getParent();
      fireEditingStopped();
    } else {
      fireEditingCanceled();
    }

    return new JLabel(path);
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent) evt).getClickCount() >= 2;
    } else {
      return true;
    }
  }
}
