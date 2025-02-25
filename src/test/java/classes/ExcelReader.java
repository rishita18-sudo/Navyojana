package classes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    private String excelPath;

    public ExcelReader(String excelPath) {
        this.excelPath = excelPath;
    }

    public List<TestStep> readTestSteps() {
        List<TestStep> testSteps = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(excelPath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                if (isRowEmpty(row)) {
                    continue; // Skip empty rows
                }

                String stepName = getCellValue(row.getCell(0)); // Step Name
                String action = getCellValue(row.getCell(1));
                String xpath = getCellValue(row.getCell(2));
                String data = getCellValue(row.getCell(3));
                double waitTime = (row.getCell(4) != null) ? row.getCell(4).getNumericCellValue() : 0;

                testSteps.add(new TestStep(stepName, action, xpath, data, waitTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testSteps;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
