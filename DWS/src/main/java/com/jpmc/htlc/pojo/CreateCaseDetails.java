package com.jpmc.htlc.pojo;

public class CreateCaseDetails {
	private String loanNumber;
	private String clientID;
	private String documentNames;
	private String documentDes;
	
	public CreateCaseDetails(String loanNumber, String clientID, String documentNames, String documentDes) {
		this.loanNumber = loanNumber;
		this.clientID = clientID;
		this.documentNames = documentNames;
		this.documentDes = documentDes;
	}
	public String getLoanNumber() {
		return loanNumber;
	}

	public void setLoanNumber(String loanNumber) {
		this.loanNumber = loanNumber;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getDocumentNames() {
		return documentNames;
	}

	public void setDocumentNames(String documentNames) {
		this.documentNames = documentNames;
	}

	public String getDocumentDes() {
		return documentDes;
	}

	public void setDocumentDes(String documentDes) {
		this.documentDes = documentDes;
	}

	@Override
	public String toString() {
		return String.format("Loan number: %s, Client ID: %s, Document Name(s): %s, Document Description(s): %s%s", 
				loanNumber, clientID, documentNames, documentDes, System.lineSeparator());
	}
	
}
