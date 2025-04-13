package model.enums;

/**
 * Enumeration for the status of BTO applications.
 * Represents the different states an application can be in during its lifecycle.
 * 
 * @author Your Name
 * @version 1.0
 */
public enum ApplicationStatus {
    /**
     * Initial status when application is first submitted.
     * No conclusive decision has been made yet.
     */
    PENDING("Pending"),
    
    /**
     * Application has been accepted.
     * Applicant is invited to make a flat booking with the HDB Officer.
     */
    SUCCESSFUL("Successful"),
    
    /**
     * Application has been rejected.
     * Applicant may apply for another project.
     */
    UNSUCCESSFUL("Unsuccessful"),
    
    /**
     * Flat booking has been completed after a successful application.
     * Applicant has secured a unit.
     */
    BOOKED("Booked");
    
    private final String displayName;
    
    /**
     * Constructor for ApplicationStatus enum.
     * 
     * @param displayName The human-readable name to display for this status
     */
    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the application status.
     * 
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns the string representation of this enum value.
     * 
     * @return The display name of this status
     */
    @Override
    public String toString() {
        return displayName;
    }
}