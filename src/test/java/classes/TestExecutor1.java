package classes;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestExecutor1 {
    WebDriver driver;
    String excelPath = "E:/testData.xlsx";
    String baseFolder = "E:/screenshots of navyojana test/";
    String reportPath;
    String screenshotFolder;
    String zipFilePath;
    ExtentReports extent;
    ExtentTest test;
    EmailService emailService = new EmailService();

    @BeforeTest
    public void setUp() throws AWTException, IOException, Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://dev-navyojna.epps-erp.in/");

        String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
        screenshotFolder = baseFolder + timestamp + "/screenshots/";
        reportPath = baseFolder + timestamp + "/TestReport.html";
        zipFilePath = baseFolder + timestamp + "/TestResults.zip";

        Files.createDirectories(Paths.get(screenshotFolder));

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Tester", "Automation QA");

        ScreenRecorderUtil.startRecording("TestExecution");
        System.out.println("🚀 Test Execution Started...");
    }

    @Test
    public void executeTestSteps() {
        ExcelReader excelReader = new ExcelReader(excelPath);
        List<TestStep> testSteps = excelReader.readTestSteps();

        if (testSteps == null || testSteps.isEmpty()) {
            test = extent.createTest("No Test Steps Found");
            test.fail("❌ No test steps were found in the Excel file.");
            return;
        }

        for (TestStep step : testSteps) {
            test = extent.createTest(step.getStepName());
            boolean result = executeStep(step);
            if (!result) {
                String screenshotPath = takeScreenshot(step.getStepName());
                test.fail("Step Failed").addScreenCaptureFromPath(screenshotPath);
            }
        }
    }

    public boolean executeStep(TestStep step) {
        try {
            double waitTimeInSeconds = step.getWaitTime(); // Read from Excel (already in seconds)

            if (waitTimeInSeconds > 0) {
                long waitTimeInMillis = (long) (waitTimeInSeconds * 1000); // Convert to milliseconds
                System.out.println("⏳ Waiting for " + waitTimeInSeconds + " seconds before executing: " + step.getStepName());
                Thread.sleep(waitTimeInMillis);
                test.info("⏳ Waited for " + waitTimeInSeconds + " seconds before executing: " + step.getStepName());
            }

            WebElement element = driver.findElement(By.xpath(step.getXpath()));
         
            switch (step.getAction().toLowerCase()) {
                case "click":
                    element.click();
                    test.pass("✅ Clicked: " + step.getXpath());
                    System.out.println("✅ Clicked: " + step.getXpath());
                    break;

                case "enter":
                    element.sendKeys(step.getData());
                    test.pass("✅ Entered: " + step.getData());
                    System.out.println("✅ Entered: " + step.getData());
                    break;

                default:
                    test.warning("⚠ Unknown action: " + step.getAction());
                    System.out.println("⚠ Unknown action: " + step.getAction());
            }
            return true;
           
        } catch (Exception e) {
            String screenshotPath = takeScreenshot(step.getStepName());
            test.fail("❌ Error: " + e.getMessage()).addScreenCaptureFromPath(screenshotPath);
            System.out.println("❌ Error in step: " + step.getStepName() + " - " + e.getMessage());
            return false;
        }
    }



    public String takeScreenshot(String stepName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filePath = screenshotFolder + stepName.replace(" ", "_") + ".png";
            FileUtils.copyFile(srcFile, new File(filePath));
            return filePath;
        } catch (IOException e) {
            return "";
        }
    }
    @AfterTest
    public void tearDown() throws IOException, Exception {
        ScreenRecorderUtil.stopRecording();
        driver.quit();
        extent.flush();
        zipTestResults();
        emailService.sendEmailWithAttachment("niranjan.mogare@epps-erp.in", "Automation Report", reportPath, zipFilePath, 587, 3);



      
    }

    public void zipTestResults() {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            File folder = new File(screenshotFolder);
            if (folder.exists() && folder.listFiles() != null) {
                for (File file : folder.listFiles()) {
                    if (file.isFile()) {
                        zos.putNextEntry(new ZipEntry(file.getName()));
                        Files.copy(file.toPath(), zos);
                        zos.closeEntry();
                    }
                }
            }
            // Add report file to ZIP
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                zos.putNextEntry(new ZipEntry(reportFile.getName()));
                Files.copy(reportFile.toPath(), zos);
                zos.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
