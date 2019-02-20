package com.jpmc.hlt.defaultworkflow;

import org.apache.log4j.Logger;

import com.jpmc.hlt.shortsale.SSLoanCondition;

public class LoanCondition {
	
	static Logger log = Logger.getLogger(SSLoanCondition.class);
	public static String gHostName, gPortNumber, gDatabase, gDBUserName, gDBPassword, gQuery, gSSLoanNumber, gSSClientid;
}
