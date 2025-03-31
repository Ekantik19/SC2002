package com.bto.model;

import java.util.Date;

/**
 * Represents a receipt for a flat booking.
 */
public class Receipt {
    private static int nextId = 1;
    
    private int receiptID;
    private Application application;
    private HDBOfficer officer;
    private Date generationDate;
    
    /**
     * Constructor for Receipt.
     * 
     * @param application The application for which the receipt is generated
     * @param officer The officer who generated the receipt
     */
    public Receipt(Application application, HDBOfficer officer) {
        this.receiptID = nextId++;
        this.application = application;
        this.officer = officer;
        this.generationDate = new Date();
    }
    
    /**
     * Generate a formatted receipt string.
     * 
     * @return The formatted receipt
     */
    public String printReceipt() {
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("=================================================\n");
        receipt.append("                  BOOKING RECEIPT                 \n");
        receipt.append("=================================================\n");
        receipt.append("Receipt ID: ").append(receiptID).append("\n");
        receipt.append("Date: ").append(generationDate).append("\n\n");
        
        receipt.append("Applicant Details:\n");
        receipt.append("Name: ").append(application.getApplicant().getUserID()).append("\n");
        receipt.append("NRIC: ").append(application.getApplicant().getUserID()).append("\n");
        receipt.append("Age: ").append(application.getApplicant().getAge()).append("\n");
        receipt.append("Marital Status: ").append(application.getApplicant().getMaritalStatus()).append("\n\n");
        
        receipt.append("Project Details:\n");
        receipt.append("Project Name: ").append(application.getProject().getProjectName()).append("\n");
        receipt.append("Neighborhood: ").append(application.getProject().getNeighborhood()).append("\n");
        receipt.append("Flat Type Booked: ").append(application.getFlatTypeBooked()).append("\n\n");
        
        receipt.append("Officer Details:\n");
        receipt.append("Officer ID: ").append(officer.getUserID()).append("\n\n");
        
        receipt.append("=================================================\n");
        receipt.append("Thank you for your booking. This receipt serves as proof of your flat booking.\n");
        receipt.append("=================================================\n");
        
        return receipt.toString();
    }
    
    // Getters
    public int getReceiptID() {
        return receiptID;
    }
    
    public Application getApplication() {
        return application;
    }
    
    public HDBOfficer getOfficer() {
        return officer;
    }
    
    public Date getGenerationDate() {
        return generationDate;
    }
}