package classes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestExecutor {
    WebDriver driver;
    String excelPath = "E:\\testData.xlsx"; // Ensure the Excel file exists

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup(); // Setup ChromeDriver
        driver = new ChromeDriver();
        driver.get("https://qa-navyojna.epps-erp.in/");
    }

    @Test
    public void executeTestSteps() {
        try {
            FileInputStream fis = new FileInputStream(new File(excelPath));
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                String action = getCellValue(row.getCell(1)); // Read Action
                String xpath = getCellValue(row.getCell(2)); // Read XPath
                String data = getCellValue(row.getCell(3));  // Read Data
                double waitTime = (row.getCell(4) != null) ? row.getCell(4).getNumericCellValue() : 0; // Read Wait Time

                executeStep(action, xpath, data, waitTime);
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeStep(String action, String xpath, String data, double waitTime) {
        try {
            // Convert seconds to milliseconds and apply wait
            Thread.sleep((long) (waitTime * 1000)); 

            WebElement element = driver.findElement(By.xpath(xpath));
            switch (action.toLowerCase()) {
                case "open":
                    driver.get(xpath);
                    break;
                case "click":
                    element.click();
                    break;
                case "enter":
                    element.sendKeys(data);
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Wait interrupted before executing: " + action);
        } catch (Exception e) {
            System.out.println("Error executing step: " + action + " on " + xpath);
            e.printStackTrace();
        }
    }

    // Method to handle different cell types
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return ""; // Return empty string if cell is empty
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue()); // Convert numeric to string
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    @AfterTest
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
