package ConfigExecution.browserFactory;

import ConfigExecution.browserFactory.DriverManagerStack.*;
import org.openqa.selenium.WebDriver;

public abstract class DriverManager {

  protected WebDriver driver;

  protected abstract void createDriver();

  protected abstract void createDriverMobile();

  public WebDriver getDriver(String view) {
    if (null == driver) {
      if (view.equalsIgnoreCase("Web")) {
        createDriver();
      } else {
        createDriverMobile();
      }
    }
    return driver;
  }

  protected void createMobileScreenDriver() {
    // TODO Auto-generated method stub

  }

  public static DriverManager getManager(DriverType type) {

    DriverManager driverManager;

    switch (type) {
      case CHROME:
        driverManager = new ChromeDriverManager();
        break;
      case FIREFOX:
        driverManager = new FirefoxDriverManager();
        break;
      case SAFARI:
        driverManager = new SafariDriverManager();
        break;
      case EDGE:
        driverManager = new EdgeDriverManager();
        break;
      default:
        driverManager = new ChromeDriverManager();
        break;
    }
    return driverManager;
  }
}
