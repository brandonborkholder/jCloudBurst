package com.github.jcloudburst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFileSource implements SourceReader {
  protected Sheet activeSheet;

  protected Iterator<Row> rowIterator;

  private List<String> header;

  private Row current;

  public ExcelFileSource(ExcelSource source) throws IOException {
    File workbookFile = new File(source.getFile());
    String worksheet = source.getExcelSheet();

    XSSFWorkbook workbook = new XSSFWorkbook(workbookFile.getAbsolutePath());
    activeSheet = workbook.getSheet(worksheet);
    rowIterator = activeSheet.iterator();

    header = null;
    if (source.isHasHeaderRow()) {
      header = Collections.unmodifiableList(readHeader());
    }
  }

  @Override
  public void close() throws IOException {
    // nop
  }

  @Override
  public List<String> getHeader() throws IOException {
    return header;
  }

  @Override
  public String getValue(int column) throws IOException {
    Cell cell = current.getCell(column);

    // if cell is null
    if (cell == null) {
      return null;
    } else {

      // if string value
      if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
        return cell.getStringCellValue();
        // if numeric
      } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
        // just regular numeric
        return String.valueOf(cell.getNumericCellValue());
      } else {
        return null;
      }
    }
  }

  @Override
  public boolean next() throws IOException {
    current = null;
    if (rowIterator.hasNext()) {
      current = rowIterator.next();
    }

    return current != null;
  }

  protected List<String> readHeader() throws IOException {
    Row row = rowIterator.next();
    if (row == null) {
      throw new IllegalArgumentException("No header row in excel sheet: " + activeSheet.getSheetName());
    }

    List<String> header = new ArrayList<String>();
    int lastCell = row.getLastCellNum();
    for (int col = 0; col < lastCell; col++) {
      Cell cell = row.getCell(col);
      if (cell == null) {
        header.add(null);
      } else {
        header.add(cell.getStringCellValue());
      }
    }

    return header;
  }
}
