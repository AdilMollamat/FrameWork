package com.jpmc.hlt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.chart.LineFormatRecord;

import com.jpmc.hlt.defaultworkflow.LoanCondition;
import com.jpmc.hlt.shortsale.SSLoanCondition;

import edu.emory.mathcs.backport.java.util.Arrays;

public class DataBase {

	static Logger log = Logger.getLogger(DataBase.class);
	
	private DataBase() {
		throw new IllegalStateException("Utility class");
	}
	
	
	public static String AppendMSPCurrentTableInQuery(String Query) {
		String currentTableQuery = "Select CurrentFLG, Processname from modprtmsp.fulfillment_current_table where Processname = "
				+ "'FULFULLMENT_MSPDATA'";
		String currentTable[] = FetchLoanFromDB(currentTableQuery);
		String REGEX = "MODPRTMSP.FULFILLMENT_MSPDATA_(A|B)";
		String REPLACE = "MODPRTMSP.FULFILLMENT_MSPDATA_" + currentTable[0];
		Pattern p = Pattern.compile(REGEX);
		
		// get a matcher object
		Matcher m = p.matcher(Query);
		StringBuffer updatedQuery = new StringBuffer();
		
		while (m.find()) {
			m.appendReplacement(updatedQuery, REPLACE);
		}
		m.appendTail(updatedQuery);
		log.info("Query Used: " + System.lineSeparator() + updatedQuery.toString());
		
		return updatedQuery.toString();
		
	}
	
	public static String AppendLoanNumberInQuery(String Query) {
		String[] tempQuery;
		String queryWithLoans = "";
		
		tempQuery = Query.split("Where");
		
		// Readfile is used to read the loans from Loan.txt file
		String loanData = readFile("loan");
		
		// Construct required query
		queryWithLoans = tempQuery[0] + "Where A.LOANNUMBER IN " + loanData + " AND " + tempQuery[1];
		
		return queryWithLoans;
		
	}
	
	public static String readFile(String fileName) {
		
		try(BufferedReader br = Files.newBufferedReader(Paths.get("./data/" + fileName + ".txt"))) {
			return br.lines().filter(
					lineFromFile -> (!lineFromFile.isEmpty() && lineFromFile.chars().allMatch(Character::isDigit)))
					.map(loanNumber -> "'" + loanNumber + "'").collect(Collectors.joining(",", "(", ")"));
					
		} catch (IOException ioe) {
			throw new UncheckedIOException("Error" + fileName + ".txt", ioe);
		}
	}
	
	
	/**
	 * Method Name:
	 * 				This method will fetch 2 column values mentioned in query
	 * @author    : 
	 * @param     : Query
	 * @return    : String Array
	 * 
	 * Example    :
	 */
	
	public static String[] FetchLoanFromDB(String Query) {
		// DB2 data base credentials
		String jdbcClassName = "com.ibm.db2.jcc.DB2Driver";
		
		if (LoanCondition.gHostName == null) {
			LoanCondition.gHostName = Objects.requireNonNull(SSLoanCondition.gHostName);
		}
		if (LoanCondition.gPortNumber == null) {
			LoanCondition.gPortNumber = Objects.requireNonNull(SSLoanCondition.gPortNumber);
		}
		if (LoanCondition.gDatabase == null) {
			LoanCondition.gDatabase = Objects.requireNonNull(SSLoanCondition.gDatabase);
		}
		if (LoanCondition.gDBUserName == null) {
			LoanCondition.gDBUserName = Objects.requireNonNull(SSLoanCondition.gDBUserName);
		}
		if (LoanCondition.gDBPassword == null) {
			LoanCondition.gDBPassword = Objects.requireNonNull(SSLoanCondition.gDBPassword);
		}
		
		String url = String.format("jdbc:db2://%s:%d/%s", LoanCondition.gHostName, 
				Double.valueOf(LoanCondition.gPortNumber).intValue(), LoanCondition.gDatabase);
		
		// Create variable to do db2 connection
		Connection connection = null;
		Statement pstmt = null;
		ResultSet rset = null;
		boolean found = false;
		String value[] = new String[10];
		
		try {
			// Load class into memory
			Class.forName(jdbcClassName);
			
			// Establish connection
			log.info("url=" + url);
			connection = getDBConnection(url, LoanCondition.gDBUserName, LoanCondition.gDBPassword);
			
			if (connection == null) {
				log.error("DB2 connection Failed");
			} else {
				log.info("Connected Successfully.");
				
				pstmt = createStatement(connection);
				rset = executeQuery(pstmt, Query);
				log.info("Rset Value is :" + rset);
				
				int i = 0;
				while (rset.next()) {
					found = true;
					value[i] = rset.getString(1); // first column
					value[i + 1] = rset.getString(2); // second column
					log.info("Value:" + value[i + 1]);					
				}
				
				if (!found) {
					log.error("No Information Found");
				}
				
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO: handle exception
			log.error("Exception" + System.lineSeparator(), e);
		} finally {
			if (connection == null) {
				log.error("DB2 connection Failed");
			}
		}
		return value;
	}
	
	@java.lang.SuppressWarnings("squid: S106")
	public static String[] FetchLoanFromDBForSS(String Query) {
		// DB2 data base credentials
		String jdbcClassName = "com.ibm.db2.jcc.DB2Driver";
		int portNumber = Double.valueOf(SSLoanCondition.gPortNumber).intValue();
		
		String url = String.format("jdbc:db2://%s:%d/%s", SSLoanCondition.gHostName, portNumber, SSLoanCondition.gDatabase);
		
		// Create variable to do db2 connection
		Connection connection = null;
		Statement pstmt = null;
		ResultSet rset = null;
		boolean found = false;
		String value[] = new String[10];
		
		try {
			// Load class into memory
			Class.forName(jdbcClassName);
			
			// Establish connection
			log.info("url=" + url);
			connection = getDBConnection(url, SSLoanCondition.gDBUserName, SSLoanCondition.gDBPassword);
			
			if (connection == null) {
				log.error("DB2 connection Failed");
			} else {
				log.info("Connected Successfully.");
				
				pstmt = createStatement(connection);
				rset = executeQuery(pstmt, Query);
				log.info("Rset Value is :" + rset);
				
				int i = 0;
				while (rset.next()) {
					found = true;
					value[i] = rset.getString(1); // first column
					value[i + 1] = rset.getString(2); // second column
					value[i + 2] = rset.getString(3); // Third column
					log.info("Rset Value:" + value[i + 1]);					
				}
				
				if (!found) {
					log.error("No Information Found");
				}
				
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO: handle exception
			log.error("Exception" + System.lineSeparator(), e);
		} finally {
			if (connection == null) {
				log.error("DB2 connection Failed");
			}
		}
		log.info("Values Captured from DB:");
		Arrays.asList(value).stream().forEach(System.out::println);
		return value;
		
	}
	
	
	/**
	 * Method Name:
	 * 				This method returns a new database connection for the given file path.
	 * 				Connections, Streams, Files and other classes that implement the Closeable
	 * 				interface or its super-interface, AutoCloseable, needs to be closed after use
	 * @author    : 
	 * @param     : URL, UserName, Password
	 * @return    : 
	 * 
	 * Example    :
	 */
	public static Connection getDBConnection(String url, String username, String password) {
		String strURL = url;
		String strUsername = username;
		String strPassword = password;
		
		try {
			return DriverManager.getConnection(strURL, strUsername, strPassword);
		} catch (SQLException e) {
			log.error("Exception:" + System.lineSeparator(), e);
			return null;
		}	
	}
	
	
	public static Statement createStatement(Connection connection) {
		try {
			return connection.createStatement();
		} catch (SQLException e) {
			log.error("Exception:" + System.lineSeparator(), e);
			return null;
		}
	}
	
	
	public static ResultSet executeQuery(Statement dbStatement, String dbQuery) {
		try {
			return dbStatement.executeQuery(dbQuery);
		} catch (SQLException e) {
			log.error("Exception:" + System.lineSeparator(), e);
			return null;
		}
	}
	
	
	public static String[] FetchLoanNumberAndClientIDFromDB() {
		String tmpQuery = DataBase.AppendMSPCurrentTableInQuery(SSLoanCondition.gQuery);
		String constructedQuery = AppendLoanNumberInQuery(tmpQuery);
		
		log.info("Query after replacing actual MSP table value " + constructedQuery);
		String loanDetails[] = FetchLoanFromDBForSS(constructedQuery);
		UIControls.delay(5);
		SSLoanCondition.gSSLoanNumber = loanDetails[0];
		SSLoanCondition.gSSClientid = loanDetails[1];
		return loanDetails;
	}
	
	public static String[][] GetLoanFromDB(String DB, String Query, String column1, String column2){
		String Host = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB2_Connection", DB, "Host Name");
		String Port = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB2_Connection", DB, "Port");
		String Database = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB2_Connection", DB, "Database");
		String Username = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB2_Connection", DB, "Username");
		String Password = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB2_Connection", DB, "Password");
		
		log.info("**************" + Port);
		Double P = Double.parseDouble(Port);
		Port = new DecimalFormat("#").format(P);
		// DB2 dev2 data base credentials
		String jdbcClassName = "com.ibm.db2.jcc.DB2Driver"; 
		String url = "jdbc:db2://" + Host + ":" + Port + "/" + Database;
		
		// Create variables to do db2 connection
		Connection connection = null;
		Statement pstmt = null;
		ResultSet rset = null;
		boolean found = false;
		String value[][] = new String[1000][5];
		
		
		try {
			// Load class into memory
			Class.forName(jdbcClassName);
			
			// Establish connection
			connection = DriverManager.getConnection(url, Username, Password);
			
			if (connection == null) {
				log.info("Connected Successfully.");
			} else {
				log.error("DB2 connection Failed");
				
				// Create statement
				pstmt = createStatement(connection);
				
				// get the composed query using getLoanQuery method
				String qr;
				if (Query.contains("select"))
					qr = Query;
				else
					qr = getLoanQuery(Query);
				
				// Execute the aqquired query
				rset = pstmt.executeQuery(qr);
				
				int i = 0;
				while (rset.next()) {
					found = true;
					value[i][0] = rset.getString(column1); 
					value[i][1] = rset.getString(column2);
					i = i + 1;
					log.info("Value:" + value[i][1]);					
				}
				
				if (!found) {
					log.error("No Information Found");
				}
				
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO: handle exception
			log.error("Exception" + System.lineSeparator(), e);
		} finally {
			if (connection != null) {
			} else {
				log.error("DB2 connection Failed");
			}
		}
		return value;
	}
	
	public static String getLoanQuery(String type) {
		// get the query from data.xlsx file
		String st = ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestSSMax, "DB_Queries", type, "query");
		String[] temp;
		String que = "";
		if (type == "fetch") {
			String delimeter = "and B.PYSTATUSWORK";
			temp = st.split(delimeter);
			// readfile is used to read the loans from loans file
			String loan = DataBase.readFile("Loan");
			// create the required query
			que = temp[0] + "and B.LOANNUMBER in " + loan + delimeter + temp[1];
			// return the created query
		} else if (type == "put") {
			log.info("given the query");
			que = st;
		}
		return que;
		
		
	}
	
	
}
