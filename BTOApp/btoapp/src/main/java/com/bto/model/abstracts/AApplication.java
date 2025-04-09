package com.bto.model.abstracts;

import java.util.Date;

import com.bto.model.Applicant;
import com.bto.model.Project;
import com.bto.model.enums.ApplicationStatus;
import com.bto.model.enums.FlatType;

/**
 * Abstract class representing a BTO application in the system.
 * Contains common attributes and methods for all types of applications.
 * 
 * @author Your Name
 * @version 1.0
 */
public abstract class AApplication {
    
    private String applicationId;
    private Applicant applicant;
    private Project project;
    private Date applicationDate;
    private ApplicationStatus status;
    private FlatType selectedFlatType;
    private boolean withdrawalRequested;
    
    /**
     * Constructor for AApplication.
     * 
     * @param applicationId The unique identifier for the application
     * @param applicant The applicant who submitted the application
     * @param project The project being applied for
     * @param selectedFlatType The type of flat selected by the applicant
     */
    public AApplication(String applicationId, Applicant applicant, Project project, FlatType selectedFlatType) {
        this.applicationId = applicationId;
        this.applicant = applicant;
        this.project = project;
        this.applicationDate = new Date(); // Current date
        this.status = ApplicationStatus.PENDING; // Initial status
        this.selectedFlatType = selectedFlatType;
        this.withdrawalRequested = false;
    }
    
    /**
     * Approves the application, changing its status to SUCCESSFUL.
     * 
     * @return true if the application was successfully approved, false otherwise
     */
    public boolean approve() {
        if (status == ApplicationStatus.PENDING) {
            status = ApplicationStatus.SUCCESSFUL;
            return true;
        }
        return false;
    }
    
    /**
     * Rejects the application, changing its status to UNSUCCESSFUL.
     * 
     * @return true if the application was successfully rejected, false otherwise
     */
    public boolean reject() {
        if (status == ApplicationStatus.PENDING) {
            status = ApplicationStatus.UNSUCCESSFUL;
            return true;
        }
        return false;
    }
    
    /**
     * Books a flat for the application, changing its status to BOOKED.
     * 
     * @return true if the flat was successfully booked, false otherwise
     */
    public boolean bookFlat() {
        if (status == ApplicationStatus.SUCCESSFUL) {
            boolean decremented = project.decrementUnit(selectedFlatType);
            if (decremented) {
                status = ApplicationStatus.BOOKED;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Requests a withdrawal of the application.
     * 
     * @return true if the withdrawal was successfully requested, false otherwise
     */
    public boolean requestWithdrawal() {
        if (status != ApplicationStatus.UNSUCCESSFUL) {
            this.withdrawalRequested = true;
            return true;
        }
        return false;
    }
    
    /**
     * Approves a withdrawal request.
     * 
     * @return true if the withdrawal was successfully approved, false otherwise
     */
    public boolean approveWithdrawal() {
        if (withdrawalRequested) {
            // If the application is already booked, increment the unit count for that flat type
            if (status == ApplicationStatus.BOOKED) {
                // Logic to add back the unit to the project would go here
            }
            status = ApplicationStatus.UNSUCCESSFUL;
            withdrawalRequested = false;
            return true;
        }
        return false;
    }
    
    /**
     * Rejects a withdrawal request.
     * 
     * @return true if the withdrawal was successfully rejected, false otherwise
     */
    public boolean rejectWithdrawal() {
        if (withdrawalRequested) {
            withdrawalRequested = false;
            return true;
        }
        return false;
    }
    
    // Getters and Setters
    
    public String getApplicationId() {
        return applicationId;
    }
    
    public Applicant getApplicant() {
        return applicant;
    }
    
    public Project getProject() {
        return project;
    }

    /**
     * Sets the application date.
     * 
     * @param applicationDate The date when the application was submitted
     */
    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public Date getApplicationDate() {
        return applicationDate;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public FlatType getSelectedFlatType() {
        return selectedFlatType;
    }
    
    public void setSelectedFlatType(FlatType selectedFlatType) {
        this.selectedFlatType = selectedFlatType;
    }
    
    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }
}