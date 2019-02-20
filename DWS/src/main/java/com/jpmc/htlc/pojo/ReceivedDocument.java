package com.jpmc.htlc.pojo;

public class ReceivedDocument {
	private String documentName;
	private String documentDes;
	private String dateReceived;
	
	public ReceivedDocument(String documentName, String documentDes, String dateReceived) {
		this.documentName = documentName;
		this.documentDes = documentDes;
		this.dateReceived = dateReceived;
	}
	public ReceivedDocument(String documentName, String documentDes) {
		this.documentName = documentName;
		this.documentDes = documentDes;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	public String getDocumentDes() {
		return documentDes;
	}
	public void setDocumentDes(String documentDes) {
		this.documentDes = documentDes;
	}
	public String getDateReceived() {
		return dateReceived;
	}
	public void setDateReceived(String dateReceived) {
		this.dateReceived = dateReceived;
	}
}
