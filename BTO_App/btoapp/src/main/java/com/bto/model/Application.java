package com.bto.model;

import java.util.Date;

/**
 * Represents a BTO application submitted by an applicant.
 */
public class Application {
    private Applicant applicant;
    private Project project;
    private String status; // PENDING, SUCCESSFUL, UNSUCCESSFUL, BOOKED
    private String flatType; // The type of flat applied for
    private String flatTypeBooked; // The type of flat booked (after approval)
    private Date applicationDate;
    private boolean withdrawalRequested;
    
    // Static constants for application status
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_SUCCESSFUL = "Successful";
    public static final String STATUS_UNSUCCESSFUL = "Unsuccessful";
    public static final String STATUS_BOOKED = "Booked";
    
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
    public Applicant getApplicant() {
        return applicant;
    }
    /**
     * Set the application date.
     * 
     * @param applicationDate The date to set
     */
    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
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

    public void setFlatTypeBooked(String flatType) {
        this.flatTypeBooked = flatType;
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
     * Get the type of flat applied for in this application.
     * 
     * @return The flat type for this application
     */
    public String getFlatType() {
        return flatType;
    }
    
    /**
     * Set the type of flat for this application.
     * 
     * @param flatType The flat type to set
     */
    public void setFlatType(String flatType) {
        this.flatType = flatType;
    }

    /**
     * Check if the application is eligible for booking.
     * 
     * @return true if application can be booked, false otherwise
     */
    public boolean isEligibleForBooking() {
        return STATUS_SUCCESSFUL.equals(status) && 
            flatTypeBooked == null;
    }

    /**
     * Validate flat type against project availability.
     * 
     * @param flatType The flat type to validate
     * @return true if flat type is valid and available, false otherwise
     */
    public boolean validateFlatType(String flatType) {
        return project.hasFlatType(flatType) && 
            project.getRemainingUnits(flatType) > 0;
    }
}