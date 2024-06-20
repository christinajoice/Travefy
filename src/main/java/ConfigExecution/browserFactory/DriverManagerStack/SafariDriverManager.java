package ConfigExecution.browserFactory.DriverManagerStack;

import static ConfigExecution.executionEngine.DriverScript.Headless;

import ConfigExecution.browserFactory.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafariDriverManager extends DriverManager {
  Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public void createDriver() {
    //    Headless is not possible in Safari
    SafariOptions options = new SafariOptions();
    options.setEnableDownloads(true);
    options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
    try {
      driver = new SafariDriver(options);
    } catch (SessionNotCreatedException e) {
      driver = new SafariDriver(options);
    } catch (Exception e) {
      logger.info("In Final Exception of Safari Browser Initialization", e.fillInStackTrace());
    } finally {
      driver.manage().deleteAllCookies();
      driver.manage().window().maximize();
    }
  }

  public void createDriverMobile() {
    Map<String, Object> deviceMetrics = new HashMap<>();
    deviceMetrics.put("width", 360);
    deviceMetrics.put("height", 650);

    deviceMetrics.put("pixelRatio", 3.0);
    Map<String, Object> mobileEmulation = new HashMap<>();
    mobileEmulation.put("deviceMetrics", deviceMetrics);
    mobileEmulation.put(
        "userAgent",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/604.1");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--no-sandbox");
    if (Headless) options.addArguments("--headless");
    options.setExperimentalOption("mobileEmulation", mobileEmulation);
    driver = new ChromeDriver(options);
  }
}
