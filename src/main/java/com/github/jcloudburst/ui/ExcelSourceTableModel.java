package com.github.jcloudburst.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import com.github.jcloudburst.config.ExcelSource;

@SuppressWarnings("serial")
public class ExcelSourceTableModel extends AbstractTableModel implements EditableTableModel {
  public static final int DELETE_COL = 0;
  public static final int FILE_COL = 1;
  public static final int SHEET_COL = 2;
  public static final int HAS_HEADER_COL = 3;
  public static final int NUM_COLS = 4;

  List<ExcelSource> sourcesList = new ArrayList<ExcelSource>();

  @Override
  public void addRow() {
    sourcesList.add(new ExcelSource());
    fireTableDataChanged();
  }

  @Override
  public void removeRow(int row) {
    sourcesList.remove(row);
    fireTableDataChanged();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return !(rowIndex == sourcesList.size() && columnIndex == DELETE_COL);
  }

  @Override
  public int getRowCount() {
    return sourcesList.size() + 1;
  }

  @Override
  public int getColumnCount() {
    return NUM_COLS;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
    case DELETE_COL:
      return Icon.class;

    case FILE_COL:
      return File.class;

    case SHEET_COL:
      return String.class;

    case HAS_HEADER_COL:
      return Boolean.class;

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
    case DELETE_COL:
      return "Delete";

    case FILE_COL:
      return "File";

    case SHEET_COL:
      return "Sheet";

    case HAS_HEADER_COL:
      return "Header Row?";

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex == sourcesList.size()) {
      return null;
    }

    ExcelSource row = sourcesList.get(rowIndex);
    switch (columnIndex) {
    case DELETE_COL:
      return Icons.deleteIcon;

    case FILE_COL:
      return row.file;

    case SHEET_COL:
      return row.excelSheet;

    case HAS_HEADER_COL:
      return row.hasHeaderRow;

    default:
      throw new AssertionError("Illegal column index");
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex == sourcesList.size()) {
      addRow();
    }

    ExcelSource row = sourcesList.get(rowIndex);
    switch (columnIndex) {
    case FILE_COL:
      File f = (File) aValue;
      row = row.withFile(f);
      break;

    case SHEET_COL:
      row = row.withExcelSheet((String) aValue);
      break;

    case HAS_HEADER_COL:
      row = row.withHasHeaderRow(Boolean.TRUE.equals(aValue));
      break;

    default:
      throw new AssertionError("Illegal column index");
    }

    sourcesList.set(rowIndex, row);
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
