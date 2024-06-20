package TestCaseExecution.ReusableLibrary;

import java.io.IOException;
import org.openqa.selenium.WebDriver;

public class GlobalLibrary extends ReusableLibrary {
  protected String jsonPath;

  public GlobalLibrary(WebDriver driver) throws IOException {
    super(driver);
    this.driver = driver;
    jsonPath =
        this.getClass().getCanonicalName().split("\\.")[2] + "/" + this.getClass().getSimpleName();
  }
}
