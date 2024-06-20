package ConfigExecution.utility;

import static ConfigExecution.utility.Constants.Col_TestCaseID;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
  private static XSSFSheet ExcelWSheet;
  private static XSSFWorkbook ExcelWBook;
  private static XSSFCell Cell;
  private static XSSFRow Row;

  public static void setExcelFile(String Path) {
    try {
      FileInputStream ExcelFile = new FileInputStream(Path);
      ExcelWBook = new XSSFWorkbook(ExcelFile);
    } catch (Exception e) {
      System.out.println("ERROR - setExcelFile\n" + e);
    }
  }

  public static String getCellData(int RowNum, int ColNum, String SheetName) {
    try {
      ExcelWSheet = ExcelWBook.getSheet(SheetName);
      Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum);
      String CellData = Cell.getStringCellValue();
      return CellData;
    } catch (Exception e) {
      if (!String.valueOf(e)
          .contains("\"org.apache.poi.xssf.usermodel.XSSFSheet.getRow(int)\" is null"))
        System.out.println("ERROR - getCellData\n" + e);
      return "";
    }
  }

  public static int getRowCount(String SheetName) {
    int iNumber = 0;
    try {
      ExcelWSheet = ExcelWBook.getSheet(SheetName);
      iNumber = ExcelWSheet.getLastRowNum() + 1;
    } catch (Exception e) {
      System.out.println("ERROR - getRowCount\n" + e);
    }
    return iNumber;
  }

  public static int getRowContains(String sTestCaseName, int colNum, String SheetName) {
    int iRowNum = 0;
    try {
      int rowCount = ExcelUtils.getRowCount(SheetName);
      for (; iRowNum < rowCount; iRowNum++) {
        if (ExcelUtils.getCellData(iRowNum, colNum, SheetName).equalsIgnoreCase(sTestCaseName)) {
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("ERROR - getRowContains\n" + e);
    }
    return iRowNum;
  }

  public static int getTestStepsCount(String SheetName, String sTestCaseID, int iTestCaseStart) {
    try {
      for (int i = iTestCaseStart; i <= ExcelUtils.getRowCount(SheetName); i++) {
        if (!sTestCaseID.equals(ExcelUtils.getCellData(i, Col_TestCaseID, SheetName))) {
          int number = i;
          return number;
        }
      }
      ExcelWSheet = ExcelWBook.getSheet(SheetName);
      int number = ExcelWSheet.getLastRowNum() + 1;
      return number;
    } catch (Exception e) {
      System.out.println("ERROR - getTestStepsCount\n" + e);
      return 0;
    }
  }

  public static void setCellData(String Result, int RowNum, int ColNum, String SheetName) {
    try {
      ExcelWSheet = ExcelWBook.getSheet(SheetName);
      Row = ExcelWSheet.getRow(RowNum);
      Cell =
          Row.getCell(
              ColNum, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
      if (Cell == null) {
        Cell = Row.createCell(ColNum);
        Cell.setCellValue(Result);
      } else {
        Cell.setCellValue(Result);
      }
      FileOutputStream fileOut = new FileOutputStream(Constants.Path_TestData);
      ExcelWBook.write(fileOut);
      fileOut.close();
      ExcelWBook = new XSSFWorkbook(new FileInputStream(Constants.Path_TestData));
    } catch (Exception e) {
      System.out.println("ERROR - setCellData\n" + e);
    }
  }
}
