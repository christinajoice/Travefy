package TestCaseExecution.ActionKeyword;

import ConfigExecution.executionEngine.BaseAction;
import TestCaseExecution.Pages.Travefy.*;
import java.io.IOException;
import org.json.simple.parser.ParseException;

public class ActionKeyword extends BaseAction {

  Login page;
  New_Trip nt;
  public String Data;

  public String locktype;

  public ActionKeyword(String data, String browser) throws IOException {
    beforeTest(browser);
    beforeMethod(data);
    page = new Login(driver, Data);
    nt= new New_Trip(driver,Data);
  }

  public void Login(String Data) throws IOException, ParseException, Exception {
    page.LoginAuthentication();
  }

  public void newTrip(String Data) throws IOException, ParseException, Exception {
    nt.newTrip();
  }
  public void quitdriver(String Data) {
    driver.quit();
  }

  public static void closeBrowser(String result) {
    //		ReusableLibrary page = new ReusableLibrary();

  }
}
