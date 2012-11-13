package com.github.jcloudburst.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.github.jcloudburst.DelimitedSource;

@SuppressWarnings("serial")
public class DelimitedSourceTableModel extends AbstractTableModel {
  List<DelimitedSource> sourcesList = new ArrayList<DelimitedSource>();

  public void addRow() {
    sourcesList.add(new DelimitedSource());
    fireTableDataChanged();
  }

  public void removeRow(int row) {
    sourcesList.remove(row);
    fireTableDataChanged();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public int getRowCount() {
    return sourcesList.size();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
    case 0:
      return String.class;

    case 1:
      return String.class;

    case 2:
      return Boolean.class;

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
    case 0:
      return "File";

    case 1:
      return "Separator";

    case 2:
      return "Header Row?";

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    DelimitedSource row = sourcesList.get(rowIndex);
    switch (columnIndex) {
    case 0:
      return row.getFile();

    case 1:
      return row.getSeparatorChar();

    case 2:
      return row.isHasHeaderRow();

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    DelimitedSource row = sourcesList.get(rowIndex);
    switch (columnIndex) {
    case 0:
      row.setFile((String) aValue);
      break;

    case 1:
      row.setSeparatorChar((String) aValue);
      break;

    case 2:
      row.setHasHeaderRow((Boolean) aValue);
      break;

    default:
      throw new AssertionError("Illegal column index");
    }
  }
}
