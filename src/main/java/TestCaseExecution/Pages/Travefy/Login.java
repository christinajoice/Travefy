/** */
package TestCaseExecution.Pages.Travefy;

import static ConfigExecution.executionEngine.DriverScript.teamName;

import TestCaseExecution.ReusableLibrary.ReusableLibrary;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author rently
 */
public class Login extends ReusableLibrary {

  protected WebDriver driver;
  String jsonPath, jsonData;
  public String view = "";
  WebDriverWait wait;

  WebElement element;

  public Login(WebDriver driver, String view) throws IOException {
    super(driver);
    this.driver = driver;
    this.view = view;
    wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    jsonPath = getObjectFile(this.getClass().getCanonicalName());
    jsonData = getDataFile(this.getClass().getCanonicalName());
  }

  @SuppressWarnings("unchecked")
  public void LoginAuthentication()
      throws IOException, ParseException, Exception, TimeoutException {
    try{
      getApplication(readPropertyFile(teamName,"URL1"));
      EnterText(locatorParser(jsonParser(jsonPath, "Login", "email")), "christinallrejoice@gmail.com");
      EnterText(locatorParser(jsonParser(jsonPath, "Login", "password")), "Christisalive1$");
      ClickElement(locatorParser(jsonParser(jsonPath, "Login", "login_btn")),"login CTA clicked");
      Thread.sleep(10000);
    } catch (Exception e) {
      System.out.println(e);
      FailScreenshot(e);
    }
  }



}
