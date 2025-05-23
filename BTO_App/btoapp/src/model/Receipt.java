package model;

import java.util.Date;
import model.enums.FlatType;

/**
 * Class representing a receipt for a flat booking in the BTO Management System.
 * Generated by HDB Officers for successful flat bookings.
 * 
 * @author Your Name
 * @version 1.0
 */
public class Receipt {
    
    private String receiptId;
    private String applicationId;
    private String applicantName;
    private String applicantNric;
    private int applicantAge;
    private String maritalStatus;
    private FlatType flatType;
    private String projectName;
    private String neighborhood;
    private Date bookingDate;
    
    /**
     * Constructor for Receipt.
     * 
     * @param applicationId The ID of the application
     * @param applicantName The name of the applicant
     * @param applicantNric The NRIC of the applicant
     * @param applicantAge The age of the applicant
     * @param maritalStatus The marital status of the applicant
     * @param flatType The type of flat booked
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     */
    public Receipt(String applicationId, String applicantName, String applicantNric, 
                  int applicantAge, String maritalStatus, FlatType flatType,
                  String projectName, String neighborhood) {
        this.receiptId = "RCPT-" + applicationId + "-" + System.currentTimeMillis() % 10000;
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.applicantNric = applicantNric;
        this.applicantAge = applicantAge;
        this.maritalStatus = maritalStatus;
        this.flatType = flatType;
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.bookingDate = new Date();
    }
    
    /**
     * Generates a formatted receipt for printing.
     * 
     * @return A formatted receipt string
     */
    public String generateFormattedReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("===================================================\n");
        sb.append("            HDB FLAT BOOKING RECEIPT               \n");
        sb.append("===================================================\n\n");
        
        sb.append("Receipt ID: ").append(receiptId).append("\n");
        sb.append("Booking Date: ").append(bookingDate).append("\n\n");
        
        sb.append("APPLICANT DETAILS:\n");
        sb.append("Name: ").append(applicantName).append("\n");
        sb.append("NRIC: ").append(applicantNric).append("\n");
        sb.append("Age: ").append(applicantAge).append("\n");
        sb.append("Marital Status: ").append(maritalStatus).append("\n\n");
        
        sb.append("BOOKING DETAILS:\n");
        sb.append("Application ID: ").append(applicationId).append("\n");
        sb.append("Project: ").append(projectName).append("\n");
        sb.append("Neighborhood: ").append(neighborhood).append("\n");
        sb.append("Flat Type: ").append(flatType.getDisplayName()).append("\n\n");
        
        sb.append("===================================================\n");
        sb.append("This receipt confirms your successful flat booking.\n");
        sb.append("Please retain this receipt for your records.\n");
        sb.append("===================================================\n");
        
        return sb.toString();
    }
}