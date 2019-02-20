package step_definitions;

import org.junit.runner.RunWith;

import com.jpmc.hlt.utils.ReportLog;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:Reports", "json:Reports/CucumberReports/cucumber.json"},
							dryRun = false, glue = "step_definitions", monochrome = true, 
							features = "./src/test/resources/features",
							tags = "DebugTestCaseName")
public class TestRunner extends ReportLog{

}
