package com.github.jcloudburst;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.jcloudburst.RowHandler.SQLType;

public class ExcelFileSource implements ImportDataSource {
  protected Sheet activeSheet;

  protected Iterator<Row> rowIterator;

  protected ColumnMapper mapper;

  public ExcelFileSource(File workbookFile, String worksheet, ConfigurationType config, ColumnMapper mapper) throws IOException {
    this.mapper = mapper;

    XSSFWorkbook workbook = new XSSFWorkbook(workbookFile.getAbsolutePath());
    activeSheet = workbook.getSheet(worksheet);
    rowIterator = activeSheet.iterator();

    initializeMapper(config);
  }

  protected void initializeMapper(ConfigurationType config) {
    Row row = null;
    if (config.getExcel().isHasHeaderRow()) {
      row = rowIterator.next();
    }

    for (int colId = 0; colId < mapper.numColumns(); colId++) {
      if (!mapper.isColumnDefined(colId)) {
        if (row == null) {
          throw new IllegalArgumentException("No header row specified and no file column index specified for column " + colId);
        }

        int fileColIndex = -1;
        String fileColName = mapper.getFileColumnName(colId);
        int lastCell = row.getLastCellNum();

        for (int i = 0; i < lastCell; i++) {
          Cell cell = row.getCell(i);
          if (cell != null) {
            String cellValue = cell.getStringCellValue();
            if (fileColName.equalsIgnoreCase(cellValue)) {
              fileColIndex = i;
              break;
            }
          }
        }

        if (fileColIndex < 0) {
          throw new IllegalArgumentException("No file column index specified for column " + colId +
              " and no corresponding column named '" + fileColName + "'");
        } else {
          mapper.setFileColumnIndex(colId, fileColIndex);
        }
      }
    }
  }

  @Override
  public void fillRow(RowHandler handler) throws SQLException, IOException {
    Row row = rowIterator.next();
    int totalColumns = mapper.numColumns();

    for (int colId = 0; colId < totalColumns; colId++) {
      int fileColIndex = mapper.getFileColumnIndex(colId);
      if (fileColIndex >= 0) {
        Cell cell = row.getCell(fileColIndex);

        // if cell is null
        if (cell == null) {
          handler.setValue(colId, (String) null);
        } else {

          // if string value
          if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            handler.setValue(colId, cell.getStringCellValue());

            // if numeric
          } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

            // possibly date
            if (handler.getSQLType(colId) == SQLType.Date) {
              handler.setValue(colId, cell.getDateCellValue());

              // just regular numeric
            } else {
              handler.setValue(colId, String.valueOf(cell.getNumericCellValue()));
            }
          }
        }
      }
    }
  }

  @Override
  public boolean hasNextRow() {
    return rowIterator.hasNext();
  }
}
