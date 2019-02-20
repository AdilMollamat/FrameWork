package com.jpmc.hlt.utils;

import java.sql.Timestamp;

public class CommonInit {

	public static final String sCDSApp = UIControls.projPath + "\\data\\CDS_DW.xls";
	public static final String sTestRegMax = UIControls.projPath + "\\data\\DW_Regression.xls";
	public static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	public static final String sTimeStamp = timestamp.toString().replace("-", "").replace(":", "_").replace(" ", "_").replace(".", "_");
	public static final String projPath = ".";
	public static String sStrCaseID; 
	public static String gStrLoanNumber; 
}
