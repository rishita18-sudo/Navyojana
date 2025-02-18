package classes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    private Workbook workbook;
    private Sheet sheet;

    // Constructor to load Excel file and sheet dynamically
    public ExcelUtils(String filePath, String sheetName) {
        try {
            FileInputStream file = new FileInputStream(new File(filePath));
            workbook = new XSSFWorkbook(file);
            sheet = workbook.getSheet(sheetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get row count dynamically (excluding empty rows)
    public int getRowCount() {
        int rowCount = sheet.getPhysicalNumberOfRows();
        int actualRows = 0;

        for (int i = 1; i < rowCount; i++) { // Skip header row
            Row row = sheet.getRow(i);
            if (row != null && row.getCell(0) != null && !row.getCell(0).toString().trim().isEmpty()) {
                actualRows++;
            }
        }
        return actualRows;
    }

    // Get cell data with type handling
    public String getCellData(int rowNum, int colNum) {
        try {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                Cell cell = row.getCell(colNum);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case STRING:
                            return cell.getStringCellValue().trim();
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                return cell.getDateCellValue().toString();
                            } else {
                                return String.valueOf((long) cell.getNumericCellValue()); // Convert to long
                            }
                        case BOOLEAN:
                            return String.valueOf(cell.getBooleanCellValue());
                        case FORMULA:
                            return cell.getCellFormula();
                        case BLANK:
                            return "";
                        default:
                            return "";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Fetch all dynamic test steps
 // Add this method inside ExcelUtils.java
    public List<String[]> getTestSteps() {
        List<String[]> steps = new ArrayList<>();
        int rowCount = getRowCount();

        if (rowCount == 0) {
            System.out.println("No data found in the Excel sheet.");
            return steps; // Return empty list to prevent null pointer exception
        }

        for (int i = 1; i <= rowCount; i++) { // Start from row 1 (skip header)
            String xpath = getCellData(i, 0);
            String action = getCellData(i, 1);
            String data = getCellData(i, 2);
            String waitTime = getCellData(i, 3);

            // Skip empty rows to prevent errors
            if (xpath == null || xpath.isEmpty() || action == null || action.isEmpty()) {
                continue;
            }

            steps.add(new String[]{xpath, action, data, waitTime});
        }
        return steps;
    }
}