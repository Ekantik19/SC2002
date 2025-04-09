package com.bto.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an HDB officer who can help with application processing and flat booking.
 * HDB Officers possess all applicant capabilities.
 */
public class HDBOfficer extends Applicant {
    private Project assignedProject;
    private String registrationStatus; // PENDING, APPROVED, REJECTED
    private List<Enquiry> handledEnquiries;
    
    // Static constants for registration status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    
    /**
     * Constructor for HDBOfficer.
     * 
     * @param userID The unique identifier for the user (NRIC)
     * @param password The user's password
     * @param age The user's age
     * @param maritalStatus The user's marital status
     */
    public HDBOfficer(String userID, String password, int age, String maritalStatus) {
        super(userID, password, age, maritalStatus);
        this.registrationStatus = null;
        this.handledEnquiries = new ArrayList<>();
    }
    
    /**
     * Register to handle a project.
     * 
     * @param project The project to register for
     * @return true if registration request is submitted, false otherwise
     */
    public boolean registerForProject(Project project) {
        // Can't register if already assigned to a project
        if (assignedProject != null) {
            return false;
        }
        
        // Can't register if already applied for the project as an applicant
        if (getCurrentApplication() != null && 
            getCurrentApplication().getProject().getProjectName().equals(project.getProjectName())) {
            return false;
        }
        
        // Check if project has available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Set the project and update status to pending
        this.assignedProject = project;
        this.registrationStatus = STATUS_PENDING;
        
        // Add officer to project's assigned officers list
        project.getAssignedOfficers().add(this);
        
        return true;
    }
    
    /**
     * View status of registration to handle a project.
     * 
     * @return The registration status, or "Not Registered" if not registered
     */
    public String viewRegistrationStatus() {
        if (registrationStatus == null) {
            return "Not Registered";
        }
        return registrationStatus;
    }
    
    /**
     * Reply to an enquiry about the assigned project.
     * 
     * @param enquiry The enquiry to respond to
     * @param response The response text
     * @return true if reply is successful, false otherwise
     */
    public boolean replyToEnquiry(Enquiry enquiry, String response) {
        // Can only reply to enquiries for the assigned project
        if (assignedProject == null || 
            !STATUS_APPROVED.equals(registrationStatus) ||
            !enquiry.getProject().getProjectName().equals(assignedProject.getProjectName())) {
            return false;
        }
        
        enquiry.addResponse(response);
        handledEnquiries.add(enquiry);
        return true;
    }
    
    /**
     * Book a flat for an applicant.
     * 
     * @param applicantID The ID of the applicant
     * @param flatType The type of flat to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(String applicantID, String flatType) {
        // Verify the officer is approved for a project
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return false;
        }
        
        // Find the application by applicant ID and project name
        Application application = null;
        
        for (Application app : assignedProject.getApplications()) {
            if (app.getApplicant().getUserID().equals(applicantID)) {
                application = app;
                break;
            }
        }
        
        if (application == null) {
            return false;
        }
        
        // Check if application status is successful
        if (!Application.STATUS_SUCCESSFUL.equals(application.getStatus())) {
            return false;
        }
        
        // Check if the flat type is valid for the project
        if (!assignedProject.hasFlatType(flatType) || assignedProject.getRemainingUnits(flatType) <= 0) {
            return false;
        }
        
        // Book the flat
        application.setFlatTypeBooked(flatType);
        
        // Decrement available units
        assignedProject.decrementUnits(flatType);
        
        // Update application status to booked
        application.updateStatus(Application.STATUS_BOOKED);
        
        // Update applicant's booked flat information
        Applicant applicant = (Applicant) application.getApplicant();
        applicant.setBookedFlatType(flatType);
        applicant.setBookedProject(assignedProject.getProjectName());
        
        return true;
    }
    
    /**
     * Generate a receipt for a flat booking.
     * 
     * @param applicantID The ID of the applicant
     * @return The receipt as a string, or null if generation fails
     */
    public String generateReceipt(String applicantID) {
        // Verify the officer is approved for a project
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return null;
        }
        
        Application application = retrieveApplication(applicantID);
        
        if (application == null || !Application.STATUS_BOOKED.equals(application.getStatus())) {
            return null;
        }
        
        // Create a receipt with the application and officer information
        Receipt receipt = new Receipt(application, this);
        return receipt.printReceipt();
    }
    
    /**
     * Retrieve an application by applicant's NRIC.
     * 
     * @param userID The applicant's NRIC
     * @return The application if found, null otherwise
     */
    public Application retrieveApplication(String userID) {
        // Can only retrieve applications for the assigned project
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return null;
        }
        
        for (Application app : assignedProject.getApplications()) {
            if (app.getApplicant().getUserID().equals(userID)) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Override the applyForProject method from Applicant.
     * HDB Officers cannot apply for projects they're handling.
     * 
     * @param project The project to apply for
     * @param flatType The type of flat to apply for
     * @return true if application is successful, false otherwise
     */
    @Override
    public boolean applyForProject(Project project, String flatType) {
        // Can't apply for the project they're handling
        if (assignedProject != null && 
            assignedProject.getProjectName().equals(project.getProjectName())) {
            return false;
        }
        
        // Call the parent method to handle application
        return super.applyForProject(project, flatType);
    }
    
    /**
     * Override viewProjects from Applicant.
     * Officers can see all projects but with more details.
     * 
     * @param filters Optional filters to apply
     * @return List of projects, including the assigned project even if visibility is off
     */
    @Override
    public List<Project> viewProjects(Map<String, Object> filters) {
        List<Project> projects = super.viewProjects(filters);
        
        // If assigned to a project, make sure it's in the list regardless of visibility
        if (assignedProject != null && !projects.contains(assignedProject)) {
            projects.add(assignedProject);
        }
        
        return projects;
    }
    
    // Getters and setters
    public Project getAssignedProject() {
        return assignedProject;
    }
    
    public void setAssignedProject(Project assignedProject) {
        this.assignedProject = assignedProject;
    }
    
    public String getRegistrationStatus() {
        return registrationStatus;
    }
    
    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
    
    public List<Enquiry> getHandledEnquiries() {
        return handledEnquiries;
    }
}