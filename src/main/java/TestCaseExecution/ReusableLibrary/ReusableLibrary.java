package TestCaseExecution.ReusableLibrary;

import static ConfigExecution.executionEngine.DriverScript.*;
import static ConfigExecution.utility.Constants.rootPath;

import ConfigExecution.browserFactory.DriverManager;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Media;
import com.deque.axe.AXE;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.selenium.AxeReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenframework.api.Galen;
import com.galenframework.api.GalenPageDump;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.model.LayoutReport;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.sridharbandi.HtmlCsRunner;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReusableLibrary extends DriverManager {

  public WebDriver driver;
  String value, filename;
  Integer count;

  WebDriverWait waitUntil;
  JavascriptExecutor executor;

  public String mainWindowsHandle;
  int retryCount = 0;
  int maxretry = 5;
  boolean success = false;
  String username, password, credentials, headerValue;
  static Logger logger = LoggerFactory.getLogger(ReusableLibrary.class);

  String browser;
  String os;

  public ReusableLibrary(WebDriver driver) throws IOException {
    this.driver = driver;
    executor = (JavascriptExecutor) driver;
    waitTime = Duration.ofSeconds(Long.parseLong(readPropertyFile(teamName, "waitTimeInSeconds")));
    waitUntil = new WebDriverWait(driver, waitTime);
    Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
    browser = cap.getBrowserName();
    os = String.valueOf(cap.getPlatformName());
    username = Mailcredentials("mail_username");
    password = Mailcredentials("mail_password");
    credentials = username + ":" + password;
    headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public ReusableLibrary() {}

  public void getApplication(String appUrl) throws IOException {
    try {
      driver.get(appUrl);
    } catch (Exception e) {
      System.out.println(e);
      FailScreenshot(e);
    }
  }

  public void getLatestMail(String Searchvalue) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "http://vpn.rentlyprotons.com:8025/api/v1/search?query="
                        + Searchvalue
                        + "&start=0"))
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .header("Authorization", headerValue)
            .build();
    HttpResponse<String> response = null;
    try {
      response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println(response.body());

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> jsonMap = objectMapper.readValue(response.body(), Map.class);
      List<Map<String, Object>> messages = (List<Map<String, Object>>) jsonMap.get("messages");
      Map<String, Object> Email = (Map<String, Object>) messages.get(0);
      String EmailID = (String) Email.get("ID");
      System.out.println(EmailID);
      geturl("http://vpn.rentlyprotons.com:8025/view/" + EmailID + ".html");
      PassScreenshot("Opened Email");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void geturl(String appUrl) throws IOException {
    for (int i = 0; i < maxretry; i++) {
      try {
        driver.get(appUrl);
        success = true;
        break;
      } catch (Exception e) {
        System.out.println(
            "Error occurred during navigation attempt #" + (retryCount + 1) + ": " + e);
        retryCount++;
        if (retryCount == maxretry) {
          System.out.println(e);
          FailScreenshot(e);
        }
      }
    }
  }

  public String Mailcredentials(String name) throws IOException {
    FileReader reader =
        new FileReader(rootPath + "/src/main/resources/FrameworkProperties/Mail.properties");
    Properties p = new Properties();
    p.load(reader);
    return p.getProperty(name);
  }

  public void Pass(String text) {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.pass(text);
    else if (findBrowserName().equalsIgnoreCase("firefox")) firefoxTest.pass(text);
    else if (findBrowserName().equalsIgnoreCase("safari")) safariTest.pass(text);
    else if (findBrowserName().equalsIgnoreCase("edge")) edgeTest.pass(text);
    else printLog("Pass - Browser Not found");
  }

  public void Log(Status status, String text) throws IOException {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.log(status, text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("firefox"))
      firefoxTest.log(status, text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("safari"))
      safariTest.log(status, text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("edge"))
      edgeTest.log(status, text, capture(driver));
    else printLog("Pass - Browser Not found");
  }

  public void infoScreenshot(String text) throws IOException {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.info(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("firefox")) firefoxTest.info(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("safari")) safariTest.info(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("edge")) edgeTest.info(text, capture(driver));
    else printLog("infoScreenshot - Browser Not found");
  }

  public void PassScreenshot(String text) throws IOException {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.pass(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("firefox")) firefoxTest.pass(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("safari")) safariTest.pass(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("edge")) edgeTest.pass(text, capture(driver));
    else printLog("PassScreenshot - Browser Not found");
    updateResultPass(findBrowserName());
  }

  public void FailScreenshot(String text) throws IOException {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("firefox")) firefoxTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("safari")) safariTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("edge")) edgeTest.fail(text, capture(driver));
    else printLog("FailScreenshot -Browser Not found");
    updateResultFail(findBrowserName());
  }

  public void FailScreenshot(Throwable text) throws IOException {
    if (findBrowserName().equalsIgnoreCase("chrome")) chromeTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("firefox")) firefoxTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("safari")) safariTest.fail(text, capture(driver));
    else if (findBrowserName().equalsIgnoreCase("edge")) edgeTest.fail(text, capture(driver));
    else printLog("FailScreenshot -Browser Not found");
    updateResultFail(findBrowserName());
  }

  public void failResult() {
    updateResultFail(findBrowserName());
  }

  public void printLog(String message) {
    System.out.println(message);
    updateInfoLog(findBrowserName(), message);
  }

  public static Media capture(WebDriver driver) throws IOException {
    String scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    return MediaEntityBuilder.createScreenCaptureFromBase64String(
            "data:image/png;base64," + scrFile)
        .build();
  }

  /**
   * Function to embed a custom screenshot to extent report during a test run
   *
   * @param text - for logging purposes
   * @param screenshotPath - the absolute path where the custom modified screenshot is present
   */
  public void embedScreenshot(String text, String screenshotPath) {
    try {
      if (findBrowserName().equalsIgnoreCase("chrome"))
        chromeTest.info(
            text, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
      else if (findBrowserName().equalsIgnoreCase("firefox"))
        firefoxTest.info(
            text, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
      else if (findBrowserName().equalsIgnoreCase("safari"))
        safariTest.info(
            text, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
      else if (findBrowserName().equalsIgnoreCase("edge"))
        edgeTest.info(text, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
      else printLog("PassScreenshot - Browser Not found");
    } catch (Exception e) {
      logger.error("Failed to embed screenshot", e.fillInStackTrace());
    }
  }

  public void EnterText(By strobj, String text) throws IOException {
    try {
      SwitchtoActive();
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      element.sendKeys(text);
      logger.debug("EnterText" + text + " is entered");
    } catch (Exception e) {
      logger.error("EnterText" + text + " is not entered");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void EnterTextTabClear(By strobj, String text) throws IOException {
    System.out.println();
    try {
      SwitchtoActive();
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));

      element.clear();
      element.clear();
      element.sendKeys(text);
      element.sendKeys(Keys.TAB);
      logger.debug("EnterText" + text + " is entered ");
    } catch (Exception e) {
      logger.debug("EnterText" + text + " is not entered", e.getCause());
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void clearText(By strobj, String text) throws IOException {
    try {
      SwitchtoActive();
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(strobj));
      Actions actions = new Actions(driver);
      if (os.equals("mac")) {
        actions
            .click(element)
            .keyDown(Keys.COMMAND)
            .sendKeys("a")
            .keyUp(Keys.COMMAND)
            .sendKeys(Keys.DELETE)
            .perform();
      } else {
        actions
            .click(element)
            .keyDown(Keys.CONTROL)
            .sendKeys("a")
            .keyUp(Keys.CONTROL)
            .sendKeys(Keys.DELETE)
            .perform();
      }
      element.click();
      System.out.println("Cleared text for" + text);

    } catch (Exception e) {
      System.out.println("Clear text not worked");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void clearTextWithTab(By strobj, String text) throws IOException {
    try {
      WebElement element = driver.findElement(strobj);
      Actions actions = new Actions(driver);
      actions
          .click(element)
          .keyDown(Keys.CONTROL)
          .sendKeys("a")
          .keyUp(Keys.CONTROL)
          .sendKeys(Keys.DELETE)
          .sendKeys(Keys.TAB)
          .perform();
      // actions.sendKeys(Keys.DELETE).perform();

      System.out.println("Cleared text for" + text);

    } catch (Exception e) {
      System.out.println("Clear text");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void EnterTextClear(By strobj, String text) throws IOException {

    try {
      SwitchtoActive();
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(strobj));
      Wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      Actions actions = new Actions(driver);
      if (os.equals("mac")) {
        actions
            .click(element)
            .keyDown(Keys.COMMAND)
            .sendKeys("a")
            .keyUp(Keys.COMMAND)
            .sendKeys(Keys.DELETE)
            .perform();
      } else {
        actions
            .click(element)
            .keyDown(Keys.CONTROL)
            .sendKeys("a")
            .keyUp(Keys.CONTROL)
            .sendKeys(Keys.DELETE)
            .perform();
      }
      element.sendKeys(text);
      System.out.println("EnterText" + text + " is entered ");

    } catch (Exception e) {
      System.out.println("EnterText" + text + " is not entered");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void EnterTextByEntert(By strobj, String text) throws IOException {

    try {
      SwitchtoActive();
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      element.sendKeys(text);
      // Added this thread because of add property address section
      element.sendKeys(Keys.ENTER);
      System.out.println("EnterText" + text + " is entered ");
    } catch (Exception e) {
      System.out.println("EnterText" + text + " is not entered");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void imageUpload(String imageFile) throws AWTException, Exception {
    try {
      StringSelection stringSelection = new StringSelection(imageFile);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

      Robot robot;
      robot = new Robot();

      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_ENTER);
      robot.keyRelease(KeyEvent.VK_ENTER);
    } catch (Exception e) {
      System.out.println("text is not cleared");
      FailScreenshot(e);
    }
  }

  public void EscKey(By strobj, String text) throws IOException {

    try {

      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(strobj));
      Actions builders = new Actions(driver);
      Action seriesOfAction = builders.moveToElement(element).sendKeys(Keys.ESCAPE).build();

      seriesOfAction.perform();

      System.out.println("escape key" + text + " is entered ");

    } catch (Exception e) {
      System.out.println("escape key" + text + " is not entered");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void EnterTextByDownEnter(By strobj, String text) throws IOException {
    System.out.println();
    try {
      SwitchtoActive();
      WebElement element = driver.findElement(strobj);
      element.sendKeys(text);
      ImplicitWaitSwitch(5000);
      element.sendKeys(Keys.ARROW_DOWN);
      element.sendKeys(Keys.ENTER);
      System.out.println("EnterText" + text + " is entered ");
    } catch (Exception e) {
      System.out.println("EnterText" + text + " is not entered");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void DownAndEnter(By strobj) throws IOException {
    System.out.println();
    try {
      SwitchtoActive();
      WebElement element = driver.findElement(strobj);
      ImplicitWaitSwitch(5000);
      element.sendKeys(Keys.ARROW_DOWN);
      element.sendKeys(Keys.ENTER);
      System.out.println("Down arrow and entered is pressed");
    } catch (Exception e) {
      System.out.println("Down arrow and entered is not pressed");

      FailScreenshot(e);
    }
  }

  public void Backspace(By strobj) throws IOException {
    System.out.println();
    try {
      SwitchtoActive();
      WebElement element = driver.findElement(strobj);
      ImplicitWaitSwitch(5000);
      element.sendKeys(Keys.BACK_SPACE);
      System.out.println("BACK_SPACE is pressed");
    } catch (Exception e) {
      System.out.println("BACK_SPACE is not pressed");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void EnterBtn(By strobj) {
    System.out.println();
    try {
      WebElement element = driver.findElement(strobj);
      // element.sendKeys(text);
      ImplicitWaitSwitch(3000);
      element.sendKeys(Keys.ENTER);
      System.out.println("entered ");

    } catch (Exception e) {
      System.out.println(" not entered");
    }
  }

  public void goBack() throws IOException {
    try {
      driver.navigate().back();
    } catch (Exception e) {

    }
  }

  public void ClickElement(By strobj, String strButtonName) throws IOException {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    JavascriptExecutor js = (JavascriptExecutor) driver;
    WebElement element = null;
    try {
      element = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      wait.until(ExpectedConditions.elementToBeClickable(strobj));
      if (browser.equals("Safari")) {
        SwitchtoActive();
        executor.executeScript("arguments[0].click();", element);
      } else element.click();
      System.out.println("Element " + strButtonName + " clicked \n");
    } catch (StaleElementReferenceException e1) {
      e1.printStackTrace();
      WebElement element1 =
          wait.until(
              ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(strobj)));
      element1.click();
    } catch (ElementClickInterceptedException e2) {
      executor.executeScript("arguments[0].scrollIntoView(false);", element);
      assert element != null;
      element.click();
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      System.out.println("Element " + strButtonName + " not clicked");
    }
  }

  public void ClickElementToOpenNewInTab(By strobj, String strButtonName) throws IOException {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    try {
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      wait.until(ExpectedConditions.elementToBeClickable(strobj));
      if (os.equals("mac")) element.sendKeys(Keys.chord(Keys.COMMAND, Keys.ENTER));
      else element.sendKeys(Keys.chord(Keys.CONTROL, Keys.ENTER));
      System.out.println("Click Button" + strButtonName + " clicked \n");
    } catch (StaleElementReferenceException e1) {
      e1.printStackTrace();
      WebElement element1 =
          wait.until(
              ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(strobj)));
      element1.click();
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      System.out.println("Click Button" + strButtonName + " not clicked");
    }
  }

  public void scrollDown(By obj) {
    WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement element = Wait.until(ExpectedConditions.visibilityOfElementLocated(obj));

    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("arguments[0].scrollIntoView();", element);
  }

  public void customSelect(String xpath, String input) throws InterruptedException, IOException {
    try {
      StringBuilder xpathAppend = new StringBuilder(100);
      String[] xpathSplit = xpath.split("zzz");
      xpathAppend.append(xpathSplit[0]).append(input).append(xpathSplit[1]);
      String locater = xpathAppend.toString();
      ClickElement(locatorParser(locater), "SelectBy Address");
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      System.out.println("SelectByaddress");
    }
  }

  public String customizeXpathLocatorWithOneInput(String xpath, String splitText, String input)
      throws InterruptedException, IOException {
    try {
      StringBuilder xpathAppend = new StringBuilder(300);
      String[] xpathSplit = xpath.split(splitText);
      xpathAppend.append(xpathSplit[0]).append(input).append(xpathSplit[1]);
      return xpathAppend.toString();
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public void validatetext(By obj, String text) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      String incomingtext = element.getText();
      System.out.println(incomingtext);
      Pass("Actual value is " + incomingtext);
      Pass("Expected value is" + text);
      if (incomingtext.equals(text)) {
        System.out.println("Tooltip Text is dispayed as expected");

      } else {
        System.out.println("Tooltip text not matched with the expected text");
      }

    } catch (Exception e) {
      System.out.println("Tooltip text not matched with the expected text");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void validatephone(By obj, String text) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      String incomingtext = element.getText();
      String incomingvalue = phoneNumberConverter(incomingtext);
      Pass("Actual value is " + incomingvalue);
      Pass("Expected value is" + text);
      System.out.println(incomingtext);

      if (incomingvalue.equals(text)) {
        System.out.println("Phone number is dispayed as expected");

      } else {
        System.out.println("Phone number not matched with the expected value");
      }

    } catch (Exception e) {
      System.out.println("Phone number text not matched with the expected value");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public String SplitAndAddValueInLocator(String xpath, String splitText, String TextToBeAppended)
      throws IOException {
    try {
      StringBuilder xpathAppend = new StringBuilder(100);
      String[] xpathSplit = xpath.split("splitText");
      xpathAppend.append(xpathSplit[0]).append(TextToBeAppended).append(xpathSplit[1]);
      return xpathAppend.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void ClickJSElement(By strobj, String strButtonName) throws IOException {

    try {
      SwitchtoActive();
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(30));
      WebElement element = Wait.until(ExpectedConditions.elementToBeClickable(strobj));

      JavascriptExecutor executor = (JavascriptExecutor) driver;
      executor.executeScript("arguments[0].click();", element);
      System.out.println("Click Button" + strButtonName + " clicked");

    } catch (Exception e) {
      FailScreenshot(e);
      e.printStackTrace();
      System.out.println("Click Button" + strButtonName + " not clicked. </ br> " + e);
    }
  }

  public boolean textToBePresentInElementValue(By strobj, String value) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      return wait.until(ExpectedConditions.textToBePresentInElementValue(strobj, value));
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(value + " not present in the in" + strobj + ". \n" + e);
      return false;
    }
  }

  public void EnterTextWithJS(By strobj, String text) {
    try {
      System.out.println(strobj);
      SwitchtoActive();
      WebElement element = driver.findElement(strobj);
      JavascriptExecutor executor = (JavascriptExecutor) driver;
      executor.executeScript("arguments[0].value='" + text + "'", element);
      System.out.println("EnterText" + text + " is entered in ");

    } catch (Exception e) {
      System.out.println(e);
      System.out.println("EnterText" + text + " is not entered in ");
    }
  }

  public void EnterTextWithJSEnter(By strobj, String text) {
    try {
      SwitchtoActive();
      System.out.println(strobj);
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(strobj));
      JavascriptExecutor executor = (JavascriptExecutor) driver;
      executor.executeScript("arguments[0].value='" + text + "'", element);
      element.sendKeys(Keys.ENTER);
      System.out.println("EnterText" + text + " is entered in ");

    } catch (Exception e) {
      System.out.println(e);
      System.out.println("EnterText" + text + " is not entered in ");
    }
  }

  public void EnterTextWithDownAndEnterKeyJS(By strobj, String text) throws IOException {
    SwitchtoActive();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
    element.sendKeys(text);
    element.sendKeys(Keys.ARROW_DOWN);
    element.sendKeys(Keys.ARROW_DOWN);
    element.sendKeys(Keys.ENTER);
    System.out.println("EnterText" + text + " is entered ");
  }

  public boolean isDisplayed(By obj, String objdesc) throws IOException {
    Boolean displayed = false;

    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      displayed = element.isDisplayed();
      if (displayed) {
        System.out.println("Element Verification" + objdesc + " is Displayed");
      } else {
        System.out.println("Element Verification" + objdesc + " is not Displayed");
      }
      return displayed;
    } catch (Exception e) {
      // FailScreenshot(e);
      return displayed;
    }
  }

  public boolean isDisplayed(By obj, String objdesc, int seconds) throws IOException {
    Boolean displayed = false;

    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      displayed = element.isDisplayed();
      if (displayed) {
        System.out.println("Element Verification" + objdesc + " is Displayed");
      } else {
        System.out.println("Element Verification" + objdesc + " is not Displayed");
      }
      return displayed;
    } catch (Exception e) {
      // FailScreenshot(e);
      return displayed;
    }
  }

  public void Wait(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isDisplayedByVisible(By obj, String objdesc)
      throws IOException, InterruptedException {

    Boolean displayed = false;

    try {

      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      WebElement element = Wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      displayed = element.isDisplayed();
      System.out.println("Element Verification " + objdesc + " is Displayed");
      return displayed;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      return displayed;
    }
  }

  public boolean isDisplayedByVisible(By obj, String objdesc, int seconds)
      throws IOException, InterruptedException {

    Boolean displayed = false;

    try {

      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      WebElement element = Wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      displayed = element.isDisplayed();
      return displayed;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      return false;
    }
  }

  public boolean isDisplayedByVisibleForAllElements(By obj, String objdesc)
      throws IOException, InterruptedException {
    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      Wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(obj));
      System.out.println("Element Verification " + objdesc + " is Displayed");
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      return false;
    }
  }

  public boolean isDisplayedByVisibleForAllElements(By obj, String objdesc, int seconds)
      throws IOException, InterruptedException {
    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      Wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(obj));
      System.out.println("Element Verification " + objdesc + " is Displayed");
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      return false;
    }
  }

  public boolean isDisplayedByClickable(By obj, String objdesc)
      throws IOException, InterruptedException {
    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = Wait.until(ExpectedConditions.elementToBeClickable(obj));
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      return false;
    }
  }

  public boolean isDisplayedByClickable(By obj, String objdesc, int seconds)
      throws IOException, InterruptedException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      wait.until(ExpectedConditions.elementToBeClickable(obj));
      System.out.println("Element Verification " + objdesc + " is Displayed");
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not Displayed");
      FailScreenshot(e);
      return false;
    }
  }

  // This method is to check whether the element is visible or not
  // if element is there in page it will set pass in test steps and take screenshot
  // else it will set as fail in test steps , so whole test case will be failed
  public boolean isClickable(By obj, String objdesc) throws IOException {
    Boolean clickable = false;
    try {
      WebDriverWait Wait = new WebDriverWait(driver, waitTime);
      clickable =
          Wait.until(
              ExpectedConditions.and(
                  ExpectedConditions.invisibilityOfElementLocated(
                      By.xpath("//div[@class='loader-billing']/img")),
                  ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(obj))));
      System.out.println("check point " + clickable);
      if (clickable) {
        System.out.println("Element Verification " + objdesc + " is clickable");
      } else {
        System.out.println("Element Verification " + objdesc + " is not clickable");
        FailScreenshot("Element Verification " + objdesc + " is not clickable");
      }
      return clickable;
    } catch (Exception e) {
      System.out.println("Element Verification " + objdesc + " is not clickable");
      return clickable;
    }
  }

  public boolean isSorted(WebElement dropdown) throws IOException {
    ArrayList<WebElement> allChildElements =
        (ArrayList<WebElement>) dropdown.findElements(By.xpath("*"));

    ArrayList<String> rentlytext = new ArrayList<>();
    ArrayList<String> shtext = new ArrayList<>();
    for (WebElement e : allChildElements) {
      if (!e.getText().contains("(Smart Home)")) rentlytext.add(e.getText());
      else shtext.add(e.getText());
    }

    rentlytext.remove(0);
    shtext.remove(0);

    if (isOrderd(shtext) && isOrderd(rentlytext)) {
      System.out.println(shtext);
      System.out.println(rentlytext);
      return true;
    }
    return false;
  }

  public boolean isOrderd(ArrayList<String> arr) {
    ArrayList<String> sorted = new ArrayList<>(List.copyOf(arr));
    Collections.sort(sorted);
    if (arr.equals(sorted)) return true;
    return false;
  }

  public boolean isDisplay(By obj, String text) throws IOException, InterruptedException {
    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      Wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      //			Pass(text);
    } catch (Exception e) {
      System.out.println("\n" + text + " : not Displayed");
      FailScreenshot(e);
    }
    return false;
  }

  public boolean isClickableByStyles(By obj, String text) throws IOException, InterruptedException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.elementToBeClickable(obj));
      boolean result = !"none".equals(element.getCssValue("pointer-events"));
      System.out.println(text + " -- Clickable -- " + result);
      return result;
    } catch (Exception e) {
      System.out.println(text + " -- Clickable -- Failed");
      FailScreenshot(e);
      return false;
    }
  }

  public void ToolTip(By obj, String text) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      WebElement element = driver.findElement(obj);
      element.click();

      Actions builder = new Actions(driver);
      Action seriesOfActions = builder.moveToElement(element).build();

      seriesOfActions.perform();
      String val = element.getAttribute("title");
      System.out.println(val);
      if (text.equals(val)) {
        System.out.println("Tooltip Text is dispayed as expected");

      } else {
        System.out.println("Tooltip text not matched with the expected text");
        FailScreenshot("Tooltip text not matched with the expected text");
      }
    } catch (Exception e) {

      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void clickAcceptOnAlertIfPresent() throws IOException {
    try {
      Alert alert = driver.switchTo().alert();
      alert.accept();
    } catch (NoAlertPresentException e) {
      e.printStackTrace();
    }
  }

  public Boolean isEnable(By obj, String text) throws IOException {
    try {
      boolean result = false;
      WebElement element = driver.findElement(obj);
      result = element.isEnabled();
      // If element is disabled by aria-disabled attribute, return false.
      String value = element.getAttribute("aria-disabled");
      if (value != null && value.equals("true")) result = false;
      System.out.println("Field - " + text + " Enabled - " + result);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
    return false;
  }

  public void SwitchtoFrame(String frameId) throws IOException {
    try {
      driver.switchTo().frame(frameId);
    } catch (Exception e) {
      System.out.println("Iframe is not found or issue in iframe");

      FailScreenshot(e);
    }
  }

  public void SwitchToFrameWithWait(String frameId, int sec) {
    WebElement element =
        new WebDriverWait(driver, Duration.ofSeconds(sec))
            .until(ExpectedConditions.visibilityOfElementLocated(By.id(frameId)));
    verifyPageLoaded(30);
    SwitchtoActive();
    element.click();
    driver.switchTo().frame(frameId);
  }

  public void SwitchtoFrameUsingExplicitWait(By Object) throws IOException {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(5))
          .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(Object));
    } catch (Exception e) {
      System.out.println("Iframe is not found or issue in iframe");
      FailScreenshot(e);
    }
  }

  public void SwitchtoFrameUsingExplicitWait(By Object, int seconds) throws IOException {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    try {
      new WebDriverWait(driver, Duration.ofSeconds(seconds))
          .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(Object));
    } catch (StaleElementReferenceException e1) {
      e1.printStackTrace();
      wait.until(
          ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(Object)));
      new WebDriverWait(driver, Duration.ofSeconds(seconds))
          .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(Object));
    } catch (Exception e) {
      System.out.println("Iframe is not found or issue in iframe");
      FailScreenshot(e);
      e.printStackTrace();
    }
  }

  public void SwitchToFrameByIndex(int indexNum) {
    waitUntil.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(indexNum));
  }

  public void SwitchtoDefaultFrame() throws IOException {
    try {
      driver.switchTo().defaultContent();
    } catch (Exception e) {
      System.out.println("Iframe is not found or issue in iframe");

      FailScreenshot(e);
    }
  }

  public void SwitchtoActive() {
    driver.switchTo().activeElement();
  }

  List<WebElement> allOptions;

  public List<WebElement> returnWebElements(By ObjName) {
    try {
      allOptions = driver.findElements(ObjName);
      return allOptions;
    } catch (Exception e) {
      return allOptions;
    }
  }

  public List<String> returnTextInElements(By ObjName) {
    allOptions = driver.findElements(ObjName);
    List<String> textValues = new ArrayList<>();
    for (WebElement element : allOptions) {
      String elementText = element.getText();
      textValues.add(elementText.strip());
    }
    return textValues;
  }

  /**
   * Retrieves the visible text of the web element identified by the provided locator.
   *
   * @param obj The locator strategy (XPath) used to find the web element.
   * @return The visible text of the web element, or {@code null} if an error occurs.
   * @throws IOException If an I/O error occurs while retrieving the text.
   */
  public String getText(By obj) throws IOException {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    try {
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      wait.until(ExpectedConditions.refreshed(ExpectedConditions.presenceOfElementLocated(obj)));
      WebElement element = driver.findElement(obj);
      logger.debug(" Inside get text: " + element.getText() + " ");
      return element.getText();
    } catch (StaleElementReferenceException e) {
      wait.until(ExpectedConditions.refreshed(ExpectedConditions.presenceOfElementLocated(obj)));
      WebElement element = driver.findElement(obj);
      logger.debug(
          " Inside the  StaleElementReferenceException catch block" + element.getText() + " ");
      return element.getText();
    } catch (Exception e) {
      logger.error("Error in getText", e.fillInStackTrace());
      return null;
    }
  }

  public String getText(By obj, int seconds) throws IOException {
    try {
      WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      WebElement element = Wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      System.out.println(" Inside get text: " + element.getText() + " ");
      return element.getText();
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public String getValue(By obj) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      System.out.println("Get value from the attribute : " + element.getAttribute("value"));

      return element.getAttribute("value");
    } catch (Exception e) {
      System.out.println(" Get value not working ");
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public String getHref(By obj) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      System.out.println("Get Href value from the Attribute " + element.getAttribute("href"));
      return element.getAttribute("href");
    } catch (Exception e) {
      System.out.println(" Get Href not working ");
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public String getSrc(By obj) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      System.out.println("Get Src value from the attribute " + element.getAttribute("src"));
      return element.getAttribute("src");
    } catch (Exception e) {
      System.out.println(" Get src not working ");
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public String getURL() throws IOException {
    try {
      return driver.getCurrentUrl();
    } catch (Exception e) {
      System.out.println(" Get URL not working ");
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public void ImplicitWaitSwitch(int time) {
    try {
      driver.manage().timeouts().implicitlyWait(Duration.ofMillis(time));
    } catch (Exception e) {
      System.out.println("Iframe is not found or issue in iframe ");
      e.printStackTrace();
      try {
        FailScreenshot(e);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  public void PageLoadTimeout(int time) throws IOException {
    try {
      driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(time));
    } catch (Exception e) {
      System.out.println(" page load time out failed ");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  // This method is to select the values in the drop down of the portal
  // This is only supported for <select> and <option> tag in HTML. Check this in DOM before using it
  // Method will check presence of element in DOM 1st and then check visible in UI or not
  // Select in Selenium has 3 option to choose from drop down. Here Visible text is used
  public void SelectText(By objName, String strValue) throws IOException {

    try {

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      wait.until(ExpectedConditions.presenceOfElementLocated(objName));
      wait.until(ExpectedConditions.visibilityOfElementLocated(objName));

      Select select = new Select(driver.findElement(objName));
      if (strValue != null && strValue != "") {
        select.selectByVisibleText(strValue);
        System.out.println("\n Select Value " + strValue + " is selected ");
      }
    } catch (Exception e) {
      System.out.println(e);
      FailScreenshot(e);
    }
  }

  // This method is to select the values in the drop down of the portal
  // This is only supported for <select> and <option> tag in HTML. Check this in DOM before using it
  // Method will check presence of element in DOM 1st and then check visible in UI or not
  // Select in Selenium has 3 option to choose from drop down. Here <option> tag attribute value is
  // used
  public void SelectByValue(By objName, String strValue) throws IOException {

    try {

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      wait.until(ExpectedConditions.presenceOfElementLocated(objName));
      wait.until(ExpectedConditions.visibilityOfElementLocated(objName));

      Select select = new Select(driver.findElement(objName));
      if (strValue != null && strValue != "") {
        select.selectByValue(strValue);
        System.out.println("\n Select Value " + strValue + " is selected ");
      }
    } catch (Exception e) {
      System.out.println(e);
      FailScreenshot(e);
    }
  }

  public void SelectAction(By objName) throws InterruptedException {
    WebElement select = driver.findElement(objName);
    Actions builders = new Actions(driver);
    Action seriesOfAction =
        builders.moveToElement(select).click().sendKeys(Keys.DOWN).click().build();
    seriesOfAction.perform();
  }

  public void selectOption(String option) {
    JavascriptExecutor executor = (JavascriptExecutor) driver;
    String script =
        "function selectOption(s) {\r\n"
            + "   var sel = document.querySelector('.custom-select-options');\r\n"
            + "   for (var i = 0; i < sel.options.length; i++)\r\n"
            + "   {\r\n"
            + "       if (sel.options[i].text.indexOf(s) > -1)\r\n"
            + "       {\r\n"
            + "           sel.options[i].selected = true;\r\n"
            + "           break;\r\n"
            + "       }\r\n"
            + "   }\r\n"
            + "}\r\n"
            + "return selectOption('"
            + option
            + "');";

    executor.executeScript(script);
  }

  public void getJSDropdown(By dropDown, String elementID) throws Exception {

    JavascriptExecutor executor = (JavascriptExecutor) driver;
    String dropdownScript =
        "var select = document.getElementByName('"
            + dropDown
            + "'); for(var i = 0; i < select.options.length; i++){if(select.options[i].text == '"
            + elementID
            + "'){ select.options[i].selected = true; } }";

    executor.executeScript(dropdownScript);
  }

  public String readPropertyFile(String file, String name) throws IOException {
    if (teamName.equals("Integration")) {
      file = "Integration";
    }
    FileReader reader =
        new FileReader(
            rootPath + "/src/main/resources/FrameworkProperties/Framework.properties");
    Properties p = new Properties();
    p.load(reader);
    return p.getProperty(name);
  }

  public void writePropertyFile(String file, String name, String value) throws IOException {
    if (teamName.equals("Integration")) {
      file = "Integration";
    }
    String filePath =
        rootPath + "/src/main/resources/FrameworkProperties/" + file + "Framework.properties";
    Properties p = new Properties();
    try (FileReader reader = new FileReader(filePath)) {
      p.load(reader);
    }
    p.setProperty(name, value);
    try (FileWriter writer = new FileWriter(filePath)) {
      p.store(writer, null);
    }
  }

  public String readDbCredentials(String name) throws IOException {
    FileReader reader =
        new FileReader(rootPath + "/src/main/resources/FrameworkProperties/DB.properties");
    Properties p = new Properties();
    p.load(reader);
    return p.getProperty(name);
  }

  void parseLogin(JSONObject employee, String object, String variable) {
    if (employee != null) {
      JSONObject employeeObject = (JSONObject) employee.get(object);

      if (employeeObject != null) {
        value = (String) employeeObject.get(variable);
      } else {
        System.err.println("Error: Nested object is null for key: " + object);
      }
    } else {
      System.err.println("Error: Main employee object is null");
    }
  }

  public String jsonParser(String filename, String Object, String variable)
      throws IOException, ParseException {
    JSONParser jsonParserObject = new JSONParser();
    if (filename.contains("Data.json")) {
      if (teamName.equals("Integration")) {
        filename = getDataCrossTeam("Integration", "Integration");
      }
    }
    try (FileReader reader = new FileReader(filename)) {
      Object obj = jsonParserObject.parse(reader);
      JSONArray userList = (JSONArray) obj;
      userList.forEach(emp -> parseLogin((JSONObject) emp, Object, variable));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return value;
  }

  public void waitClick(By obj, int i) {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(i));
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
    element.click();
    System.out.println(" Clicked!!! ");
  }

  public void waitClickableCheck(By obj, int i) {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(i));
    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(obj));
    element.click();
    System.out.println(" Clicked!!! ");
  }

  private String parselogin(JSONObject employee, String Object, String variable)
      throws IOException {

    JSONObject employeeObject = (JSONObject) employee.get(Object);
    value = (String) employeeObject.get(variable);
    return value;
  }

  public String jsonPut1(String filename, String Object, String variable, String value)
      throws FileNotFoundException, IOException, ParseException {

    JSONParser jsonParserObject = new JSONParser();
    if (teamName.equals("Integration")) {
      filename = getDataCrossTeam("Integration", "Integration");
      try (FileReader reader = new FileReader(filename)) {
        JSONObject obj = (JSONObject) jsonParserObject.parse(reader);
        JSONObject obj1 = (JSONObject) obj.get(Object);
        obj1.put(variable, value);

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      try (FileReader reader = new FileReader(filename)) {
        JSONObject obj = (JSONObject) jsonParserObject.parse(reader);
        JSONObject obj1 = (JSONObject) obj.get(Object);
        obj1.put(variable, value);

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private void parsePut(
      JSONObject employee, String filename, String Object, String variable, String value)
      throws IOException {

    JSONObject employeeObject = (JSONObject) employee.get(Object);
    employeeObject.put(variable, value);

    try (FileWriter file1 = new FileWriter(filename)) {
      file1.write("[" + employee.toJSONString() + "]");
      file1.flush();
    }
  }

  public void jsonPut(String filename, String Object, String variable, String value)
      throws FileNotFoundException, IOException, ParseException {
    String finalfilename = teamnamecheck(filename);
    JSONParser jsonParserObject = new JSONParser();
    try (FileReader reader = new FileReader(finalfilename)) {
      Object obj = jsonParserObject.parse(reader);
      JSONArray userlist = (JSONArray) obj;
      userlist.forEach(
          emp -> {
            try {
              parsePut((JSONObject) emp, finalfilename, Object, variable, value);
            } catch (IOException e) {

            }
          });

    } catch (FileNotFoundException e) {

    }
  }

  public String teamnamecheck(String filename) {
    if (teamName.equals("Integration")) {
      filename = getDataCrossTeam("Integration", "Integration");
      System.out.println(filename);
    }
    return filename;
  }

  public void jsonPutIntegration(String Object, String variable, String value)
      throws FileNotFoundException, IOException, ParseException {
    filename = teamnamecheck(filename);
    JSONParser jsonParserObject = new JSONParser();
    try (FileReader reader = new FileReader(filename)) {
      Object obj = jsonParserObject.parse(reader);
      JSONArray userlist = (JSONArray) obj;
      userlist.forEach(
          emp -> {
            try {
              parsePut((JSONObject) emp, filename, Object, variable, value);
            } catch (IOException e) {

            }
          });

    } catch (FileNotFoundException e) {

    }
  }

  public static By locatorParser(String locator) {

    By loc = By.id(locator.replaceAll("'", "").trim());

    String[] arrSplit = locator.split("By.");
    for (int i = 0; i < arrSplit.length; i++) {
      logger.debug(arrSplit[i]);
    }
    char identifier = arrSplit[1].charAt(0);
    String message = arrSplit[1].substring(0, 1);

    if (message.contains("i")) {
      String loc1 = locator.substring(locator.indexOf("(") + 1, locator.length() - 1);
      loc = By.id(loc1.replaceAll("'", "").trim());
    } else if (message.contains("n")) {
      String loc2 = locator.substring(locator.indexOf("(") + 1, locator.length() - 1);
      loc = By.name(loc2.replaceAll("'", "").trim());
    } else if (message.contains("x")) {
      String loc3 = locator.substring(locator.indexOf("(") + 1, locator.length() - 1);
      String result = loc3.substring(1, loc3.length() - 1);
      loc = By.xpath(result);
    } else if (message.contains("l")) {
      String loc2 = locator.substring(locator.indexOf("(") + 1, locator.length() - 1);
      loc = By.linkText(loc2.replaceAll("'", "").trim());
    } else if (message.contains("c")) {
      String loc2 = locator.substring(locator.indexOf("(") + 1, locator.length() - 1);
      loc = By.cssSelector(loc2.replaceAll("'", "").trim());
    }

    return loc;
  }

  public Boolean fluentWait(By obj) {
    try {
      FluentWait<WebDriver> wait =
          new FluentWait<WebDriver>(driver)
              .withTimeout(Duration.ofSeconds(10))
              .pollingEvery(Duration.ofSeconds(2))
              .ignoring(NoSuchElementException.class);

      WebElement element =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  WebElement ele = driver.findElement(obj);
                  return ele;
                }
              });
      System.out.println(element);
      Boolean result = element.isDisplayed();
      return result;
    } catch (NoSuchElementException e) {

      return false;
    }
  }

  public static String getObjectFile(String pathname) {
    if (pathname.contains("."))
      pathname = pathname.split("\\.")[2] + "/" + pathname.split("\\.")[3];
    return rootPath + "/src/main/java/TestCaseExecution/Objects/" + pathname + ".json";
  }

  public static String getDataFile(String pathname) {
    if (teamName.equals("Integration")) {
      pathname = "Integration/Integration";
    }
    if (pathname.contains("."))
      pathname = pathname.split("\\.")[2] + "/" + pathname.split("\\.")[3];
    return rootPath + "/src/main/resources/TestData/" + pathname + "Data" + ".json";
  }

  public String getObjectCrossTeam(String TeamName, String classname) {
    String path;
    path =
        System.getProperty("user.dir")
            + "/src/main/java/TestCaseExecution/Objects/"
            + TeamName
            + "/"
            + classname
            + ".json";
    return path;
  }

  public String getDataCrossTeam(String TeamName, String classname) {
    String path;
    path =
        System.getProperty("user.dir")
            + "//src/main/resources/TestData/"
            + TeamName
            + "/"
            + classname
            + "Data"
            + ".json";
    return path;
  }

  public void closedriver() {
    driver.close();
  }

  public void sendKeysByAction(By obj, String key) throws IOException {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
    wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
    Actions builder = new Actions(driver);
    Action seriesOfActions = builder.moveToElement(element).click().sendKeys(element, key).build();

    seriesOfActions.perform();
  }

  public void SendHover(By obj, String key) {
    WebElement element = driver.findElement(obj);
    Actions builder = new Actions(driver);
    Action seriesOfActions = builder.moveToElement(element).build();

    seriesOfActions.perform();
  }

  public void sendKeysByActionForSearch(By obj, String key) {
    WebElement element = driver.findElement(obj);
    Actions builder = new Actions(driver);
    Action seriesOfActions =
        builder.moveToElement(element).click().sendKeys(element, key).sendKeys(Keys.ENTER).build();

    seriesOfActions.perform();
  }

  public void sendNumberByAction(By obj, String key) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(obj));

    WebElement zipcode = driver.findElement(obj);

    Actions builders4 = new Actions(driver);
    Action seriesOfAction4 =
        builders4.moveToElement(zipcode).click().sendKeys(zipcode, key).build();
    seriesOfAction4.perform();
  }

  public void ExplicitWaitSwitch(By element, Duration timeInSeconds, String type) {
    try {
      Wait<WebDriver> wait =
          new FluentWait<>(driver)
              .withTimeout(timeInSeconds)
              .pollingEvery(Duration.ofMillis(500))
              .withMessage("Time expired for " + type);
      switch (type) {
        case "visible":
          wait.until(ExpectedConditions.visibilityOfElementLocated(element));
          logger.debug("IT was visible");
          break;
        case "clickable":
          wait.until(ExpectedConditions.elementToBeClickable(element));
          logger.debug("IT was clickable");
          break;
        case "notVisible":
          wait.until(ExpectedConditions.invisibilityOfElementLocated(element));
          logger.debug("IT was invisible ");
          break;
        default:
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.debug("ExplicitWaitSwitch failed for " + element.toString(), e.getCause());
    }
  }

  public void clickByAction(By obj) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
      WebElement element = wait.until(ExpectedConditions.elementToBeClickable(obj));
      Actions builder = new Actions(driver);
      Action seriesOfActions = builder.moveToElement(element).click().build();
      seriesOfActions.perform();
    } catch (Exception e) {
      FailScreenshot(e);
      System.out.println("click by action not performed!!!");
    }
  }

  public void switchToAlert() throws IOException {

    try {
      Alert alert = driver.switchTo().alert();
      System.out.println(alert.getText() + " Alert is Displayed");
    } catch (NoAlertPresentException ex) {
      System.out.println("Alert is NOT Displayed");
      FailScreenshot("Alert is NOT Displayed");
    }
  }

  public void acceptInAlertPopup() throws IOException {
    try {
      Alert alert = driver.switchTo().alert();
      System.out.println(alert.getText() + " Alert is Displayed");
      alert.accept();
    } catch (Exception e) {
      System.out.println("Alert is NOT Displayed");
      FailScreenshot(e);
    }
  }

  // button is enable or not

  public Boolean isBtnEnable(By obj, String text) throws IOException {
    try {
      Boolean result = false;
      WebElement element = driver.findElement(obj);
      result = element.isEnabled();
      if (result) System.out.println("Enabled Button");
      else System.out.println("Disabled Button");
      return result;
    } catch (Exception e) {
      FailScreenshot(e);
    }
    return false;
  }

  // button is enable or not with custom wait time

  public Boolean isBtnEnable(By obj, String text, long Seconds) throws IOException {
    try {
      Boolean result = false;
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Seconds));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      result = element.isEnabled();
      if (result) System.out.println("Enabled Button");
      else System.out.println("Disabled Button");
      return result;
    } catch (Exception e) {
      FailScreenshot(e);
    }
    return false;
  }

  // Check page url and content in the page then close that tab
  public void checkURLandContent(String URL, By obj, String stepInfo) throws IOException {
    try {
      ArrayList<String> tabs2 = new ArrayList<String>(driver.getWindowHandles());
      driver.switchTo().window(tabs2.get(1));

      String url = driver.getCurrentUrl();

      if (url.contentEquals(URL)) {
        Pass(URL + " : URL showed in the browser URL field is correct ");
      } else {
        FailScreenshot(URL + " : URL is not correct");
      }

      if (isDisplayedByVisible(obj, stepInfo))
        PassScreenshot(stepInfo + " : Content showed in the page");
      else FailScreenshot(stepInfo + " : Content not showed in the page");

      driver.close();
      driver.switchTo().window(tabs2.get(0));
      SwitchtoFrame("platform-iframe");

    } catch (Exception e) {

      FailScreenshot(e);
    }
  }

  // dropdown select using list
  public void liSelect(By obj, String text) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(obj));
      wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(obj));
      List<WebElement> liElement = driver.findElements(obj);

      for (WebElement element : liElement) {
        System.out.println(element.getText());
        if (element.getText().equals(text)) {
          element.click();
          driver.switchTo().activeElement();
          System.out.println("li selected");
          break;
        }
      }

    } catch (Exception e) {

      FailScreenshot(e);
    }
  }

  // dropdown select using list
  public void liSelectUsingJS(By obj, String text) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(obj));
      List<WebElement> liElement = driver.findElements(obj);

      for (WebElement element : liElement) {
        System.out.println(element.getText());
        if (element.getText().equals(text)) {
          JavascriptExecutor executor = (JavascriptExecutor) driver;
          executor.executeScript("arguments[0].click();", element);
          driver.switchTo().activeElement();
          System.out.println("li selected");
          break;
        }
      }

    } catch (Exception e) {

      FailScreenshot(e);
    }
  }

  public boolean isPresent(By obj, String value) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, waitTime);
      wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      System.out.println("Element present: " + value);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isPresent(By obj, String value, int seconds) throws IOException {
    try {

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      System.out.println("Element present: " + value);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isNotPresent(By obj, String value) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      return wait.until(ExpectedConditions.invisibilityOfElementLocated(obj));
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isNotPresent(By obj, String value, int seconds) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      return wait.until(ExpectedConditions.invisibilityOfElementLocated(obj));
    } catch (Exception e) {
      return false;
    }
  }

  // To generate random number for address
  // int input is for max range value example: 100 means output between 1 to 100 range
  public String randNum(int max) {
    Random rand = new Random();

    // Generate random integers in range 0 to 999
    int rand_int1 = rand.nextInt(max);
    String value = Integer.toString(rand_int1);
    return value;
  }

  // To generate random number for address
  // int input is for max range value example: 100 means output between 1 to 100 range
  public String randomNumberWithMinAndMax(int min, int max) {
    String value = "";
    if (min < max) {
      try {
        Random rand = new Random();
        // Generate random integers in range min=0 to max=1000 will give 0 to 999
        int rand_int1 = rand.nextInt((max - min) + 1) + min;
        value = Integer.toString(rand_int1);
      } catch (Exception e) {

      }
    }
    return value;
  }

  // To generate random names for names

  public String randString() {
    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    StringBuilder sb = new StringBuilder(14);
    Random random = new Random();
    for (int i = 0; i < 14; i++) {
      char c = chars[random.nextInt(chars.length)];
      sb.append(i == 5 ? " " : c);
    }
    String output = sb.toString();
    return output;
  }

  // To find Check box is selected or not
  public boolean isSelected(By obj, String objdesc) {
    Boolean result = false;
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      result = element.isSelected();
      if (result) {
        logger.debug("Element Verification" + objdesc + " is Selected");
      } else {
        logger.error("Element Verification" + objdesc + " is not Selected");
      }
      return result;
    } catch (Exception e) {
      logger.error("Element Verification" + objdesc + " is not Selected");
      ImplicitWaitSwitch(30);
      return result;
    }
  }

  public boolean isSelected(By obj, String objdesc, int seconds) {
    Boolean result = false;
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      result = element.isSelected();
      if (result) {
        System.out.println("Element Verification" + objdesc + " is Selected");
      } else {
        System.out.println("Element Verification" + objdesc + " is not Selected");
      }
      return result;
    } catch (Exception e) {
      System.out.println("Element Verification" + objdesc + " is not Selected");
      ImplicitWaitSwitch(30);
      return result;
    }
  }

  // Alert is displayed or not
  public boolean isAlertDisplyed() {
    boolean foundAlert = false;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    try {
      wait.until(ExpectedConditions.alertIsPresent());
      foundAlert = true;
    } catch (TimeoutException eTO) {
      foundAlert = false;
    }
    return foundAlert;
  }

  public void dragAndDrop(By obj, int value1, int value2) {
    WebElement slider = driver.findElement(obj);
    Actions move = new Actions(driver);
    Action action = (Action) move.dragAndDropBy(slider, value1, value2).build();
    action.perform();
  }

  public void switchTab(int tabNum) throws IOException {

    try {
      ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
      driver.switchTo().window(tabs.get(tabNum));
      driver.close();
      // driver.switchTo().window(tabs.get(0));
    } catch (TimeoutException eTO) {
      FailScreenshot(eTO);
    }
  }

  public void switchBrowserTab() throws IOException {

    try {
      ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
      driver.switchTo().window(tabs.get(1));
      //			    	    driver.close();
      //			    	    driver.switchTo().window(tabs.get(0));
      //			    	    SwitchtoFrame(frameID);
    } catch (TimeoutException e) {
      FailScreenshot(e);
    }
  }

  public void switchBrowserTab(int tabNumber) throws IOException {

    try {
      ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
      driver.switchTo().window(tabs.get(tabNumber));
    } catch (TimeoutException e) {
      FailScreenshot(e);
    }
  }

  public void switchBrowserTab(String frameID) throws IOException {

    try {
      ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
      //			    	    driver.switchTo().window(tabs.get(1));																		Thread.sleep(2000);
      driver.close();
      driver.switchTo().window(tabs.get(0));
      SwitchtoFrame(frameID);
    } catch (TimeoutException | IOException e) {
      FailScreenshot(e);
    }
  }

  public boolean paginationTextCheck(By obj) {

    try {

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      String pagination = getText(obj);
      String[] textSplited = pagination.split(" ");
      if (Integer.parseInt(textSplited[2]) >= 1) {
        System.out.println("\ntotal " + obj + " showed in the page:");
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public void CheckPageURL(String URL) throws IOException {

    try {
      String currentURL = driver.getCurrentUrl();
      if (currentURL.equals(URL)) {
        System.out.println("URL is matching for " + URL);
        Pass(" Checking URL :" + URL);
      } else {
        System.out.println("URL not is matching for " + URL);
        FailScreenshot(" Not showing correct URL " + URL);
        //			    		 return;
      }

    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public String PhoneNumberRegex(String Number) throws IOException {
    Number = Number.replaceAll("[^0-9]+", "");
    return Number;
  }

  public String dateAndTimenow() throws IOException {

    try {

      String dateOnly = "", dateAndTime = "", finalDateAndtTime = "";
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
      Date date = new Date();
      String dateandtime = formatter.format(date);
      String[] splitDate = dateandtime.split("/");
      for (String a : splitDate) dateOnly = dateOnly + a;

      String[] splitDateAndTime = dateOnly.split(" ");
      for (String a : splitDateAndTime) dateAndTime = dateAndTime + a;

      String[] splitFinalDateAndTime = dateAndTime.split(":");
      for (String a : splitFinalDateAndTime) finalDateAndtTime = finalDateAndtTime + a;

      return finalDateAndtTime;

    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  public String previousMonthDate(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public String currentDate() throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Date date = new Date();
    return formatter.format(date);
  }

  public String nextMonthDate(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, +count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public String previousYear(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, -count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public String currentMonth() throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MMMM");
    Date date = new Date();
    return formatter.format(date);
  }

  public String nextYear(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, +count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public String currentYear() throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
    Date date = new Date();
    return formatter.format(date);
  }

  public String nextDate(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, +count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public String previousDate(int count) throws InterruptedException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, +count);
    Date date = calendar.getTime();
    return formatter.format(date);
  }

  public boolean isTextPresentByValue(By obj) throws IOException {

    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      wait.until(ExpectedConditions.attributeToBeNotEmpty(element, "value"));
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification is not Displayed");
      // FailScreenshot(e);
      return false;
    }
  }

  public boolean isTextPresentByValue(By obj, int seconds) throws IOException {

    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      wait.until(ExpectedConditions.attributeToBeNotEmpty(element, "value"));
      return true;
    } catch (Exception e) {
      System.out.println("Element Verification is not Displayed");

      FailScreenshot(e);
      return false;
    }
  }

  public String getTextFromInputField(String id) throws IOException {

    try {
      WebElement inpElement = driver.findElement(By.id(id));
      String val = inpElement.getAttribute("value");
      return val;
    } catch (Exception e) {
      System.out.println("Element Verification is not Displayed");

      FailScreenshot(e);
      return null;
    }
  }

  public boolean isTextPresent(String val) throws IOException {

    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(
          ExpectedConditions.presenceOfAllElementsLocatedBy(
              By.xpath("//*[contains(text()," + val + ")]")));
      List<WebElement> liElement =
          driver.findElements(By.xpath("//*[contains(text()," + val + ")]"));
      if (liElement.size() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      System.out.println("Element Verification is not Displayed");

      FailScreenshot(e);
      return false;
    }
  }

  // To get current page URL
  public String getCurrentPageURL() throws IOException {
    try {
      return driver.getCurrentUrl();
    } catch (Exception e) {
      FailScreenshot(e);
      return "";
    }
  }

  public void accessability() {
    final URL scriptUrl = ReusableLibrary.class.getResource("/axe.min.js");
    org.json.JSONObject responseJSON = new AXE.Builder(driver, scriptUrl).analyze();
    org.json.JSONArray violation = responseJSON.getJSONArray("violations");
    if (violation.isEmpty()) {
      System.out.println("No violations found");
    } else {
      AXE.writeResults("Accessabiltyresult", responseJSON);
    }
    AxeBuilder builder = new AxeBuilder();
    Results results = builder.analyze(driver);
    List<Rule> violations = results.getViolations();
    if (violations.size() == 0) {
      System.out.println("No violations found");
    } else {
      JSONParser jsonParser = new JSONParser();
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String AxeReportPath =
          System.getProperty("user.dir") + File.separator + "AxeReports" + File.separator;
      String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
      String AxeViolationReportPath = AxeReportPath + "AccessibilityViolations_ " + timeStamp;
      AxeReporter.writeResultsToJsonFile(AxeViolationReportPath, results);
    }
  }

  /**
   * For Generating the Axe Report at the Point of function call during run. If the runAxe is set as
   * False and executionFlag is set as True , the function will return
   *
   * @param driver the driver instance
   * @param executionFlag it can be either custom boolean i.e you can run the accessAbilityReport
   *     only in chrome accessAbilityReport(driver, ifChrome())
   * @throws IOException when writing into the java-ally we can get the IOException
   */
  public void accessAbilityReport(WebDriver driver, boolean executionFlag) throws IOException {
    if (!runAxe && executionFlag) return;
    HtmlCsRunner htmlCsRunner = new HtmlCsRunner(driver);
    try {
      htmlCsRunner.execute();
    } catch (Exception e) {
    }
    try {
      htmlCsRunner.generateHtmlReport();
    } catch (IOException e) {
    }
  }

  public void webDriverWaitSend(By obj, String text, int i) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(i));
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
    element.sendKeys(text);
  }

  public String getCSS(By strobj, String css) {
    WebElement element = driver.findElement(strobj);
    return element.getCssValue(css);
  }

  public void fileUpload(String str) throws IOException {
    StringSelection stringSelection = new StringSelection(str);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    Robot robot;
    try {
      robot = new Robot();

      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      Wait(5000);
      robot.keyPress(KeyEvent.VK_ENTER);
      robot.keyRelease(KeyEvent.VK_ENTER);
    } catch (Exception e) {
      System.out.println("Exception:" + e);
      FailScreenshot("fileUpload Failure");
    }
  }

  public void SwitchtoFrameByWebElement(By Object) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
      wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(Object));

    } catch (Exception e) {
      System.out.printf("Iframe is not found or issue in iframe");

      FailScreenshot(e);
    }
  }

  public void webDriverWaitforAllElements(By strobj, int time) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(time));
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(strobj));
  }

  public void webDriverWaitFind(By strobj) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      WebElement ele = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      System.out.println("Element Found");
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public String phoneNumberConverter(String phnumber) {
    String[] arr = phnumber.split("[- ( )  ]+");
    String new_ph = "";
    new_ph += arr[1];
    new_ph += arr[2];
    new_ph += arr[3];
    return new_ph;
  }

  public void closePopup() throws Exception {
    Actions action = new Actions(driver);
    action.sendKeys(Keys.ESCAPE).build().perform();
  }

  public void mouseOverElement(By strobj, By clickElement) throws IOException {
    try {
      WebElement element = driver.findElement(strobj);
      WebElement click = driver.findElement(clickElement);
      String mouseOverScript =
          "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover',true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
      ((JavascriptExecutor) driver).executeScript(mouseOverScript, element);
      wait(1000);
      ((JavascriptExecutor) driver).executeScript(mouseOverScript, click);
      wait(1000);
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", click);

    } catch (Exception e) {
      System.out.printf("Mouse over failed");
      System.out.printf(e.toString());
      FailScreenshot("mouseOverElement Failure");
    }
  }

  public void layoutTest(String filename, boolean executionFlag) throws IOException {
    if (executionFlag && runGalen) {
      try {
        logger.info("Layout test for " + filename);
        LayoutReport layoutReport =
            Galen.checkLayout(
                driver,
                rootPath
                    + "/src/main/java/TestCaseExecution/Objects/Galen/"
                    + teamName
                    + "/"
                    + filename
                    + ".gspec",
                List.of("desktop"));
        GalenTestInfo test = GalenTestInfo.fromString(filename);
        test.getReport().layout(layoutReport, filename);
        galenTest.add(test);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void Datepicker_MF() throws InterruptedException {
    Wait(7000);
    LocalDate date = LocalDate.now(); // Gets the current date
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
    String datenow = date.format(formatter);
    System.out.println(datenow);
    int i1 = Integer.parseInt(datenow);
    ZoneId zone1 = ZoneId.of("America/New_York");
    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("hh");
    String timenow = time1.format(timeformatter);
    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter).toUpperCase();
    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }

    String tourdatexpath =
        "//*[@class='DayPicker-Day DayPicker-Day--selected DayPicker-Day--today'][text()='"
            + i1
            + "']";
    String tourtimexpath = "//*[text()=" + "'" + timenow + ":" + minnow + FNANnow + "']";
    System.out.println(tourdatexpath);
    System.out.println(tourtimexpath);
    driver.findElement(By.xpath(tourdatexpath)).click();
    Wait(3000);
    driver.findElement(By.xpath("//*[@data-testid='select-date-submit']")).click();
    Wait(5000);
    DateTimeFormatter railwayFormatter = DateTimeFormatter.ofPattern("HH");
    int hourRailway = Integer.parseInt(time1.format(railwayFormatter));
    System.out.println(hourRailway + "check it");
    if (hourRailway < 12) {
      driver.findElement(By.xpath("//*[@data-testid='slot_Morning']")).click();
    } else if (hourRailway < 17) {
      driver.findElement(By.xpath("//*[@data-testid='slot_Afternoon']")).click();
    } else {
      driver.findElement(By.xpath("//*[@data-testid='slot_Evening']")).click();
    }
    Wait(3000);
    driver.findElement(By.xpath(tourtimexpath)).click();
    Wait(3000);
  }

  public void pageRefresh() throws Exception {
    try {
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
      driver.navigate().refresh();
      System.out.println(driver.getCurrentUrl());
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public String Tooltipvalidation(By obj, String text) throws IOException {

    try {
      WebElement element = driver.findElement(obj);
      Actions builder = new Actions(driver);
      Action seriesOfActions = builder.moveToElement(element).build();

      seriesOfActions.perform();
      String val = element.getAttribute("data-original-title");
      System.out.printf(val);

      if (val.equals(text)) {
        System.out.printf("Tooltip is displayed as expected");

      } else {
        System.out.printf("Tooltip is not displayed as expected");
      }
    } catch (Exception e) {
      System.out.printf("Element Verification is not Displayed");

      FailScreenshot(e);
      return null;
    }
    return value;
  }
  // copy the data to the clipboard from source

  // paste the contents from clipboard to destination
  public void PasteContent(By strobj) {
    try {
      System.out.println(strobj);
      SwitchtoActive();
      WebElement element = driver.findElement(strobj);
      element.click();
      element.sendKeys(Keys.CONTROL + "v");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public URL toUrl(String browserUrl) throws MalformedURLException, URISyntaxException {
    URI uri = new URI(browserUrl);
    return uri.toURL();
  }

  public boolean isAvailable(By obj, String objdesc) throws IOException {
    boolean displayed;
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      displayed = element.isDisplayed();
      if (displayed) {
        logger.debug("Element Verification " + objdesc + " is Displayed");
      } else {
        logger.debug("Element Verification " + objdesc + " is not Displayed");
      }
      return displayed;
    } catch (Exception e) {
      logger.error("In isAvailable", e.getCause());
      logger.error("Element Verification " + objdesc + " is not Displayed");
      return false;
    }
  }

  public boolean isBtnWorking(By strobj) throws IOException {
    try {
      WebElement element = driver.findElement(strobj);
      String attribute = element.getAttribute("style");
      attribute = attribute.substring(0, attribute.indexOf(";"));
      if (attribute.equals("cursor: not-allowed")) {
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("In isBtnWorking", e.getCause());
      return false;
    }
  }

  public void ClickElementbyclick(By strobj, String strButtonName) throws IOException {
    try {
      WebDriverWait wait = null;
      if (!isDisplayed(strobj, strButtonName)) {
        Assert.fail("button not present");
        return;
      }
      wait = new WebDriverWait(driver, waitTime);
      WebElement element = wait.until(ExpectedConditions.elementToBeClickable(strobj));
      element.click();
      Wait(1);
      logger.debug("Click Button " + strButtonName + " clicked");
    } catch (Exception e) {
      logger.error("Click Button " + strButtonName + " not clicked", e.getCause());
      FailScreenshot(e);
    }
  }

  public String clearElement(By Objname, String attribute) throws IOException {
    WebElement element = driver.findElement(Objname);
    element.clear();
    return element.getAttribute(attribute);
  }

  public void selectDropdownElement(By strobj, String text) throws IOException {
    try {
      ExplicitWaitSwitch(strobj, waitTime, "visible");
      WebElement testDropDown = driver.findElement(strobj);
      Select dropdown = new Select(testDropDown);
      dropdown.selectByVisibleText(text);
    } catch (Exception e) {
      logger.error("Dropdown" + text + " is not selected", e.getCause());
    }
  }

  public void dumpPage(WebDriver driver, String pageName) throws IOException {
    GalenPageDump dump = new GalenPageDump("pageName");
    try {
      dump.setMaxHeight(1080)
          .setMaxWidth(1920)
          .dumpPage(
              driver,
              System.getProperty("user.dir")
                  + "/src/main/java/TestCaseExecution/Objects/Galen/"
                  + teamName
                  + "/"
                  + pageName
                  + ".gspec",
              System.getProperty("user.dir") + "/src/main/resources" + pageName);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  public String generatePhoneNo(String Role) {
    Random randomGenerator = new Random();
    String randomPhNo1 = Integer.toString(randomGenerator.nextInt(600) + 100);
    String randomPhNo2 = Integer.toString(randomGenerator.nextInt(600) + 100);
    String randomPhNo3 = Integer.toString(randomGenerator.nextInt(1000) + 1000);
    String phone_input = randomPhNo1 + randomPhNo2 + randomPhNo3;
    printLog(Role + " PhoneNo is = " + phone_input);
    return phone_input;
  }

  public String generateMailID(String name, String Role) {
    Random randomGenerator = new Random();
    String Email =
        name
            + randomGenerator.nextInt(10000)
            + "@"
            + (circleci ? findBrowserName() + "CI" : "localtest")
            + ".com";
    printLog(Role + " PhoneNo is = " + Email);
    return Email;
  }

  public void waitToClick(String File, String Object, String Element, int sec)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Element));
    WebElement element =
        new WebDriverWait(driver, Duration.ofSeconds(sec))
            .until(ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(30);
    verifyAJAXLoaded();
    element.click();
    verifyPageLoaded(30);
    printLog("Clicked on " + selector);
  }

  public void clickElement(String File, String Object, String Key)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Key));
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    executor.executeScript("arguments[0].click();", element);
    printLog("Clicked on " + selector);
  }

  // Conditions -> 0 = visible, 1 = presence, 2 = clickable, 3 = refresh_visible
  public void clickElement(String File, String Object, String Key, int condition) throws Exception {
    By selector = locatorParser(jsonParser(File, Object, Key));
    WebElement element =
        waitUntil.until(
            (condition == 1)
                ? ExpectedConditions.presenceOfElementLocated(selector)
                : (condition == 2)
                    ? ExpectedConditions.elementToBeClickable(selector)
                    : (condition == 3)
                        ? ExpectedConditions.refreshed(
                            ExpectedConditions.visibilityOfElementLocated(selector))
                        : ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    executor.executeScript("arguments[0].click();", element);
    try {
      element.click();
    } catch (Exception e) {
    }
  }

  public void enterText(String locatorPath, String DataPath, String Object, String Key)
      throws Exception {
    By selector = locatorParser(jsonParser(locatorPath, Object, Key));
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    String Data = jsonParser(DataPath, Object, Key);
    verifyPageLoaded(60);
    executor.executeScript("arguments[0].value='" + Data + "'", element);
    printLog("Data Entered in " + selector);
  }

  public void enterData(String File, String Object, String Key, String text) throws Exception {
    By selector = locatorParser(jsonParser(File, Object, Key));
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    element.click();
    element.sendKeys(text);
    printLog("Data Entered in " + selector);
  }

  public void enterTextTabClear(
      String JSONPath, String DataPath, String Object, String Key, String NewData)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(JSONPath, Object, Key));
    String Data = jsonParser(DataPath, Object, NewData);
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    element.clear();
    element.sendKeys(Data);
    element.sendKeys(Keys.TAB);
    printLog("Data Entered in " + selector);
  }

  public void uploadFile(String File, String Object, String Space, String file, String name)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Space));
    String assetPath = System.getProperty("user.dir") + readPropertyFile(file, name);
    WebElement uploadID = waitUntil.until(ExpectedConditions.presenceOfElementLocated(selector));
    verifyPageLoaded(60);
    uploadID.sendKeys(assetPath);
    waitUntil.until(ExpectedConditions.alertIsPresent());
    waitSec(2); // Verifying alert out of DOM is not possible ...
    driver.switchTo().alert().accept();
    printLog("Document Uploaded");
  }

  public void selectAddressDropdown(String File, String Object, String Key, int Down)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Key));
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    waitUntil.until(ExpectedConditions.elementToBeClickable(selector));
    verifyPageLoaded(60);
    executor.executeScript("arguments[0].click();", element);
    waitSec(3); // Wait for Google API response ...
    for (int i = 0; i < Down; i++) element.sendKeys(Keys.ARROW_DOWN);
    element.sendKeys(Keys.ENTER);
    executor.executeScript("arguments[0].click();", element);
    waitSec(3); // Wait for Autofill fields ...
    printLog("Data Entered in " + selector);
  }

  public void selectByDownArrow(String File, String Object, String Key, int Down)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Key));
    WebElement element = waitUntil.until(ExpectedConditions.visibilityOfElementLocated(selector));
    waitUntil.until(ExpectedConditions.elementToBeClickable(selector));
    verifyPageLoaded(60);
    Actions builder = new Actions(driver);
    Action seriesOfActions = builder.moveToElement(element).click().build();
    seriesOfActions.perform();
    waitSec(2); // Wait for interacting through keys ...
    for (int i = 0; i < Down; i++) element.sendKeys(Keys.ARROW_DOWN);
    element.sendKeys(Keys.ENTER);
    printLog("Data Entered in " + selector);
  }

  public void verifyPageLoaded(int sec) {
    WebDriverWait waitUntil = new WebDriverWait(driver, Duration.ofSeconds(sec));
    waitUntil.until(
        (ExpectedCondition<Boolean>)
            wd ->
                ((JavascriptExecutor) wd)
                    .executeScript("return document.readyState")
                    .equals("complete"));
  }

  public void verifyAJAXLoaded() {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    wait.until(
        webDriver -> {
          JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
          // AJAX expressions and their checks
          String[] ajaxExpressions = {
            "typeof jQuery !== 'undefined' ? jQuery.active : 0",
            "typeof Ajax !== 'undefined' ? Ajax.activeRequestCount : 0",
            "typeof dojo !== 'undefined' ? dojo.io.XMLHTTPTransport.inFlight.length : 0"
          };
          // Check AJAX request progress
          for (String expression : ajaxExpressions) {
            Object ajaxStatus = jsExecutor.executeScript("return " + expression);
            if (ajaxStatus instanceof Long && (Long) ajaxStatus != 0) {
              return false;
            }
          }
          return true;
        });
  }

  public void waitToInvisible(String File, String Object, String Element, int sec)
      throws Exception {
    By selector = locatorParser(jsonParser(File, Object, Element));
    new WebDriverWait(driver, Duration.ofSeconds(sec))
        .until(ExpectedConditions.invisibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    verifyAJAXLoaded();
  }

  public void verifyElement(String File, String Object, String Element, int sec) throws Exception {
    By selector = locatorParser(jsonParser(File, Object, Element));
    new WebDriverWait(driver, Duration.ofSeconds(sec))
        .until(ExpectedConditions.visibilityOfElementLocated(selector));
    verifyPageLoaded(60);
    verifyAJAXLoaded();
    printLog("The Element is Displayed = " + selector);
  }

  public void verifyCount(String File, String Object, String Element, int count)
      throws IOException, ParseException {
    By selector = locatorParser(jsonParser(File, Object, Element));
    verifyPageLoaded(60);
    int elementCount = driver.findElements(selector).size();
    printLog("The Present Count is " + elementCount);
    if (count != elementCount) org.junit.Assert.fail("* Condition is Failed!");
  }

  public void waitSec(int Sec) {
    try {
      Sec = Sec * 1000;
      Thread.sleep(Sec);
    } catch (Exception e) {
      System.out.println("waitSec Method have an error to Handle");
    }
  }

  public String jsonGetBrowserData(String filename, String objectName, String variable) {
    String browser =
        ifChrome() ? "chrome" : ifSafari() ? "safari" : ifFirefox() ? "firefox" : "edge";
    String finalObject = browser + "_" + objectName;
    JSONParser jsonParserObject = new JSONParser();
    try (FileReader reader = new FileReader(filename)) {
      Object obj = jsonParserObject.parse(reader);
      JSONArray userList = (JSONArray) obj;
      userList.forEach(emp -> parseLogin((JSONObject) emp, finalObject, variable));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
    return value;
  }

  public void putObject(JSONObject employee, String objectName, String variable, String value) {
    JSONObject employeeObject = (JSONObject) employee.get(objectName);
    employeeObject.put(variable, value);
  }

  public void jsonPutBrowserData(
      String filename, String objectName, String variable, String value) {
    String browser =
        ifChrome() ? "chrome" : ifSafari() ? "safari" : ifFirefox() ? "firefox" : "edge";
    String finalObject = browser + "_" + objectName;
    JSONParser jsonParserObject = new JSONParser();

    try (FileReader reader = new FileReader(filename)) {
      Object obj = jsonParserObject.parse(reader);
      JSONArray userList = (JSONArray) obj;
      userList.forEach(emp -> putObject((JSONObject) emp, finalObject, variable, value));
      try (FileWriter writer = new FileWriter(filename)) {
        writer.write(userList.toJSONString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
  }

  public String getAlphaNumericString(int n) {

    // chose a Character random from this String
    String AlphaNumericString =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";

    // create StringBuffer size of AlphaNumericString
    StringBuilder sb = new StringBuilder(n);

    for (int i = 0; i < n; i++) {

      // generate a random number between
      // 0 to AlphaNumericString variable length
      int index = (int) (AlphaNumericString.length() * Math.random());

      // add Character one by one in end of sb
      sb.append(AlphaNumericString.charAt(index));
    }

    return sb.toString();
  }

  public String getNumericString(int n) {

    // chose a Character random from this String
    String NumericString = "0123456789";

    // create StringBuffer size of AlphaNumericString
    StringBuilder sb = new StringBuilder(n);

    for (int i = 0; i < n; i++) {

      // generate a random number between
      // 0 to AlphaNumericString variable length
      int index = (int) (NumericString.length() * Math.random());

      // add Character one by one in end of sb
      sb.append(NumericString.charAt(index));
    }

    return sb.toString();
  }

  public String getAlphaString(int n) {

    // chose a Character random from this String
    String AlphaString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvxyz";

    // create StringBuffer size of AlphaNumericString
    StringBuilder sb = new StringBuilder(n);

    for (int i = 0; i < n; i++) {

      // generate a random number between
      // 0 to AlphaNumericString variable length
      int index = (int) (AlphaString.length() * Math.random());

      // add Character one by one in end of sb
      sb.append(AlphaString.charAt(index));
    }

    return sb.toString();
  }

  public void Datepicker() throws InterruptedException, ParseException {
    Thread.sleep(3000);
    String nextButton = "//*[@data-testid='select-date-submit']";
    String nextButton1 = "//*[@data-testid ='submission-date-time-submission-multi-family']";

    LocalDate date = LocalDate.now(); // Gets the current date
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
    String datenow = date.format(formatter);
    System.out.println(datenow);
    int i1 = Integer.parseInt(datenow);

    ZoneId zone1 = ZoneId.of("America/Los_Angeles");
    //		ZoneId zone1 = ZoneId.of("Asia/Kolkata");

    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("hh");
    String timenow = time1.format(timeformatter);
    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter);

    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }

    //		String FNAN = (hour<12)?"AM":"PM";
    String tourdatexpath = "//*[text()='" + i1 + "']";
    try {
      driver.findElement(By.xpath(tourdatexpath)).click();
      Thread.sleep(5000);

      driver.findElement(By.xpath(nextButton)).click();
      Thread.sleep(5000);
    } catch (NoSuchElementException e) {

    }

    try {
      DateTimeFormatter railwayFormatter = DateTimeFormatter.ofPattern("HH");
      int hourRailway = Integer.parseInt(time1.format(railwayFormatter));
      System.out.println(hourRailway + "check it");
      if (hourRailway < 12) {
        String morning = "//*[@data-testid='slot_Morning']";
        driver.findElement(By.xpath(morning)).click();
      } else if (hourRailway < 17) {
        String afternoon = "//*[@data-testid='Afternoon']";
        driver.findElement(By.xpath(afternoon)).click();
      } else {
        String evening = "//*[@data-testid='slot_Evening']";
        driver.findElement(By.xpath(evening)).click();
      }
      if (driver
          .findElement(By.xpath("//*[text()=" + "'" + timenow + ":" + minnow + "PM" + "']"))
          .isDisplayed()) {

        String tourtimexpath = "//*[text()=" + "'" + timenow + ":" + minnow + "PM" + "']";

        System.out.println(tourtimexpath);

        Thread.sleep(20000);

        driver.findElement(By.xpath(tourtimexpath)).click();
        Thread.sleep(5000);

        driver.findElement(By.xpath(nextButton1)).click();
        Thread.sleep(5000);

        // driver.findElement(By.xpath(tourtimexpath)).click();
        Thread.sleep(20000);
      }
    } catch (NoSuchElementException e) {

      String tourtimexpath = "(//*[@id='modalContainer']/div/div/div[3]/div/div)[1]";
      System.out.println(tourtimexpath);
      Thread.sleep(20000);

      driver.findElement(By.xpath(tourtimexpath)).click();
      driver.findElement(By.xpath(nextButton1)).click();
      Thread.sleep(5000);
    }
  }

  public void Datepicker_Nextday() throws InterruptedException, ParseException {
    Thread.sleep(3000);
    String nextButton = "//*[@data-testid='select-date-submit']";
    String nextButton1 = "//*[@data-testid ='submission-date-time-submission-multi-family']";

    LocalDate date = LocalDate.now(); // Gets the current date
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
    String datenow = date.format(formatter);
    System.out.println(datenow);
    int i1 = Integer.parseInt(datenow) + 2;

    ZoneId zone1 = ZoneId.of("America/Los_Angeles");
    //		ZoneId zone1 = ZoneId.of("Asia/Kolkata");

    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("hh");
    String timenow = time1.format(timeformatter);
    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter);
    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }

    //		String FNAN = (hour<12)?"AM":"PM";
    String tourdatexpath = "//*[text()='" + i1 + "']";
    count = driver.findElements(By.xpath(tourdatexpath)).size();
    try {
      if (i1 > 25) {
        driver.findElement(By.xpath("(//*[@class='MuiSvgIcon-root'])[2]")).click();
        i1 = 1;
        tourdatexpath = "(//*[text()='" + i1 + "'])[3]";
        driver.findElement(By.xpath(tourdatexpath)).click();
        Thread.sleep(5000);
        driver.findElement(By.xpath(nextButton)).click();
        Thread.sleep(5000);
      } else {
        Thread.sleep(3000);
        count = 1;
        tourdatexpath = "(//*[text()='" + i1 + "'])[" + count + "]";
        driver.findElement(By.xpath(tourdatexpath)).click();
        Thread.sleep(5000);
        driver.findElement(By.xpath(nextButton)).click();
        Thread.sleep(5000);
      }
    } catch (NoSuchElementException e) {

    }

    try {
      DateTimeFormatter railwayFormatter = DateTimeFormatter.ofPattern("HH");
      int hourRailway = Integer.parseInt(time1.format(railwayFormatter));
      System.out.println(hourRailway + "check it");
      if (hourRailway < 12) {
        String morning = "//*[@data-testid='slot_Morning']";
        driver.findElement(By.xpath(morning)).click();
      } else if (hourRailway < 17) {
        String afternoon = "//*[@data-testid='Afternoon']";
        driver.findElement(By.xpath(afternoon)).click();
      } else {
        String evening = "//*[@data-testid='slot_Evening']";
        driver.findElement(By.xpath(evening)).click();
      }
      if (driver
          .findElement(By.xpath("//*[text()=" + "'" + timenow + ":" + minnow + "PM" + "']"))
          .isDisplayed()) {

        String tourtimexpath = "//*[text()=" + "'" + timenow + ":" + minnow + "PM" + "']";

        System.out.println(tourtimexpath);

        Thread.sleep(20000);

        driver.findElement(By.xpath(tourtimexpath)).click();
        Thread.sleep(5000);

        driver.findElement(By.xpath(nextButton1)).click();
        Thread.sleep(5000);

        // driver.findElement(By.xpath(tourtimexpath)).click();
        Thread.sleep(20000);
      }
    } catch (NoSuchElementException e) {

      String tourtimexpath = "(//*[@id='modalContainer']/div/div/div[3]/div/div)[1]";
      System.out.println(tourtimexpath);
      Thread.sleep(20000);

      driver.findElement(By.xpath(tourtimexpath)).click();
      driver.findElement(By.xpath(nextButton1)).click();
      Thread.sleep(5000);
    }
  }

  public void DatepickerDesktop() throws InterruptedException {

    LocalDate date = LocalDate.now(); // Gets the current date
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
    String datenow = date.format(formatter);
    System.out.println(datenow);
    int i1 = Integer.parseInt(datenow);
    ZoneId zone1 = ZoneId.of("Asia/Kolkata");
    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("hh");
    String timenow = time1.format(timeformatter);
    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter).toUpperCase();
    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }
    // String FNAN = (hour<12)?"AM":"PM";
    String tourdatexpath = "//div[text()='" + i1 + "']";
    String tourtimexpath = "//*[text()=" + "'" + timenow + ":" + minnow + FNANnow + "']";
    System.out.println(tourdatexpath);
    System.out.println(tourtimexpath);
    driver.findElement(By.xpath(tourdatexpath)).click();
    Thread.sleep(5000);
    // select slot
    driver.findElement(By.xpath(tourtimexpath)).click();
    Thread.sleep(5000);
  }

  public String timepicker() throws InterruptedException, ParseException {

    ZoneId zone1 = ZoneId.of("America/Los_Angeles");
    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("hh");
    String timenow = time1.format(timeformatter);
    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter);
    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }

    String botime = timenow + ":" + minnow + " PM";

    System.out.println(botime);

    return (botime);
  }

  public String timepicker24() throws InterruptedException, ParseException {

    // ZoneId zone1 = ZoneId.of("America/Los_Angeles");
    ZoneId zone1 = ZoneId.of("Asia/Kolkata");

    LocalTime time1 = LocalTime.now(zone1);
    DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("HH");
    String timenow = time1.format(timeformatter);

    DateTimeFormatter minformatter = DateTimeFormatter.ofPattern("mm");
    String minnow = time1.format(minformatter);
    DateTimeFormatter FNANformatter = DateTimeFormatter.ofPattern("a");
    String FNANnow = time1.format(FNANformatter);
    System.out.println(FNANnow);
    int min = Integer.parseInt(minnow);
    int hour = Integer.parseInt(timenow);
    String FNAN = (hour < 12) ? "AM" : "PM";
    System.out.println(hour);
    if (min >= 0 && min < 15) minnow = "15";
    if (min >= 15 && min < 30) minnow = "30";
    if (min >= 30 && min < 45) minnow = "45";
    if (min >= 45) {
      hour = (hour + 1) % 12;
      if (hour > 0 && hour < 10) timenow = "0" + hour;
      else if (hour == 0) timenow = "12";
      else timenow = "" + hour;
      minnow = "00";
    }

    String time24 = timenow + ":" + minnow;
    System.out.println(time24);
    return time24;
  }

  public void ScrollDownToBottom() {
    try {
      // Create a JavascriptExecutor instance
      JavascriptExecutor js = (JavascriptExecutor) driver;

      // Scroll to the bottom of the page
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public void ScrollUp() {
    try {
      // Create a JavascriptExecutor instance
      JavascriptExecutor js = (JavascriptExecutor) driver;

      // Scroll to the bottom of the page
      js.executeScript("window.scrollTo(0, -document.body.scrollHeight);");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  // To quit the driver
  public void quitDriver() {
    driver.quit();
  }

  //   Find current driver by Browser thread ...

  public String findBrowserName() {
    if (driver.toString().contains("Chrome")) return "Chrome";
    else if (driver.toString().contains("Firefox")) return "Firefox";
    else if (driver.toString().contains("Safari")) return "Safari";
    else if (driver.toString().contains("Edge")) return "Edge";
    else return "none";
  }

  public static boolean isPageLoaded(WebDriver driver) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

      // Wait until the document is in 'complete' state
      wait.until(
          webDriver -> {
            return ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete");
          });

      return true;
    } catch (Exception e) {
      // Handle exceptions if needed
      e.printStackTrace();
      return false;
    }
  }

  public void findElement(By strobj) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      WebElement ele = wait.until(ExpectedConditions.visibilityOfElementLocated(strobj));
      System.out.println("Element Found");
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void ClearKey(By obj) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(obj));
      //			element.sendKeys(text);
      element.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void sendKeysByAction1(By obj, String key) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));

      WebElement element = driver.findElement(obj);
      Actions builder = new Actions(driver);
      Action seriesOfActions =
          builder
              .moveToElement(element)
              .click()
              .sendKeys(element, key)
              .sendKeys(Keys.ENTER)
              .build();

      seriesOfActions.perform();
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public void dclickByAction(By obj) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      wait.until(ExpectedConditions.visibilityOfElementLocated(obj));

      WebElement element = driver.findElement(obj);
      Actions builder = new Actions(driver);
      Action seriesOfActions = builder.moveToElement(element).doubleClick().build();

      seriesOfActions.perform();
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public void timeiqual() throws Exception {
    try {
      driver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
      driver.navigate().refresh();
      System.out.println(driver.getCurrentUrl());
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public void waitUntil(By fieldObj) throws Exception {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    WebElement web1 = wait.until(ExpectedConditions.visibilityOfElementLocated(fieldObj));
    boolean status = web1.isDisplayed();
    try {
      if (status) {
        System.out.println("Found");
      } else {
        System.out.println("Not found");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public void scrolldown(By Object) throws Exception {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
      wait.until(ExpectedConditions.visibilityOfElementLocated(Object));
      JavascriptExecutor js = (JavascriptExecutor) driver;
      // Locating element by link text and store in variable "Element"
      WebElement Element = driver.findElement(Object);

      // Scrolling down the page till the element is found
      js.executeScript("arguments[0].scrollIntoView();", Element);
    } catch (Exception ex) {
      ex.printStackTrace();
      FailScreenshot(ex);
    }
  }

  public void imageupload(String imagefile) throws AWTException, Exception {
    try {

      StringSelection stringSelection = new StringSelection(imagefile);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

      Robot robot;
      robot = new Robot();
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyPress(KeyEvent.VK_ENTER);
      robot.keyRelease(KeyEvent.VK_ENTER);
    } catch (Exception e) {
      System.out.println("text is not cleared");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void openNewWindow(String Url) throws Exception {
    try {
      ((JavascriptExecutor) driver).executeScript("window.open('" + Url + "');");
    } catch (Exception e) {
      System.out.println("problem loading page");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void SwitchToWindowByTitle(String url, String title) throws IOException {
    try {
      ArrayList<String> handles;
      mainWindowsHandle = driver.getWindowHandle();
      handles = new ArrayList<String>(driver.getWindowHandles());
      for (String handle : handles) {
        driver.switchTo().window(handle);
        if (driver.getTitle().equals(title)) {
          return;
        }
      }
      openNewWindow(url);
      SwitchToWindowByTitle(url, title);
    } catch (Exception e) {
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public void cleartext(By obj) throws Exception {
    try {
      WebElement object = driver.findElement(obj);

      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("arguments[0].value = '';", object);
    } catch (Exception e) {
      System.out.println("text is not cleared");
      e.printStackTrace();
      FailScreenshot(e);
    }
  }

  public boolean ifChrome() {
    return (driver.toString().contains("Chrome"));
  }

  public boolean ifFirefox() {
    return (driver.toString().contains("Firefox"));
  }

  public boolean ifSafari() {
    return (driver.toString().contains("Safari"));
  }

  public boolean ifEdge() {
    return (driver.toString().contains("Edge"));
  }

  // copy the data to the clipboard from source
  public void CopyContentFromJsonData(String parser) {
    try {
      StringSelection stringSelection = new StringSelection(parser);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  // To Get the width and height dimension of the element
  public Dimension getWidthAndHeight(By obj) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      return element.getSize();
    } catch (Exception e) {
      System.out.println(" Width and Height Dimension not working ");
      e.printStackTrace();
      FailScreenshot(e);
      return null;
    }
  }

  // To fetch value of HTML tag attribute values
  public String getAttribute(By obj, String attribute) throws IOException {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(obj));
      System.out.println("Get Src value from the attribute " + element.getAttribute(attribute));
      return element.getAttribute(attribute);
    } catch (Exception e) {
      System.out.println(" Get " + attribute + " src not working in " + obj);
      return null;
    }
  }

  @Override
  protected void createDriver() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void createDriverMobile() {}

  @Override
  protected void createMobileScreenDriver() {}

  public ResultSet connectDbReturnResult(
      String PGDATABASE, String PGUSER, String PGPASSWORD, String query)
      throws IOException, SQLException, NullPointerException {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      conn = java.sql.DriverManager.getConnection(PGDATABASE, PGUSER, PGPASSWORD);
      stmt = conn.createStatement();
      logger.info("Executed Query " + query);
      rs = stmt.executeQuery(query);
      return rs;
    } catch (SQLException e) {
      // Close the database resources in case of an exception
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
      throw e;
    }
  }

  protected static String resultSetToJson(ResultSet resultSet) throws SQLException {
    ObjectMapper objectMapper = new ObjectMapper();

    // Convert ResultSet to a List<Map<String, Object>>
    List<Map<String, Object>> resultList = new ArrayList<>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnCount = metaData.getColumnCount();
    while (resultSet.next()) {
      Map<String, Object> row = new HashMap<>();
      for (int i = 1; i <= columnCount; i++) {
        String columnName = metaData.getColumnName(i);
        Object value = resultSet.getObject(i);
        if (metaData.getColumnTypeName(i).equals("date"))
          value = formatDate(resultSet.getTimestamp(i), "yyyy-MM-dd");
        row.put(columnName, value);
      }
      resultList.add(row);
    }

    // Convert the List<Map<String, Object>> to JSON
    try {
      return objectMapper.writeValueAsString(resultList);
    } catch (JsonProcessingException e) {
      logger.error(String.valueOf(e.getCause()));
      return "Error converting ResultSet to JSON";
    }
  }

  public static String formatDate(Timestamp timestamp, String format) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    return dateFormat.format(timestamp);
  }

  public String getFormattedDate(String Case, String format) throws InterruptedException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Date currentDate = calendar.getTime();

    // Function to add days to a date
    Calendar targetCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    switch (Case.toLowerCase()) {
      case "today":
        targetCalendar.setTime(currentDate);
        break;
      case "tomorrow":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.DAY_OF_MONTH, 1);
        break;
      case "yesterday":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.DAY_OF_MONTH, -1);
        break;
      case "previous month":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.MONTH, -1);
        break;
      case "next month":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.MONTH, 1);
        break;
      case "past 30 days":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.DAY_OF_MONTH, -30);
        break;
      case "past 60 days":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.DAY_OF_MONTH, -60);
        break;
      case "past 90 days":
        targetCalendar.setTime(currentDate);
        targetCalendar.add(Calendar.DAY_OF_MONTH, -90);
        break;
      default:
        return "Invalid case. Please use 'Today', 'Tomorrow', or 'Yesterday'";
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(targetCalendar.getTime());
  }

  // Get timestamp in MM/DD/YYYY HH:MM A format
  // pass timezone value as argument and return timestamp in string
  // "America/Los_Angeles" - Pacific Standard Time
  // "America/New_York" - Eastern Standard Time
  // "America/Denver" - Mountain Daylight Time
  // "America/Phoenix" - Arizona Time (no DST)
  // "America/Anchorage" - Alaska Time
  // "America/Chicago" - Central Time
  // "America/Halifax" - Atlantic Time Zone
  // "Pacific/Honolulu" - Hawaii Time
  public static String getCurrentTimeInZone(String zone, Boolean needSpaceInMeridiem)
      throws Exception {
    // Get the current timestamp
    Date currentDate = new Date();
    try {
      SimpleDateFormat sdf;
      if (needSpaceInMeridiem) {
        sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
      } else {
        sdf = new SimpleDateFormat("MM/dd/yyyy hh:mma");
      }
      TimeZone tz = TimeZone.getTimeZone(zone);
      sdf.setTimeZone(tz);
      return sdf.format(currentDate);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void clickEscape() throws Exception {
    Actions action = new Actions(driver);
    action.sendKeys(Keys.ESCAPE).build().perform();
  }

  public String generateRandomPhoneNumber() {
    // Create a StringBuilder to store the generated number
    StringBuilder sb = new StringBuilder();

    // Create a Random object
    Random random = new Random();

    // Generate 10 random digits
    for (int i = 0; i < 10; i++) {
      // Append a random digit (0-9) to the StringBuilder
      sb.append(random.nextInt(10));
    }

    // Convert the StringBuilder to a String and return
    return sb.toString();
  }

  public static String generateRandomString(Integer stringLength) {
    // Define the alphabet from which to generate random letters
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // Length of the random string
    int length = stringLength;

    // Create an instance of Random class
    Random random = new Random();

    // StringBuilder to store the random string
    StringBuilder sb = new StringBuilder();

    // Generate random letters and append them to the StringBuilder
    for (int i = 0; i < length; i++) {
      // Generate a random index within the length of the alphabet
      int index = random.nextInt(alphabet.length());
      // Append the randomly chosen letter to the StringBuilder
      sb.append(alphabet.charAt(index));
    }

    // Convert StringBuilder to String and return
    return sb.toString();
  }

  public String formatToUSPhoneNumber(String phoneNumber) {
    // Remove all non-numeric characters from the phone number
    String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

    // Check if the phone number is of valid length
    if (cleanedNumber.length() != 10) {
      // Handle invalid phone number length
      throw new IllegalArgumentException("Invalid phone number length");
    }

    // Format the phone number as (###) ###-####
    return "("
        + cleanedNumber.substring(0, 3)
        + ") "
        + cleanedNumber.substring(3, 6)
        + "-"
        + cleanedNumber.substring(6, 10);
  }

  /**
   * Retrieves the visible text of the first selected option from a dropdown list or select element
   * identified by the provided By object.
   *
   * @param obj the By object used to locate the dropdown list or select element.
   * @return the visible text of the first selected option.
   * @throws RuntimeException if any exception occurs during the execution, such as timeout waiting
   *     for the element to be located or if the element is not visible.
   */
  public String getSelectedOption(By obj) {
    try {
      WebDriverWait w = new WebDriverWait(driver, waitTime);
      WebElement element = w.until(ExpectedConditions.visibilityOfElementLocated(obj));
      Select select = new Select(element);
      WebElement option = select.getFirstSelectedOption();
      return option.getText();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Asserts that two lists contain the same elements, regardless of their order.
   *
   * @param expected the expected list of strings.
   * @param actual the actual list of strings to be compared against the expected list.
   * @throws AssertionError if the lists are not equal regardless of order.
   */
  public void assertListsEqualIgnoringOrder(List<String> expected, List<String> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    Assert.assertTrue(new HashSet<>(expected).containsAll(actual));
    Assert.assertTrue(new HashSet<>(actual).containsAll(expected));
  }

  public void EnterTextWithEnter(By strobj, String text) throws IOException {
    System.out.println();
    try {
      WebElement webElement = driver.findElement(strobj);
      ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", webElement);
      ClickJSElement(strobj, text);
      WebElement element = driver.findElement(strobj);
      element.sendKeys(text);
      element.sendKeys(Keys.ENTER);
      System.out.printf("EnterText", text + " is entered ");
    } catch (Exception e) {
      System.out.printf("EnterText", text + " is not entered");
      FailScreenshot("EnterText Failure");
    }
  }

  public void slotDeletion(String xpathExpression) {
    WebElement element2 = driver.findElement(By.xpath(xpathExpression));
    Actions actions = new Actions(driver);
    actions.moveToElement(element2);
    actions.moveByOffset(0, -10);
    actions.click();
    actions.perform();
  }

  public static boolean elementNotClickableVerification(WebDriver driver, By by) {
    try {
      // Wait for the element to be present and not clickable
      new WebDriverWait(driver, Duration.ofSeconds(10))
          .until(ExpectedConditions.elementToBeClickable(by));
      return false; // Element is clickable
    } catch (Exception e) {
      return true; // Element is not clickable
    }
  }

  public void waitForNumberOfTabsToBeOne() throws IOException {
    switchBrowserTab(0);
    WebDriverWait wait = new WebDriverWait(driver, waitTime);
    ExpectedCondition<Boolean> numberOfTabsToBeOne =
        new ExpectedCondition<>() {
          @Override
          public Boolean apply(WebDriver driver) {
            Set<String> windowHandles = driver.getWindowHandles();
            logger.debug("Number of windows open: {}", windowHandles.size());
            for (String handle : windowHandles) {
              driver.switchTo().window(handle);
              logger.debug("Window title: {}", driver.getTitle());
            }
            return windowHandles.size() == 1;
          }
        };
    wait.until(numberOfTabsToBeOne);
  }
}
