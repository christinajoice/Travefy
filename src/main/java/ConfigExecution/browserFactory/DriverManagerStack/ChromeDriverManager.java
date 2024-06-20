package ConfigExecution.browserFactory.DriverManagerStack;

import static ConfigExecution.executionEngine.DriverScript.Headless;
import static ConfigExecution.executionEngine.DriverScript.teamName;

import ConfigExecution.browserFactory.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromeDriverManager extends DriverManager {
  Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public void createDriver() {
    HashMap<String, Object> chromePrefs = new HashMap<>();
    ChromeOptions options = new ChromeOptions();
    options.setExperimentalOption("prefs", chromePrefs);
    options.addArguments("start-maximized");
    options.addArguments("--window-size=1920,1080");
    options.addArguments("--disable-download-notification");
    if (Headless) options.addArguments("--headless=new");
    driver = new ChromeDriver(options);
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
        "Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.2 Mobile/15E148 Safari/604.1");
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--remote-allow-origins=*");
    chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
    chromeOptions.addArguments("--window-size=1920,1080");
    if (Headless) chromeOptions.addArguments("--headless=new");
    driver = new ChromeDriver(chromeOptions);
  }
}
