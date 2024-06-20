package ConfigExecution.executionEngine;

import ConfigExecution.browserFactory.DriverManager;
import ConfigExecution.browserFactory.DriverType;
import TestCaseExecution.ActionKeyword.*;
import TestCaseExecution.ReusableLibrary.ReusableLibrary;
import java.io.IOException;
import java.util.logging.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

interface BrowserExtension {
  void quitBrowser();
}

public class BaseAction extends ReusableLibrary implements BrowserExtension {
  DriverManager driverManager;
  public String browserVersion;

  @Override
  public void quitBrowser() {
    driver.quit();
  }

  public void beforeTest(String browser) {
    if (browser.equalsIgnoreCase("Chrome"))
      driverManager = DriverManager.getManager(DriverType.CHROME);
    else if (browser.equalsIgnoreCase("Firefox"))
      driverManager = DriverManager.getManager(DriverType.FIREFOX);
    else if (browser.equalsIgnoreCase("Safari"))
      driverManager = DriverManager.getManager(DriverType.SAFARI);
    else if (browser.equalsIgnoreCase("Edge"))
      driverManager = DriverManager.getManager(DriverType.EDGE);
  }

  public void beforeMethod(String Data) {
    driver = driverManager.getDriver(Data);
    Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
    browserVersion = cap.getBrowserVersion();
  }

  // Get the appropriate Team name
  public BaseAction getActionKeyword(String Team, String view, String browserValue)
      throws IOException {
    Logger logger = Logger.getLogger("Team Name Mismatched");
    switch (Team) {
      case "Travefy":
        return new ActionKeyword(view, browserValue);
      default:
        logger.warning("TeamKey = " + Team + ", Please Update the Valid TeamKey in DataSheet...");
        System.exit(0);
        return new BaseAction();
    }
  }
}
