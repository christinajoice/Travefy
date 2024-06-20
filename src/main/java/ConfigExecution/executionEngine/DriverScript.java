package ConfigExecution.executionEngine;

import static ConfigExecution.utility.Constants.*;
import static ConfigExecution.utility.ExcelUtils.*;
import static java.lang.Integer.parseInt;

import TestCaseExecution.ReusableLibrary.ReusableLibrary;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.model.Media;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import java.io.*;
import java.lang.reflect.Method;
import java.time.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DriverScript extends Thread {
  boolean chrome, firefox, safari, edge, conditionStep, conditionCase;
  public static List<GalenTestInfo> galenTest = new LinkedList<>();
  public static String teamName;
  public static Duration waitTime;
  String toMail, ccMail;
  public static boolean Headless,
      runGalen,
      runLightHouse,
      runAxe,
      circleci,
      chrome_bResult,
      firefox_bResult,
      safari_bResult,
      edge_bResult;
  public static ExtentTest chromeTest, firefoxTest, safariTest, edgeTest;
  BufferedWriter writer;
  Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @BeforeTest
  public void BeforeActions() {
    try {
      // Value to be get from Excel sheet ...
      setExcelFile(Path_TestData);
      File mailFile = new File(MailDataPath);
      if (!mailFile.exists()) mailFile.createNewFile();
      updateConfigSettings();
      logger.info(
          "\nChrome Enabled - "
              + chrome
              + "\nFirefox Enabled - "
              + firefox
              + "\nSafari Enabled - "
              + safari
              + "\nEdge Enabled - "
              + edge
              + "\nHeadless Enabled - "
              + Headless
              + "\nGalen Enabled - "
              + runGalen
              + "\nLightHouse Enabled - "
              + runLightHouse
              + "\nAxe Enabled - "
              + runAxe
              + "\n");
      writer = new BufferedWriter(new FileWriter(MailDataPath));
      writer.write("Team=" + teamName + "\n");
    } catch (Exception e) {
      logger.error("In Before Actions", e);
    }
  }

  @Test // Chrome Browser Thread starts ...
  public void ChromeBrowser() {
    if (chrome) startExecution("chrome");
  }

  @Test // Firefox Browser Thread starts ...
  public void FirefoxBrowser() {
    if (firefox) startExecution("firefox");
  }

  @Test // Safari Browser Thread starts ...
  public void SafariBrowser() {
    if (safari) startExecution("safari");
  }

  @Test // Edge Browser Thread starts ...
  public void EdgeBrowser() {
    if (edge) startExecution("edge");
  }

  @AfterMethod
  private void tearDown(ITestResult result) {
    try {
      if (runGalen) {
        HtmlReportBuilder htmlReportBuilder = new HtmlReportBuilder();
        htmlReportBuilder.build(galenTest, "Reports/galenReport");
      }
      writer.flush();
    } catch (Throwable e) {
      logger.error("In tearDown ", e);
    }
  }

  // Step-1 = Initiate Browser and Extract each Testcase from Datasheet ...
  private void startExecution(String browser) {

    BaseAction actionKeywords = new BaseAction();
    int passCount = 0, failCount = 0;
    String sTestCaseID, sTestCaseDescription, sRunMode, precondition, post_condition, view;
    boolean flag = true;
    ExtentTest Test;
    try {
      // Check the current browser ...
      ExtentReports currentReport = initiateExtentSparkReporter(browser);
      int iTotalTestCases = getRowCount(Sheet_TestCases);

      for (int iTestcase = 1; iTestcase < iTotalTestCases; iTestcase++) {
        boolean preResult = true, tResult, postResult = true;
        sRunMode = getCellData(iTestcase, Col_RunMode, Sheet_TestCases);

        if (sRunMode.equals("Yes")) {
          if (iTestcase > 1 && conditionCase) {
            if (getCellData(iTestcase - 1, Col_Result, Sheet_TestCases).equalsIgnoreCase("Fail")) {
              break;
            }
          }

          sTestCaseID = getCellData(iTestcase, Col_TestCaseID, Sheet_TestCases);
          sTestCaseDescription = getCellData(iTestcase, Col_TestCaseDescription, Sheet_TestCases);
          precondition = getCellData(iTestcase, Col_Precondition, Sheet_TestCases);
          post_condition = getCellData(iTestcase, Col_PostCondition, Sheet_TestCases);
          view = getCellData(iTestcase, Col_View, Sheet_TestCases);

          actionKeywords = actionKeywords.getActionKeyword(teamName, view, browser);

          if (flag) {
            currentReport.setSystemInfo(browser, actionKeywords.browserVersion);
            flag = false;
          }
          Test =
              currentReport
                  .createTest(sTestCaseID)
                  .createNode(sTestCaseID + " - " + sTestCaseDescription + " - " + browser);
          logger.info(browser + " execution for : " + sTestCaseID);
          if (circleci) System.out.println("TESTCASE ID : " + sTestCaseID);

          if (precondition != null && !precondition.isEmpty()) {
            preResult = executeTestCase(precondition, actionKeywords, browser, iTestcase, Test);
          }
          tResult = executeTestCase(sTestCaseID, actionKeywords, browser, iTestcase, Test);
          if (post_condition != null && !post_condition.isEmpty()) {
            postResult = executeTestCase(post_condition, actionKeywords, browser, iTestcase, Test);
          }
          if (preResult && tResult && postResult) passCount++;
          else failCount++;

          // Closing the browser at the end of each test case even if it passes or fail
          actionKeywords.quitBrowser();
          currentReport.flush();
        }
      }
      writer.write(browser + "Passed=" + passCount + "\n");
      writer.write(browser + "Failed=" + failCount + "\n");
      writer.write(browser + "Total=" + (passCount + failCount) + "\n");
    } catch (Exception e) {
      logger.error("In startExecution", e);
    }
  }

  // Step-2 = Used to call methods from ActionKeywords and update the Status as true or false ...
  private boolean executeTestCase(
      String precondition,
      ReusableLibrary actionKeyword,
      String browser,
      int iTestcase,
      ExtentTest Test) {
    int iTestStep, iTestLastStep;
    String TestScenarioID, TestStepDescription, sActionKeyword;
    Method[] method = actionKeyword.getClass().getMethods();

    iTestStep = getRowContains(precondition, Col_TestCaseID, Sheet_TestSteps);
    iTestLastStep = getTestStepsCount(Sheet_TestSteps, precondition, iTestStep);
    boolean flagResult = true, result = false;

    // Execute each steps with ActionKeyword methods
    for (; iTestStep < iTestLastStep; iTestStep++) {
      updateResultPass(browser);
      sActionKeyword = getCellData(iTestStep, Col_ActionKeyword, Sheet_TestSteps);
      TestScenarioID = getCellData(iTestStep, Col_TestScenarioID, Sheet_TestSteps);
      TestStepDescription = getCellData(iTestStep, Col_TestStepDescription, Sheet_TestSteps);
      Exception exp = null;

      updateBrowserTest(browser, Test.createNode(TestScenarioID + " - " + TestStepDescription));

      for (Method value : method) {
        if (value.getName().equals(sActionKeyword)) {
          try {
            logger.info(browser + " execution : " + TestScenarioID + " - " + sActionKeyword);
            if (circleci)
              System.out.println(
                  "TEST STEP : " + TestScenarioID + " - SUMMARY : " + sActionKeyword);
            value.invoke(actionKeyword, browser);
          } catch (Exception e) {
            exp = e;
            logger.error("In Test_ID - " + TestScenarioID, e);
            updateResultFail(browser);
          }
          // Update the Execution Status as Pass or Fail ...
          result = getTestResult(browser);
          if (result) {
            setCellData("PASS", iTestcase, Col_Result, Sheet_TestCases);
            updatePassLog(browser, "* Execution Completed successfully - " + TestStepDescription);
            break;
          } else {
            setCellData("FAIL", iTestcase, Col_Result, Sheet_TestCases);
            updateFailLog(browser, TestStepDescription, exp, actionKeyword);
            flagResult = false;
          }
        }
      }
      if (!result && conditionStep) break;
    }
    return flagResult;
  }

  private Media capture(WebDriver driver) {
    String scrFile;
    try {
      scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
      return MediaEntityBuilder.createScreenCaptureFromBase64String(
              "data:image/png;base64," + scrFile)
          .build();
    } catch (WebDriverException e) {
      Logger logger = LoggerFactory.getLogger("DriverScript-Screenshot");
      logger.error("In capture - ", e);
      return null;
    }
  }

  // Update ExtentSparkReporter path for all Browsers in specific ...
  private ExtentReports initiateExtentSparkReporter(String browser) throws IOException {
    ExtentSparkReporter currentReporter =
        new ExtentSparkReporter("./Reports/" + browser + "Automation.html");
    currentReporter.loadXMLConfig(new File(rootPath + "/extent-config.xml"));
    ExtentReports Report = new ExtentReports();
    Report.attachReporter(currentReporter);
    Report.setSystemInfo("Team Key", teamName);
    Report.setSystemInfo("URL", Test_URL);
    Report.setSystemInfo("Version", version);
    return Report;
  }

  private void updateConfigSettings() throws IOException {
    Properties p = new Properties();
    teamName = getCellData(TeamName, RunValue, TeamConfig);
    if (teamName.isEmpty()) {
      logger.error("Team Key doesn't Exist. Please check the DataEngine file and Update it.");
      System.exit(0);
    }
    p.load(
        new FileReader(
            rootPath
                + "/src/main/resources/FrameworkProperties/Framework.properties"));
    conditionCase = p.getProperty("StopOnFail_TestCase").equalsIgnoreCase("StopOnFail");
    conditionStep = p.getProperty("StopOnFail_TestStep").equalsIgnoreCase("StopOnFail");
    waitTime = Duration.ofSeconds(Long.parseLong(p.getProperty("waitTimeInSeconds")));
    toMail = getCellData(toMailID, RunValue, TeamConfig);
    ccMail = getCellData(ccMailID, RunValue, TeamConfig);
    Headless = getCellData(runHeadless, RunValue, TeamConfig).equals("Yes");
    circleci = rootPath.contains("circleci") || rootPath.contains("distiller");
    String OS = System.getProperty("os.name").toLowerCase();
    chrome = getCellData(ChromeBrowser, RunValue, TeamConfig).equals("Yes");
    firefox =
        OS.contains("linux") && getCellData(FirefoxBrowser, RunValue, TeamConfig).equals("Yes");
    safari = OS.contains("mac") && getCellData(SafariBrowser, RunValue, TeamConfig).equals("Yes");
    edge = OS.contains("linux") && getCellData(EdgeBrowser, RunValue, TeamConfig).equals("Yes");
    runGalen = checkCron(getCellData(GalenTest, RunValue, TeamConfig), p.getProperty("Galen"));
    runAxe = checkCron(getCellData(AxeTest, RunValue, TeamConfig), p.getProperty("Axe"));
    runLightHouse = false;
    // runLightHouse = checkCron(getCellData(LightHouseTest, RunValue, TeamConfig),
    FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/target/java-a11y"));
    FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/Reports/galenReport"));
  }

  public boolean checkCron(String cellData, String dayValue) {
    boolean isTodayWeek, isTodayMonth, run = false;
    String[] value;
    if (cellData.contains(",")) {
      value = cellData.split(",");
      if (value[0].equalsIgnoreCase("Yes")) {
        LocalDate currentDate = LocalDate.now();
        isTodayWeek =
            value[1].equalsIgnoreCase("Weekly")
                && String.valueOf(currentDate.getDayOfWeek()).equalsIgnoreCase(dayValue);
        isTodayMonth =
            value[1].equalsIgnoreCase("Monthly")
                && currentDate.getDayOfMonth() == parseInt(dayValue);
        if ((isTodayWeek || isTodayMonth)) return true;
      }
    } else if (cellData.equalsIgnoreCase("Yes")) return true;
    return run;
  }

  // Updating Log In Browser Reports ...
  private void updateBrowserTest(String browser, ExtentTest Test) {
    if (browser.equalsIgnoreCase("chrome")) chromeTest = Test;
    else if (browser.equalsIgnoreCase("firefox")) firefoxTest = Test;
    else if (browser.equalsIgnoreCase("safari")) safariTest = Test;
    else if (browser.equalsIgnoreCase("edge")) edgeTest = Test;
  }

  public static void updatePassLog(String browser, String message) {
    if (browser.equalsIgnoreCase("chrome")) chromeTest.pass(message);
    else if (browser.equalsIgnoreCase("firefox")) firefoxTest.pass(message);
    else if (browser.equalsIgnoreCase("safari")) safariTest.pass(message);
    else if (browser.equalsIgnoreCase("edge")) edgeTest.pass(message);
  }

  public static void updateInfoLog(String browser, String message) {
    if (browser.equalsIgnoreCase("chrome")) chromeTest.info(message);
    else if (browser.equalsIgnoreCase("firefox")) firefoxTest.info(message);
    else if (browser.equalsIgnoreCase("safari")) safariTest.info(message);
    else if (browser.equalsIgnoreCase("edge")) edgeTest.info(message);
  }

  private void updateFailLog(
      String browser, String message, Exception exp, ReusableLibrary actionKeyword) {
    if (browser.equalsIgnoreCase("chrome")) {
      if (exp != null) chromeTest.fail(exp, capture(actionKeyword.driver));
      else chromeTest.fail(message, capture(actionKeyword.driver));
    } else if (browser.equalsIgnoreCase("firefox")) {
      if (exp != null) firefoxTest.fail(exp, capture(actionKeyword.driver));
      else firefoxTest.fail(message, capture(actionKeyword.driver));
    } else if (browser.equalsIgnoreCase("safari")) {
      if (exp != null) safariTest.fail(exp, capture(actionKeyword.driver));
      else safariTest.fail(message, capture(actionKeyword.driver));
    } else if (browser.equalsIgnoreCase("edge")) {
      if (exp != null) edgeTest.fail(exp, capture(actionKeyword.driver));
      else edgeTest.fail(message, capture(actionKeyword.driver));
    }
  }

  public static void updateResultPass(String browser) {
    if (browser.equalsIgnoreCase("chrome")) chrome_bResult = true;
    else if (browser.equalsIgnoreCase("firefox")) firefox_bResult = true;
    else if (browser.equalsIgnoreCase("safari")) safari_bResult = true;
    else if (browser.equalsIgnoreCase("edge")) edge_bResult = true;
  }

  public static void updateResultFail(String browser) {
    if (browser.equalsIgnoreCase("chrome")) chrome_bResult = false;
    else if (browser.equalsIgnoreCase("firefox")) firefox_bResult = false;
    else if (browser.equalsIgnoreCase("safari")) safari_bResult = false;
    else if (browser.equalsIgnoreCase("edge")) edge_bResult = false;
  }

  public static boolean getTestResult(String browser) {
    if (browser.equalsIgnoreCase("chrome")) return chrome_bResult;
    else if (browser.equalsIgnoreCase("firefox")) return firefox_bResult;
    else if (browser.equalsIgnoreCase("safari")) return safari_bResult;
    else if (browser.equalsIgnoreCase("edge")) return edge_bResult;
    else {
      System.out.println("In getTestResult - No Browser found");
      return false;
    }
  }
}
