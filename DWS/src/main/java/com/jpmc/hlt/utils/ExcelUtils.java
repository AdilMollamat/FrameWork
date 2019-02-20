package com.jpmc.hlt.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.*;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.jpmc.htlc.pojo.CreateCaseDetails;
import com.relevantcodes.extentreports.LogStatus;

import gherkin.lexer.Sr_cyrl;

public class ExcelUtils {

	static Logger log = Logger.getLogger(ExcelUtils.class);
	private static HSSFSheet ExcelWSheet;
	private static HSSFWorkbook ExcelWBook;
	private static HSSFCell Cell;
	private static FileInputStream inputFile;
	//private static HSSFRow ROW;
	public static String ResultFolderName;
	public static final String sCDSApp = "./data/CDS_DW.xls";
	public static final String sTestCasesSheet = "./data/Testcases.xls";
	public static final String sTestRegMax = "./data/DW_Regression.xls";
	public static final String sTestSSMax = "./data/SS_Regression.xls";
	public static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	public static final String sTimeStamp = timestamp.toString().replace("-", "").replace(":", "_").replace(" ", "_")
			.replace(".", "_");
	
	public static String getCellData(String sFilePath, String sSheetName, int RowNum, int ColNum) {
		Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		// log.info(Cell);
		switch(Cell.getCellTypeEnum()) {
		case STRING:
			return Cell.getStringCellValue();
		case NUMERIC:
			return Double.toString(Cell.getNumericCellValue());
		case BLANK:
		default:
			log.info("This Cell is blank");
			return "";
		}
	}
	
	public static int getTotalTestDataRows(String sFilePath, String sSheetName) {
		return ExcelWSheet.getLastRowNum();
	}
	
	public static int getTotalTestDataColumns(String sFilePath, String sSheetName) {
		return ExcelWSheet.getRow(0).getPhysicalNumberOfCells();
	}
	
	public static String getAppUrl(String sAppName, String sEnv) {
		String sRowVal = null;
		String sUrl = null;
		if (sEnv.equalsIgnoreCase("QA3")) {
			sRowVal = "";
		} else if (sEnv.equalsIgnoreCase("DEV2")) {
			sRowVal = "";
		}
		int iRowCount;
		try {
			inputFile = new FileInputStream(sCDSApp);
			ExcelWBook = new HSSFWorkbook(inputFile);
			ExcelWSheet = ExcelWBook.getSheet("Urls");
			iRowCount = getTotalTestDataRows(sCDSApp, "Urls");
			for (int i = 1; i <= iRowCount; i++) {
				String sNameRow = getCellData(sCDSApp, "Urls", i, 0);
				if (sRowVal == null) {
					log.error("App url not found with app name: " + sAppName + " and environment " + sEnv);
				} else if (sNameRow.equalsIgnoreCase(sRowVal)) {
					sUrl = getCellData(sCDSApp, "Urls", i, 1);
					break;
				}
			}
		} catch (Exception e) {
			log.error("Exception: " + System.lineSeparator(), e);
		}
		return sUrl;
	}
	
	public static String getUserCredentials(String sRole) {
		String sCredentials = null;
		String sSid = null;
		String sPwd = null;
		int iRowCount;
		try {
			inputFile = new FileInputStream(sCDSApp);
			ExcelWBook = new HSSFWorkbook(inputFile);
			ExcelWSheet = ExcelWBook.getSheet("Credentials");
			iRowCount = getTotalTestDataRows(sCDSApp, "Credentials");
			for (int i = 1; i <= iRowCount; i++) {
				String sRoleName = getCellData(sCDSApp, "Credentials", i, 0);
				if (sRoleName.equalsIgnoreCase(sRole)) {
					sSid = getCellData(sCDSApp, "Credentials", i, 1);
					sPwd = getCellData(sCDSApp, "Credentials", i, 2);
					break;
				}
				
			}
			sCredentials = sSid + ":" + sPwd;
			return sCredentials;
			
		}  catch (Exception e) {
			log.error("Exception: " + System.lineSeparator(), e);
			return "";
		}
	}
	
	public static List<String> getTestCasesExecutionRows(){
		int iRowCount;
		List<String> arrData = new ArrayList<String>();
		try {
			inputFile = new FileInputStream(sTestCasesSheet);
			ExcelWBook = new HSSFWorkbook(inputFile);
			ExcelWSheet = ExcelWBook.getSheet("TestCases");
			iRowCount = getTotalTestDataRows(sTestCasesSheet, "TestCases");
			log.info("Total TestData Rows : " + iRowCount);
			for (int i = 1; i <= iRowCount; i++) {
				String sExecutionFlag = getCellData(sTestCasesSheet, "TestCases", i, 1);
				if (sExecutionFlag.equalsIgnoreCase("Y")) {
					String sTestCaseID = getCellData(sTestCasesSheet, "TestCases", i, 0);
					String sModuleName = getCellData(sTestCasesSheet, "TestCases", i, 2);
					String sTestSetName = getCellData(sTestCasesSheet, "TestCases", i, 3);
					String sTestCaseName = getCellData(sTestCasesSheet, "TestCases", i, 4);
					String sEnv = getCellData(sTestCasesSheet, "TestCases", i, 5);
					
					String sRowData = sTestCaseID + ":" + sModuleName + ":" + sTestCaseName + ":" + sTestSetName + ":" + sEnv;
					arrData.add(sRowData);
				}
			}
		} catch (Exception e) {
			log.error("Exception: " + System.lineSeparator(), e);
		}
		return arrData;
	}
	
	public static String getCellValWithRowColName(String sFilePath, String sSheetName, String sRowName, String sColName) {
		int iRowCount;
		int iColCount;
		String sCellVal = null;
		String sCol_Name = null;
		try {
			inputFile = new FileInputStream(sFilePath);
			ExcelWBook = new HSSFWorkbook(inputFile);
			ExcelWSheet = ExcelWBook.getSheet(sSheetName);
			iRowCount = getTotalTestDataRows(sFilePath, sSheetName);
			//log.info("Total TestData Rows : " + iRowCount);
			for (int i = 0; i <= iRowCount; i++) {
				String sRow_Name = getCellData(sFilePath, sSheetName, i, 0);
				if (sRow_Name.equalsIgnoreCase(sRowName)) {
					iColCount = getTotalTestDataColumns(sFilePath, sSheetName);
					for (int j = 0; j <= iColCount; j++) {
						sCol_Name = getCellData(sFilePath, sSheetName, 0, j);
						if (sCol_Name.equalsIgnoreCase(sColName)) {
							sCellVal = getCellData(sFilePath, sSheetName, i, j);
							break;
						} else {
							if (i == iColCount) {
								ReportLog.extLog.log(LogStatus.FAIL, sColName + " - not found in sheet -" + sSheetName);
								log.error(sColName + " - not found in sheet -" + sSheetName);
								break;
							}
						}
					}
				} else {
					if (i == iRowCount) {
						ReportLog.extLog.log(LogStatus.FAIL, sRowName + " - not found in sheet -" + sSheetName);
						log.error(sRowName + " - not found in sheet -" + sSheetName);
						break;
					}
				}
				if (sRow_Name.equalsIgnoreCase(sRowName)) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("Exception: " + System.lineSeparator(), e);
		}
		return sCellVal;
	}

	
	public static FileInputStream getFileInputStream(String filepath) {
		String strFilePath = filepath;
		try {
			return (new FileInputStream(strFilePath));
		} catch (FileNotFoundException e) {
			log.error("Exception: " + System.lineSeparator());
			return null;
		}
	}
	
	public static String[][] readExcel_DocLoadInput(){
		String [][] str = null;
		FileInputStream file = getFileInputStream(UIControls.projPath + "\\data\\DocLoadInputs.xls");
		try (Workbook wb = WorkbookFactory.create(file)){
			Sheet sh = wb.getSheet("Sheet1");
			int rowcount = sh.getLastRowNum() + 1;
			int noOfColumns = sh.getRow(0).getLastCellNum();
			str = new String[noOfColumns][rowcount];
			for (int r = 1; r <= rowcount; r++) {
				Row row = sh.getRow(r);
				for (int i = 0; i < noOfColumns; i++) {
					str[i][r - 1] = new DataFormatter().formatCellValue(row.getCell(i));
				}
			}
		} catch (FileNotFoundException e) {
			log.error("Exception: " + System.lineSeparator());
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (EncryptedDocumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvalidFormatException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return str;
	}
	
	public static List<CreateCaseDetails> getCaseCreationDetails(){
		List<CreateCaseDetails> caseCreationDetails = new ArrayList<>();
		
		try (HSSFWorkbook excelWorkBook = new HSSFWorkbook(Files.newInputStream(Paths.get("./data/SS_Regression.xls")))){
			HSSFSheet caseCreationDetailsSheet = excelWorkBook.getSheet("caseCreationDetails");
			
			int numberOfRowsInSheet = caseCreationDetailsSheet.getLastRowNum();
			HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
			
			for (int i = 0; i < numberOfRowsInSheet; i++) {
				HSSFRow RowName = caseCreationDetailsSheet.getRow(i + 1);
				String loanNumber = dataFormatter.formatCellValue(RowName.getCell(0));
				String clientID = RowName.getCell(1).toString();
				String documentNames = RowName.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).toString();
				String documentDes = RowName.getCell(3, MissingCellPolicy.CREATE_NULL_AS_BLANK).toString();
				
				caseCreationDetails.add(new CreateCaseDetails(loanNumber, clientID, documentNames, documentDes));
			}
			
		} catch (IOException ioe) {
			log.info("IOException:" + ioe);
		}
		return caseCreationDetails;
	}
	
	public static void saveCreatedCaseIDs(List<String> caseIDs) {
		try (HSSFWorkbook excelWorkBook = new HSSFWorkbook(Files.newInputStream(Paths.get("./data/SS_Regression.xls")))){
			HSSFSheet caseCreationDetailsSheet = excelWorkBook.getSheet("caseCreationDetails");
			int numberOfRowsInSheet = caseCreationDetailsSheet.getLastRowNum();
			
			for (int i = 0; i < numberOfRowsInSheet; i++) {
				HSSFRow RowName = caseCreationDetailsSheet.getRow(i);
				HSSFCell caseIdCell = RowName.getCell(4, MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String caseID = caseIDs.get(i - 1);
				
				caseIdCell.setCellValue(caseID);
			}
			
			excelWorkBook.write(Files.newOutputStream(Paths.get("./data/SS_Regression.xls")));
			
		} catch (IOException ioe) {
			log.info("IOException:" + ioe);
		}
	}
	
}
