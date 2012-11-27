package com.github.jcloudburst.ui;

import static com.github.jcloudburst.ui.DelimitedSourceTableModel.DELETE_COL;
import static com.github.jcloudburst.ui.DelimitedSourceTableModel.DELIMITER_COL;
import static com.github.jcloudburst.ui.DelimitedSourceTableModel.FILE_COL;

import java.util.Collections;
import java.util.List;

import javax.swing.JTable;

import com.github.jcloudburst.config.DelimitedSource;

@SuppressWarnings("serial")
public class DelimitedSourceTable extends JTable {
  private DelimitedSourceTableModel model;

  public DelimitedSourceTable() {
    model = new DelimitedSourceTableModel();
    setModel(model);

    getColumnModel().getColumn(DELETE_COL).setCellEditor(new DeleteRowEditor());
    getColumnModel().getColumn(FILE_COL).setCellEditor(new FileChooserEditor());
    getColumnModel().getColumn(DELIMITER_COL).setCellEditor(new SeparatorCellEditor());
    getColumnModel().getColumn(DELIMITER_COL).setCellRenderer(new UTFStringRenderer());
  }

  public List<DelimitedSource> getSourcesList() {
    return Collections.unmodifiableList(model.sourcesList);
  }

  public void initFrom(List<DelimitedSource> csv) {
    model.sourcesList.clear();
    model.sourcesList.addAll(csv);
    model.fireTableDataChanged();
  }
}
