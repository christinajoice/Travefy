package ConfigExecution.browserFactory.DriverManagerStack;

import static ConfigExecution.executionEngine.DriverScript.Headless;
import static ConfigExecution.executionEngine.DriverScript.teamName;

import ConfigExecution.browserFactory.DriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class FirefoxDriverManager extends DriverManager {

  @Override
  protected void createDriver() {
    FirefoxOptions options = new FirefoxOptions();
    options.addPreference("browser.download.folderList", 2);
    options.addPreference(
        "browser.download.dir", System.getProperty("user.dir") + "/Downloads/" + teamName + "/");
    options.addArguments("--disable-download-notification");
    if (Headless) options.addArguments("--headless");
    driver = new FirefoxDriver(options);
    driver.manage().window().setSize(new Dimension(1920, 1080));
  }

  @Override
  protected void createDriverMobile() {
    FirefoxOptions options = new FirefoxOptions();
    if (Headless) options.addArguments("--headless=new");
    options.addPreference(
        "general.useragent.override",
        ""
            + "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1");
    driver = new FirefoxDriver(options);
    driver.manage().window().setSize(new Dimension(360, 667));
  }
}
