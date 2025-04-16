package model;

import enquiry.Enquiry;
import enquiry.EnquiryEditor;
import java.util.ArrayList;
import java.util.List;
import model.enums.ApplicationStatus;
import model.enums.UserRole;;

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
     * Retrieves an application by the applicant's NRIC.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return The application if found, null otherwise
     */
    public Application retrieveApplicationByNric(String applicantNric) {
        if (isProjectAssigned()) {
            List<Application> applications = assignedProject.getApplications();
            for (Application app : applications) {
                if (app.getApplicant().getNric().equals(applicantNric)) {
                    return app;
                }
            }
        }
        return null;
    }
    
    /**
     * Books a flat for an approved application.
     * 
     * @param application The application to book a flat for
     * @return true if the flat was successfully booked, false otherwise
     */
    public boolean bookFlat(Application application) {
        // Only officers assigned to the project can book flats
        if (!isAssignedToProject(application.getProject())) {
            return false;
        }
        
        if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
            boolean booked = application.bookFlat();
            if (booked) {
                Applicant applicant = application.getApplicant();
                applicant.setBookedFlatType(application.getSelectedFlatType());
                applicant.setBookedProject(application.getProject());
            }
            return booked;
        }
        return false;
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
     * Replies to an enquiry for the assigned project.
     * 
     * @param enquiry The enquiry to reply to
     * @param replyText The text of the reply
     * @param enquiryEditor The enquiry editor to use
     * @return true if the reply was successful, false otherwise
     */
    public boolean replyToEnquiry(Enquiry enquiry, String replyText, EnquiryEditor enquiryEditor) {
        // Officer must be assigned to the project to reply
        if (!isAssignedToProject(enquiry.getProject())) {
            return false;
        }
        
        // Use the EnquiryEditor to reply, which will handle authorization
        return enquiryEditor.reply(enquiry, replyText, getNric());
    }
    
    /**
     * Gets a list of enquiries for the assigned project.
     * 
     * @return A list of enquiries for the assigned project
     */
    public List<Enquiry> getProjectEnquiries() {
        if (isProjectAssigned()) {
            return assignedProject.getEnquiries();
        }
        return new ArrayList<>();
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
    
    public Project getAssignedProject() {
        return assignedProject;
    }
    
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
    
    /**
     * Returns a string representation of the HDB Officer.
     * 
     * @return A string with the officer's details
     */
    @Override
    public String toString() {
        String baseInfo = super.toString();
        if (assignedProject != null) {
            return baseInfo + ", Project: " + assignedProject.getProjectName() + 
                   ", Status: " + (registrationApproved ? "Approved" : "Pending");
        }
        return baseInfo + ", No Project Assigned";
    }
}