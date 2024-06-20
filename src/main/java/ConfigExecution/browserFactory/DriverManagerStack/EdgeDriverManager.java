package ConfigExecution.browserFactory.DriverManagerStack;

import static ConfigExecution.executionEngine.DriverScript.Headless;
import static ConfigExecution.executionEngine.DriverScript.teamName;

import ConfigExecution.browserFactory.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class EdgeDriverManager extends DriverManager {
  public void createDriver() {
    HashMap<String, Object> edgePrefs = new HashMap<>();
    edgePrefs.put("browser.show_hub_popup_on_download_start", false);
    edgePrefs.put(
        "download.default_directory",
        System.getProperty("user.dir") + "/Downloads/" + teamName + "/");
    EdgeOptions options = new EdgeOptions();
    options.setExperimentalOption("prefs", edgePrefs);
    options.addArguments("start-maximized");
    options.addArguments("--window-size=1920,1080");
    if (Headless) options.addArguments("--headless=new");
    driver = new EdgeDriver(options);
  }

  @Override
  protected void createDriverMobile() {
    Map<String, Object> deviceMetrics = new HashMap<>();
    deviceMetrics.put("width", 360);
    deviceMetrics.put("height", 650);
    deviceMetrics.put("pixelRatio", 3.0);
    Map<String, Object> mobileEmulation = new HashMap<>();
    mobileEmulation.put("deviceMetrics", deviceMetrics);
    mobileEmulation.put(
        "userAgent",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.2 Mobile/15E148 Safari/604.1");
    EdgeOptions edgeOptions = new EdgeOptions();
    edgeOptions.addArguments("--remote-allow-oriWebDriverManagergins=*");
    edgeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
    edgeOptions.addArguments("--window-size=1920,1080");
    if (Headless) edgeOptions.addArguments("--headless=new");
    driver = new EdgeDriver(edgeOptions);
  }
}
