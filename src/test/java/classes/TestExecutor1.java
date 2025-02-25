package classes;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestExecutor1 {
    WebDriver driver;
    String excelPath = "E:\\testData.xlsx"; 
    String baseFolder = "E:/screenshots of navyojana test/";
    String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    String reportPath = baseFolder + timestamp + "/SparkReport.html";
    String screenshotFolder = baseFolder + timestamp + "/screenshots/";
    String testSummary="";

    ExtentReports extent;
    ExtentTest test;
    EmailService emailService = new EmailService(); 

    int passedTests = 0;
    int failedTests = 0;

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://qa-navyojna.epps-erp.in/");

        // Initialize Extent Reports
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Automation Test Report");
        sparkReporter.config().setReportName("Test Execution Results");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Tester", "Automation QA");

        // Create screenshot directory
        try {
            Files.createDirectories(Paths.get(screenshotFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("🚀 Test Execution Started...");
    }

    @Test
    public void executeTestSteps() {
        ExcelReader excelReader = new ExcelReader(excelPath);
        List<TestStep> testSteps = excelReader.readTestSteps();

        for (TestStep step : testSteps) {
            test = extent.createTest("Step: " + step.getStepName());
            System.out.println("🔹 Executing Step: " + step.getStepName());

            boolean stepResult = executeStep(step);

            if (stepResult) {
                passedTests++;
            } else {
                failedTests++;
            }
        }
    }

    public boolean executeStep(TestStep step) {
        try {
            Thread.sleep((long) (step.getWaitTime() * 1000));
            WebElement element = driver.findElement(By.xpath(step.getXpath()));

            switch (step.getAction().toLowerCase()) {
                case "open":
                    driver.get(step.getXpath());
                    test.pass("Opened URL: " + step.getXpath());
                    break;
                case "click":
                    element.click();
                    test.pass("Clicked on element: " + step.getXpath());
                    break;
                case "enter":
                    element.sendKeys(step.getData());
                    test.pass("Entered data: " + step.getData());
                    break;
                default:
                    test.warning("Unknown action: " + step.getAction());
            }
            return true;
        } catch (Exception e) {
            String screenshotPath = takeScreenshot(step.getStepName());
            test.fail("Error executing step: " + step.getAction() + " on " + step.getXpath())
                .fail(e.getMessage())
                .addScreenCaptureFromPath(screenshotPath);
            return false;
        }
    }

    public String takeScreenshot(String stepName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filePath = screenshotFolder + stepName.replace(" ", "_") + "_" + System.currentTimeMillis() + ".png";
            File destFile = new File(filePath);
            Files.copy(srcFile.toPath(), destFile.toPath());
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @AfterTest
    public void tearDown() {
        System.out.println("🔻 Cleaning up resources...");

        if (driver != null) {
            driver.quit();
        }

        extent.flush();
        System.out.println("📄 Extent report generated: " + reportPath);

        // Zip Screenshots
        String zipFilePath = baseFolder + timestamp + "/screenshots.zip";
        zipScreenshots(screenshotFolder, zipFilePath);

        // Open Extent Report in Browser
        try {
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                Desktop.getDesktop().browse(reportFile.toURI());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send Email with Report Attachment
        String summaryMessage = "Test Summary: " + passedTests + " PASSED out of " + (passedTests + failedTests);
        emailService.sendEmailWithAttachment(
            "rishita.kolhe@epps-erp.in",
            "Automation Test Report",
            reportPath,
            zipFilePath,
            summaryMessage
        );

        System.exit(0);
    }

    public void zipScreenshots(String folderPath, String zipFilePath) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            File folder = new File(folderPath);
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);
                        Files.copy(file.toPath(), zos);
                        zos.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
