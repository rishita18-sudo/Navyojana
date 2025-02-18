package classes;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;
import org.testng.Assert;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;

public class TestDataDriven {
    ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    ExcelUtils excel;
    private static final Logger logger = LogManager.getLogger(TestDataDriven.class);

    @Parameters({"browser"}) // Get browser type from TestNG XML
    @BeforeClass
    public void setUp(@Optional("chrome") String browser) { // Default to Chrome if not specified
        excel = new ExcelUtils("E:\\testData.xlsx", "Sheet1");
        initializeDriver(browser);
    }

    public void initializeDriver(String browser) {
        if (browser.equalsIgnoreCase("chrome")) {
            WebDriverManager.chromedriver().driverVersion("133.0.6943.59").setup(); // Set exact driver version
            driver.set(new ChromeDriver());
            logger.info("[INFO] Chrome browser (v133.0.6943.59) initialized.");
        } else if (browser.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            driver.set(new EdgeDriver());
            logger.info("[INFO] Edge browser initialized.");
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().manage().window().maximize();
        getDriver().get("https://qa-navyojna.epps-erp.in/");
        logger.info("[INFO] Browser launched: " + browser);
    }

    public WebDriver getDriver() {
        return driver.get();
    }

    @Test
    public void executeTestSteps() {
        List<String[]> steps = excel.getTestSteps();

        for (String[] step : steps) {
            String xpath = step[0];
            String action = step[1];
            String data = step[2];
            String waitTimeStr = step[3];
            String assertType = step.length > 4 ? step[4] : "";  
            String expectedValue = step.length > 5 ? step[5] : "";

            int waitTime = 0;
            try {
                waitTime = Integer.parseInt(waitTimeStr.trim());
            } catch (NumberFormatException e) {
                logger.warn("[WARN] Invalid wait time for step: " + xpath);
            }

            try {
                if (waitTime > 0) {
                    logger.info("[INFO] Waiting for " + waitTime + " seconds before interacting with element: " + xpath);
                    Thread.sleep(waitTime * 1000);
                }

                WebElement element = getDriver().findElement(By.xpath(xpath));
                logger.debug("[DEBUG] Action on element " + xpath + ": " + action);

                switch (action.toLowerCase()) {
                    case "sendkeys":
                        element.sendKeys(data);
                        logger.debug("[DEBUG] Sent keys '" + data + "' to element: " + xpath);
                        break;
                    case "click":
                        element.click();
                        logger.debug("[DEBUG] Clicked element: " + xpath);
                        break;
                    case "select":
                        new Select(element).selectByVisibleText(data);
                        logger.debug("[DEBUG] Selected '" + data + "' from dropdown: " + xpath);
                        break;
                    case "gettext":
                        String text = element.getText();
                        logger.debug("[DEBUG] Extracted text: '" + text + "' from element: " + xpath);
                        break;
                    default:
                        logger.error("[ERROR] Invalid action: " + action);
                        break;
                }

                if (!assertType.isEmpty()) {
                    performAssertion(xpath, assertType, expectedValue);
                }

            } catch (Exception e) {
                logger.error("[ERROR] Failed to execute step for element: " + xpath, e);
                Assert.fail("Test failed due to an exception for element: " + xpath);
            }
        }
    }

    public void performAssertion(String xpath, String assertType, String expectedValue) {
        try {
            WebElement element = getDriver().findElement(By.xpath(xpath));

            switch (assertType.toLowerCase()) {
                case "ispresent":
                    Assert.assertTrue(element.isDisplayed(), "Element not present: " + xpath);
                    logger.info("[ASSERTION PASSED] Element is present: " + xpath);
                    break;
                case "equals":
                    String actualText = element.getText().trim();
                    Assert.assertEquals(actualText, expectedValue, "Text does not match for: " + xpath);
                    logger.info("[ASSERTION PASSED] Text matches for element: " + xpath);
                    break;
                case "contains":
                    actualText = element.getText().trim();
                    Assert.assertTrue(actualText.contains(expectedValue), "Text does not contain expected value for: " + xpath);
                    logger.info("[ASSERTION PASSED] Text contains expected value for element: " + xpath);
                    break;
                default:
                    logger.warn("[WARN] Invalid assertion type: " + assertType);
                    break;
            }
        } catch (Exception e) {
            logger.error("[ASSERTION FAILED] Error performing assertion for element: " + xpath, e);
            Assert.fail("Assertion failed for element: " + xpath);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            logger.info("[INFO] Browser closed.");
        }
    }
}
