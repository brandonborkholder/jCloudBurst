package com.github.jcloudburst.ui;

import static com.github.jcloudburst.ui.ExcelSourceTableModel.DELETE_COL;
import static com.github.jcloudburst.ui.ExcelSourceTableModel.FILE_COL;
import static com.github.jcloudburst.ui.ExcelSourceTableModel.SHEET_COL;

import java.util.Collections;
import java.util.List;

import javax.swing.JTable;

import com.github.jcloudburst.ExcelSource;

@SuppressWarnings("serial")
public class ExcelSourceTable extends JTable {
  private ExcelSourceTableModel model;

  public ExcelSourceTable() {
    model = new ExcelSourceTableModel();
    setModel(model);

    getColumnModel().getColumn(DELETE_COL).setCellEditor(new DeleteRowEditor());
    getColumnModel().getColumn(FILE_COL).setCellEditor(new FileChooserEditor());
    getColumnModel().getColumn(SHEET_COL).setCellEditor(new ExcelSheetEditor());
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
