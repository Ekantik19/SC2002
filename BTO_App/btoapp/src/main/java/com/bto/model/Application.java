package com.bto.model;

import java.util.Date;

/**
 * Represents a BTO application submitted by an applicant.
 */
public class Application {
    private int applicationID;
    private Applicant applicant;
    private Project project;
    private String status; // PENDING, SUCCESSFUL, UNSUCCESSFUL, BOOKED
    private String flatTypeBooked;
    private Date applicationDate;
    private boolean withdrawalRequested;
    
    // Static constants for application status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESSFUL = "SUCCESSFUL";
    public static final String STATUS_UNSUCCESSFUL = "UNSUCCESSFUL";
    public static final String STATUS_BOOKED = "BOOKED";
    
    /**
     * Constructor for Application.
     * 
     * @param applicant The applicant submitting the application
     * @param project The project being applied for
     */
    public Application(Applicant applicant, Project project) {
        this.applicant = applicant;
        this.project = project;
        this.status = STATUS_PENDING;
        this.applicationDate = new Date();
        this.withdrawalRequested = false;
        
        // Generate application ID (in a real system this would be more sophisticated)
        this.applicationID = (int) (Math.random() * 10000);
        
        // Add this application to the project
        project.addApplication(this);
    }
    
    /**
     * Update the status of the application.
     * 
     * @param newStatus The new status to set
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
    
    /**
     * Book a flat for this application.
     * 
     * @param flatType The type of flat to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(String flatType) {
        // Can only book if application is successful
        if (!STATUS_SUCCESSFUL.equals(status)) {
            return false;
        }
        
        // Check if project has this flat type available
        if (!project.hasFlatType(flatType)) {
            return false;
        }
        
        // Decrement available units
        boolean decremented = project.decrementUnits(flatType);
        if (!decremented) {
            return false;
        }
        
        // Update application
        this.flatTypeBooked = flatType;
        this.status = STATUS_BOOKED;
        return true;
    }
    
    // Getters and setters
    public int getApplicationID() {
        return applicationID;
    }
    
    public Applicant getApplicant() {
        return applicant;
    }
    
    public Project getProject() {
        return project;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getFlatTypeBooked() {
        return flatTypeBooked;
    }
    
    public Date getApplicationDate() {
        return applicationDate;
    }
    
    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }
    
    public void setWithdrawalRequested(boolean withdrawalRequested) {
        this.withdrawalRequested = withdrawalRequested;
    }

        /**
     * Get the type of flat for this application.
     * This is an alias for getFlatTypeBooked() for API consistency.
     * 
     * @return The flat type for this application
     */
    public String getFlatType() {
        return getFlatTypeBooked();
    }
}