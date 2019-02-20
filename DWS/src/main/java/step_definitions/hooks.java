package step_definitions;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.jpmc.hlt.utils.ObjectProp;
import com.jpmc.hlt.utils.ReportLog;
import com.jpmc.hlt.utils.UIControls;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class hooks extends ReportLog{

	static Logger log = Logger.getLogger(hooks.class);
	public static String sReportFolder;
	
	@Before
	public void beforeScenario() {
		ObjectProp.loadProperties("./data/LoginObjectRepository.properties");
		
		// Start building the Report
		String log4jConfPath = "./log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);
		ReportLog.Reportfolder = UIControls.createReportFolders();
		log.info("sReportFolder" + ReportLog.Reportfolder);
		ReportLog.CreateHtmlReport();
		ReportLog.StartTestCase("");
		
	}
	
	@After
	public void afterScenario(Scenario scenario) {
		log.info("This will run after the Scenario");
		if (scenario.isFailed()) {
			log.error("Test Scenario has Failed!!!");
		} else {
			log.error("Test Scenario has Passed!!!");
		}
		
		ReportLog.EndTestCase();
		UIControls.closeBrowser();
	}
	
}
