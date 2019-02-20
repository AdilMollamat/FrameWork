package com.jpmc.hlt.shortsale;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.jpmc.hlt.utils.DataBase;
import com.jpmc.hlt.utils.ExcelUtils;
import com.jpmc.hlt.utils.ObjectProp;
import com.jpmc.hlt.utils.ReportLog;
import com.jpmc.hlt.utils.UIControls;

import net.masterthought.cucumber.Reportable;

public class SSLoanCondition {

	static Logger log = Logger.getLogger(SSLoanCondition.class);
	
	public static String gHostName, gPortNumber, gDatabase, gDBUserName, gDBPassword, gQuery, gSSLoanNumber, gSSClientid,
						 gSSCaseID, gSSNNumber, gTestCaseName, gSheetName, gStrConditionWB, gRCC_FollowUpResult;
	
	public static String gLoanNumber, gSSClientID, gSURL, gEnvironment, gUserName, gSUserID, gApplicationNam, gSSDBLoanNumber,
						 gSSInitialpwd, gSSInitialLoginID, gStatus, gSubStatus;
	
	public static WebDriver driver = UIControls.driver;
					     
	
	
	public static void loadSSVariables() {
		gEnvironment = "QA4";
		ssReadData();
	}
	
	public static void preRequisite(String strTestCaseName, String strSheetName) throws IllegalAccessException {
		
		gTestCaseName = strTestCaseName;
		gSheetName = strSheetName;
		
		// Refactoring code which handles object repository loading
		log.info("Start of preRequisite.......");
		ObjectProp.loadProperties("./data/SSObjectRepository.Properties");
		
		Boolean pactualSts = false, pactualSubsts = false;
		
		// Read the environment details
		//gEnvironment = System.getProperty("env");
		gEnvironment = "QA4";
		
		// Read Test data from SS_Regression.xls
		ssReadData();
		
		if (gSSCaseID.isEmpty()) {
			if (gLoanNumber != null && !gLoanNumber.isEmpty()) {
				
			} else {
				String tmpQuery = DataBase.AppendMSPCurrentTableInQuery(gQuery);
				String strLoannumber[] = SSFindTestLoan(tmpQuery);
				gSSCaseID = strLoannumber[0];
				gSSClientid = strLoannumber[1];
				gSSDBLoanNumber = strLoannumber[2];
			}
		}
		
		ReportLog.StartTestCase(strTestCaseName);
		log.info("Test Case Name : " + strTestCaseName);
		
		// Create browser Instance
		driver = UIControls.createBrowser("internetexplorer");
		UIControls.openUrl(gSURL);
		
		if ("rules".equals(gSSInitialpwd)) {
			//SSGeneric.ssLogin(gSSInitialLoginID, gSSInitialpwd);
		} else {
			//SSGeneric.ssSSOLogin(gSSInitialLoginID, gSSInitialpwd)
		}
		
		if (!gStatus.isEmpty()) {              // handle if nothing is given Status value in excel
			log.info("Case ID :" + gSSCaseID);
			//SSGeneric.openShortSaleCase(gSSCaseID);
			//pactualSts = SSGeneric.ssVerifyStatus(gStatus);
			log.info("pactualSts is " + pactualSts);
			if (!pactualSts) {
				//MoveCaseToStatus(gStatus);
			}
			if (!gSubStatus.isEmpty()) {
				//pactualSubsts = SSGeneric.ssVerifySubStatus(gSubStatus);
				log.info("pactualSubsts is " + pactualSubsts);
				if (!pactualSubsts) {
					//MoveCaseToSubStatus(gSubStatus);
				}
			}
			
		}
	
		log.info("End of preRequisite");
	}
	
	
	public static void ssReadData() {
		String tmpUserID;
		
		//=========doc load=========
		gSSDBLoanNumber = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, gSheetName, gTestCaseName, "SSDBLoanNumber");
		gSSClientID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, gSheetName, gTestCaseName, "SSDBLoanNumber");
		gSSCaseID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, gSheetName, gTestCaseName, "CaseID");
		gSURL = ExcelUtils.getCellValWithRowColName(ExcelUtils.sCDSApp, "URLs", gEnvironment, "URL");
		gSSClientID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sCDSApp, "DB_Credential", gEnvironment, "HOSTNAME");
		gSSClientID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB_Credential", gEnvironment, "DATABASE_PORT");
		gSSClientID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB_Credential", gEnvironment, "DATABASE_UID");
		gSSClientID = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB_Credential", gEnvironment, "DATABASE_PWD");
		
	}
	
	
	public static String[] SSFindTestLoan(String Query) {
		// Read Loan Numbers from actual query
		//  Concatenate the loan numbers in actual query
		String ConstructedQuery = DataBase.AppendLoanNumberInQuery(Query);
		
		// Construct the query for different case status
		String subStatus = "";
		if ((SSLoanCondition.gSubStatus).isEmpty()) {
			subStatus = "is null";
		} else {
			subStatus = "'" + SSLoanCondition.gSubStatus + "'";
		}
		
		String originalStatus = "and B.PYSTATUSWORK = '" + SSLoanCondition.gStatus + "' and B.SBSTS_NM = " + subStatus;
		String openerStatus = "and B.PYSTATUSWORK = 'Pending-SSIntakeReview' and B.SBSTS_NM is null";
		String pendingCCStatus = "and B.PYSTATUSWORK = 'Pending-CustomerContact' and B.SBSTS_NM is like '%Setup%'";
		String pendingUWStatus = "and B.PYSTATUSWORK = 'Pending-Underwriting' and B.SBSTS_NM is like '%Sent To Underwriting%'";
		String pendingQCStatus = "and B.PYSTATUSWORK = 'Pending-QC' and B.SBSTS_NM is like '%Short Sale Approval%'";
		
		// Executing the query which is retrieved from the excel along with loan numbers
		log.info("Actual Query: " + System.lineSeparator() + ConstructedQuery);
		String LoanDetails[] = DataBase.FetchLoanFromDBForSS(ConstructedQuery);
		String runTimeQuery = ConstructedQuery;
		
		if (LoanDetails[0] == null) {
			// verify if Initial status is pending-SSIntakeReview then throw an error message
			if (SSLoanCondition.gStatus.contains("Pending-SSIntakeReview")) {
				ReportLog.reportAStep(UIControls.driver, "Fail", "Read Loans from Loan.txt", 
						"Loans not found with 'Pending-SSIntakeReview' status", false);
			}
			
			switch (SSLoanCondition.gStatus) {
			case "Pending-QC":
				runTimeQuery = runTimeQuery.replace(originalStatus, pendingUWStatus);
				log.info("UW Query is : " + runTimeQuery);
				LoanDetails = DataBase.FetchLoanFromDBForSS(runTimeQuery);
				if (LoanDetails[0] == null) {
					runTimeQuery = runTimeQuery.replace(pendingUWStatus, pendingCCStatus);
					log.info("CC Query is : " + runTimeQuery);
					LoanDetails = DataBase.FetchLoanFromDBForSS(runTimeQuery);
					if (LoanDetails[0] == null) {
						runTimeQuery = runTimeQuery.replace(pendingUWStatus, pendingCCStatus);
						log.info("QC query is : " + runTimeQuery);
						LoanDetails = DataBase.FetchLoanFromDBForSS(runTimeQuery);
						if (LoanDetails[0] == null) {
							ReportLog.reportAStep(UIControls.driver, "Fail", "Fetch Loans from Loan.txt", 
									"Unable to fetch Loans with 'Pending-SSIntakeReview' status", false);
						}
					}
				}
				break;
			
			case "Pending-Underwriting":
				runTimeQuery = runTimeQuery.replace(pendingUWStatus, pendingCCStatus);
				log.info("CC Query is : " + runTimeQuery);
				LoanDetails = DataBase.FetchLoanFromDBForSS(runTimeQuery);
				if (LoanDetails[0] == null) {
					runTimeQuery = runTimeQuery.replace(pendingUWStatus, pendingCCStatus);
					log.info("QC query is : " + runTimeQuery);
					LoanDetails = DataBase.FetchLoanFromDBForSS(runTimeQuery);
					if (LoanDetails[0] == null) {
						ReportLog.reportAStep(UIControls.driver, "Fail", "Fetch Loans from Loan.txt", 
								"Unable to fetch Loans with 'Pending-SSIntakeReview' status", false);
					}
			    }
				break;
			}
		}
			
			return LoanDetails;
		}
			
	
	
}
