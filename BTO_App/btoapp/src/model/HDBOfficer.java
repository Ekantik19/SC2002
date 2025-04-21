package model;

import model.enums.ApplicationStatus;
import model.enums.UserRole;
;

/**
 * Class representing an HDB Officer in the BTO Management System.
 * Extends the Applicant class, inheriting all applicant capabilities.
 * 
 * @author Your Name
 * @version 1.0
 */
public class HDBOfficer extends Applicant {
    
    private Project assignedProject;
    private boolean registrationApproved;
    
    /**
     * Constructor for HDBOfficer.
     * 
     * @param name The name of the officer
     * @param nric The NRIC of the officer
     * @param age The age of the officer
     * @param maritalStatus The marital status of the officer
     * @param password The officer's password
     */
    public HDBOfficer(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.registrationApproved = false;
        // Override the role set in the parent constructor
        setRole(UserRole.OFFICER);
    }
    
    /**
     * Registers to handle a project.
     * 
     * @param project The project to register for
     * @return true if registration was successful, false otherwise
     */
    public boolean registerForProject(Project project) {
        // Check if already registered or approved for a project
        if (assignedProject != null) {
            return false;
        }
        
        // Check if already applied for this project as an Applicant
        if (getCurrentApplication() != null && 
            getCurrentApplication().getProject().getProjectName().equals(project.getProjectName())) {
            return false;
        }
        
        // Check if the project has available officer slots
        if (project.getRemainingOfficerSlots() <= 0) {
            return false;
        }
        
        // Set as pending registration
        this.assignedProject = project;
        this.registrationApproved = false;
        
        return true;
    }
    
    /**
     * Generates a receipt for a flat booking.
     * 
     * @param application The application to generate a receipt for
     * @return The generated receipt, or null if not authorized
     */
    public Receipt generateBookingReceipt(Application application) {
        if (isAssignedToProject(application.getProject()) && 
            application.getStatus() == ApplicationStatus.BOOKED) {
            Applicant applicant = application.getApplicant();
            Project project = application.getProject();
            
            return new Receipt(
                application.getApplicationId(),
                applicant.getName(),
                applicant.getNric(),
                applicant.getAge(),
                applicant.getMaritalStatus(),
                application.getSelectedFlatType(),
                project.getProjectName(),
                project.getNeighborhood()
            );
        }
        return null;
    }
    
    /**
     * Checks if the officer is assigned to a specific project.
     * 
     * @param project The project to check
     * @return true if the officer is assigned to the project, false otherwise
     */
    public boolean isAssignedToProject(Project project) {
        return assignedProject != null && 
               assignedProject.getProjectName().equals(project.getProjectName()) && 
               registrationApproved;
    }
    
    /**
     * Checks if the officer is assigned to any project.
     * 
     * @return true if assigned to a project, false otherwise
     */
    public boolean isProjectAssigned() {
        return assignedProject != null && registrationApproved;
    }
    
    /**
     * Internal method to approve registration (called by HDBManager).
     * 
     * @return true if approval was successful, false otherwise
     */
    protected boolean approveRegistration() {
        if (assignedProject != null && !registrationApproved) {
            registrationApproved = true;
            assignedProject.addOfficer(this);
            return true;
        }
        return false;
    }
    
    /**
     * Internal method to reject registration (called by HDBManager).
     * 
     * @return true if rejection was successful, false otherwise
     */
    protected boolean rejectRegistration() {
        if (assignedProject != null && !registrationApproved) {
            assignedProject = null;
            return true;
        }
        return false;
    }
    
    // Getters
    
    /**
     * Gets the project assigned to this officer.
     * 
     * @return The assigned project, or null if not assigned.
     */
    public Project getAssignedProject() {
        return assignedProject;
    }

     /**
     * Checks if the officer's registration has been approved.
     * 
     * @return true if registration is approved, false otherwise.
     */
    public boolean isRegistrationApproved() {
        return registrationApproved;
    }

    /**
     * Sets the assigned project for this officer.
     * For use during system initialization.
     * 
     * @param project The project to assign to this officer
     */
    public void setAssignedProject(Project project) {
        this.assignedProject = project;
    }

    /**
     * Sets the registration approval status.
     * For use during system initialization.
     * 
     * @param approved The approval status
     */
    public void setRegistrationApproved(boolean approved) {
        this.registrationApproved = approved;
    }

}