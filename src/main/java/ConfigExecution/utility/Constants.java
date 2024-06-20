package ConfigExecution.utility;

public class Constants {

  public static final int waitTimeInSeconds = 30;
  // List of System Variables ...

  public static final String rootPath = System.getProperty("user.dir"),
      Path_TestData = rootPath + "/src/main/resources/DataEngine/DataEngine.xlsx",
      MailDataPath = rootPath + "/Reports/MailData.txt",
      Test_URL = "https://travefy.com/",

      version = "v1.1",
      Sheet_TestSteps = "Test_Steps",
      Sheet_TestCases = "Test_Cases",
      TeamConfig = "Team_Config";

  // Data Engine Excel sheets = Sheet_TestCases ...
  public static final int Col_TestCaseID = 0,
      Col_Precondition = 1,
      Col_PostCondition = 2,
      Col_RunMode = 3,
      Col_Result = 4,
      Col_TestCaseDescription = 5,
      Col_View = 6;

  // Data Engine Excel Sheets = Sheet_TestSteps ...
  public static final int Col_TestScenarioID = 1,
      Col_TestStepDescription = 2,
      Col_ActionKeyword = 3;

  // Data Engine Excel sheets = TeamConfig ...

  public static final int RunValue = 1,
      TeamName = 1,
      toMailID = 3,
      ccMailID = 4,
      runHeadless = 6,
      ChromeBrowser = 7,
      FirefoxBrowser = 8,
      SafariBrowser = 9,
      EdgeBrowser = 10,
      GalenTest = 11,
      AxeTest = 12,
      LightHouseTest = 13;
}
