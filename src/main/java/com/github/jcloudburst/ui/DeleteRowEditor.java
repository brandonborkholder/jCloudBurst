package com.github.jcloudburst.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class DeleteRowEditor extends AbstractCellEditor implements TableCellEditor {
  @Override
  public Object getCellEditorValue() {
    return null;
  }

  @Override
  public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent && ((MouseEvent)e).getClickCount() == 1;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    TableModel model = table.getModel();
    if (model instanceof EditableTableModel) {
      ((EditableTableModel) model).removeRow(row);
    }

    return null;
  }
}
