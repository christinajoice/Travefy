package TestCaseExecution.Pages.Travefy;

import TestCaseExecution.ReusableLibrary.ReusableLibrary;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;

public class New_Trip extends ReusableLibrary {
    protected WebDriver driver;
    String jsonPath, jsonData;
    public String view = "";
    WebDriverWait wait;

    public New_Trip(WebDriver driver, String view) throws IOException {
        super(driver);
        this.driver = driver;
        this.view = view;
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        jsonPath = getObjectFile(this.getClass().getCanonicalName());
        jsonData = getDataFile(this.getClass().getCanonicalName());
    }

    public void newTrip()
            throws IOException, ParseException, Exception, TimeoutException {
        try {
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "newtrip")), "newtrip clicked");
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "itinerary")), "itinerary clicked");
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "Day1")), "Day1 clicked");
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "setDatetextbox")), "setDatetextbox clicked");
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "currentdate")), "currentdate clicked");
            ClickElement(locatorParser(jsonParser(jsonPath, "trip", "Done")), "Done clicked");
        } catch (Exception e) {
            System.out.println(e);
            FailScreenshot(e);
        }
    }
}
