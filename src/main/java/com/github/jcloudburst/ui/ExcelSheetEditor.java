package com.github.jcloudburst.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@SuppressWarnings("serial")
public class ExcelSheetEditor extends DefaultCellEditor {
  private static final Logger LOGGER = Logger.getLogger(ExcelSheetEditor.class);

  private JComboBox<String> sheetChooser;

  @SuppressWarnings("unchecked")
  public ExcelSheetEditor() {
    super(new JComboBox<>());
    sheetChooser = (JComboBox<String>) getComponent();
    sheetChooser.setEditable(true);
    setClickCountToStart(2);
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    ComboBoxModel<String> model = new DefaultComboBoxModel<>();

    Object file = table.getModel().getValueAt(row, ExcelSourceTableModel.FILE_COL);
    if (file instanceof File) {
      try {
        model = createModel((File) file);
      } catch (Exception e) {
        LOGGER.error("Could not read Excel file: " + file);
      }
    }

    sheetChooser.setModel(model);

    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
  }

  private ComboBoxModel<String> createModel(File excelFile) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getAbsolutePath());

    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

    int numSheets = workbook.getNumberOfSheets();
    for (int i = 0; i < numSheets; i++) {
      model.addElement(workbook.getSheetName(i));
    }

    return model;
  }
}
