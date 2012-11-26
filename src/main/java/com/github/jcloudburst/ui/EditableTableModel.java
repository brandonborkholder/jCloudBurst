package com.github.jcloudburst.ui;

import javax.swing.table.TableModel;

public interface EditableTableModel extends TableModel {
  void addRow();

  void removeRow(int row);
}
