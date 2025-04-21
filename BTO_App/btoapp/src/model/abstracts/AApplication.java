package model.abstracts;

import java.util.Date;
import model.Applicant;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

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
        // Only change status if it's currently PENDING
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
        // Only change status if the application is already SUCCESSFUL
        if (status == ApplicationStatus.SUCCESSFUL) {
            status = ApplicationStatus.BOOKED;
            return true;
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
    
    /**
     * Returns the unique identifier for the application.
     * 
     * @return the applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Returns the applicant who submitted the application.
     * 
     * @return the applicant
     */
    public Applicant getApplicant() {
        return applicant;
    }
    
    /**
     * Returns the project being applied for.
     * 
     * @return the project
     */
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

     /**
     * Returns the date when the application was submitted.
     * 
     * @return the applicationDate
     */
    public Date getApplicationDate() {
        return applicationDate;
    }

    /**
     * Returns the current status of the application.
     * 
     * @return the status
     */
    public ApplicationStatus getStatus() {
        return status;
    }

    /**
     * Sets the application status.
     * 
     * @param status The new status for the application
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    /**
     * Returns the type of flat selected by the applicant.
     * 
     * @return the selectedFlatType
     */
    public FlatType getSelectedFlatType() {
        return selectedFlatType;
    }

    /**
     * Sets the selected flat type for the application.
     * 
     * @param selectedFlatType The flat type selected by the applicant
     */
    public void setSelectedFlatType(FlatType selectedFlatType) {
        this.selectedFlatType = selectedFlatType;
    }
    
    /**
     * Checks if a withdrawal has been requested for this application.
     * 
     * @return true if a withdrawal has been requested, false otherwise
     */
    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }
}