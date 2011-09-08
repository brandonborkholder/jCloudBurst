package anon;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.mappingconfig.ConfigurationType;

import anon.RowHandler.SQLType;

public class ExcelFileSource implements ImportDataSource {
  protected Sheet activeSheet;

  protected List<String> columnNames;

  protected Iterator<Row> rowIterator;

  public ExcelFileSource(ConfigurationType config) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook(config.getFile());
    activeSheet = workbook.getSheet(config.getExcelSheet());
    rowIterator = activeSheet.iterator();
    columnNames = getColumnNames(config);
  }

  protected List<String> getColumnNames(ConfigurationType config) {
    List<String> names = new ArrayList<String>();

    Row row = rowIterator.next();
    int lastCell = row.getLastCellNum();
    for (int i = 0; i < lastCell; i++) {
      Cell cell = row.getCell(i);
      if (cell == null) {
        names.add(null);
      } else {
        names.add(cell.getStringCellValue());
      }
    }

    return names;
  }

  protected String getName(int index) {
    return columnNames.get(index);
  }

  @Override
  public void fillRow(RowHandler handler) throws SQLException, IOException, ParseException {
    Row row = rowIterator.next();

    int lastCell = row.getLastCellNum();
    for (int i = 0; i < lastCell; i++) {
      Cell cell = row.getCell(i);
      String colName = getName(i);
      if (handler.doSkipFileColumn(colName)) {
        continue;
      }

      if (cell == null) {
        handler.setValue(colName, (String) null);
      } else {
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
          handler.setValue(colName, cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
          if (handler.getSQLType(colName) == SQLType.Date) {
            handler.setValue(colName, cell.getDateCellValue());
          } else {
            handler.setValue(colName, String.valueOf(cell.getNumericCellValue()));
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
