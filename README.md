# Rently - Test Automation Suite

# Supported Frameworks :
## Selenium
  * For Testing the Web Applications User Interface
  * The Browsers Supported Currently by the Suite are below
    * In **Linux** OS:
      * Chrome
      * FireFox
      * Edge (Pipeline Parameters need to be Passed to Download in Image)
    * In **Mac** OS:
      * Safari
    * All the browsers need to Specified for execution in the **_Team Config_** Sheet of **DataEngine**
## Galen Framework
  * For Testing the Layout of the Web Applications
  * All the Specs file will be placed in the `src/main/java/TestCaseExecution/Objects/Galen` which will be used by the `layoutTest` method for test

## Axe Framework
  * For Testing the Accessibility level of the web applications
  * We need to call the method from reusableLibrary `accessAbilityReport` at places where this need to be checked

## Library used for formatting and Validation
1. [Git Code Format Maven Plugin](https://github.com/Cosium/git-code-format-maven-plugin)

> [!IMPORTANT]  
> Before Committing any code changes, run the below commands
### For Manual code formatting
```shell
mvn git-code-format:format-code -Dgcf.globPattern=**/*
```
### For Manual code format validation
```shell
mvn git-code-format:validate-code-format -Dgcf.globPattern=**/*
```

## How to run Automation in local
1. Clone the RentlyQE repo by ```git clone repository_url```
2. Add the Project as Maven Project in Intellij or Eclipse. For more Details refer [this.](https://rently.atlassian.net/wiki/spaces/QP/pages/244680315/Rently+Test+Automation+Framework+RTAF)
3. Download the Template Sheet from [here.](https://docs.google.com/spreadsheets/d/1SxMMrwew-JcBNV03Vc_sUrvXqUc-e90n0lUmG8f7fEI/export) and modify according to your actionKeywords and test cases and move it under `src/main/resources/DataEngine`
4. Run the DriverScript

## Setting up triggers in Circle CI
1. Go to Circle CI
2. Select RentlyQE Project
3. On Project Settings , Navigate to the Triggers
4. Click on Add Trigger 
   1. In Trigger Name - Give a Proper name as this will be used on Mail Subject for Understanding the reports origin
   2. In Trigger Description - Give a Brief of the Configuration for understanding
   3. In Repeats - Set as Weekly
   4. In Repeats on these days* - Select All
   5. In Repeats on these months* - Select All
   6. In Start Time (UTC)* - Select the time when the execution need to be started
   7. In Repeats Per Hour* - Select Once Per Hour
   8. In Branch or Tag Name* - mention the name of the branch which needs to triggered
   9. In Pipeline Parameters - According to the need , add the pipeline parameters
      1. For All types of run `DataSheet` Parameter(String) is mandatory - you need to provide the google sheet link with General Access of Anyone with the link as Viewer
      2. For Executing the tests in Linux , `Linux` Parameter(Boolean) should be `true`
      3. For Executing the tests in Mac , `Mac` Parameter(Boolean) should be `true`
      4. For Executing the tests in Edge in **Linux OS** , you need to provide additional parameter `Edge` (Boolean) of true
      5. For Fetching the Database Credentials, `DB` Parameter(String) , You can provide multiple Applications in Comma Separated Values, If Not Passed , Default is taken as None and no operations will not be done
   10. Save the trigger

> [!NOTE]  
> The Trigger will not not start exactly at the time set in trigger , there will be some deviations



