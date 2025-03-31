package com.bto.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an HDB officer who can help with application processing and flat booking.
 */
public class HDBOfficer extends User {
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
        //DataManager dataManager = DataManager.getInstance();
        List<Application> applications = dataManager.getAllApplications();
        
        for (Application app : applications) {
            if (app.getApplicant().getUserID().equals(this.getUserID()) &&
                app.getProject().getProjectID() == project.getProjectID()) {
                return false;
            }
        }
        
        // Set the project and update status to pending
        this.assignedProject = project;
        this.registrationStatus = STATUS_PENDING;
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
        if (assignedProject == null || enquiry.getProject().getProjectID() != assignedProject.getProjectID()) {
            return false;
        }
        
        enquiry.addResponse(response);
        handledEnquiries.add(enquiry);
        return true;
    }
    
    /**
     * Update the number of flats available for a flat type.
     * 
     * @param flatType The flat type to update
     * @param newCount The new count of flats
     * @return true if update is successful, false otherwise
     */
    public boolean updateFlatAvailability(String flatType, int newCount) {
        // Can only update flat availability for the assigned project
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return false;
        }
        
        Map<String, Integer> flatTypes = assignedProject.getFlatTypes();
        if (!flatTypes.containsKey(flatType)) {
            return false;
        }
        
        flatTypes.put(flatType, newCount);
        return true;
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
     * Update the status of an application.
     * 
     * @param application The application to update
     * @param newStatus The new status to set
     * @return true if update is successful, false otherwise
     */
    public boolean updateApplicationStatus(Application application, String newStatus) {
        // Can only update applications for the assigned project
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return false;
        }
        
        if (application.getProject().getProjectID() != assignedProject.getProjectID()) {
            return false;
        }
        
        application.updateStatus(newStatus);
        return true;
    }
    
    /**
     * Generate a receipt for a flat booking.
     * 
     * @param application The application for which to generate a receipt
     * @return The generated receipt, or null if generation fails
     */
    public Receipt generateReceipt(Application application) {
        // Can only generate receipts for the assigned project and if application is booked
        if (assignedProject == null || !STATUS_APPROVED.equals(registrationStatus)) {
            return null;
        }
        
        if (application.getProject().getProjectID() != assignedProject.getProjectID() ||
            !Application.STATUS_BOOKED.equals(application.getStatus())) {
            return null;
        }
        
        return new Receipt(application, this);
    }
    
    /**
     * Implementation of viewProjects from User.
     * Officers can see all projects but with more details.
     * 
     * @param filters Optional filters to apply
     * @return List of projects, including the assigned project even if visibility is off
     */
    @Override
    public List<Project> viewProjects(Map<String, Object> filters) {
        //List<Project> allProjects = DataManager.getInstance().getAllProjects();
        List<Project> allProjects = dataManager.getAllProjects();
        
        // Filter based on visibility, but always include assigned project
        List<Project> visibleProjects = allProjects.stream()
            .filter(p -> p.isVisible() || (assignedProject != null && p.getProjectID() == assignedProject.getProjectID()))
            .collect(Collectors.toList());
        
        // Apply additional filters if provided
        if (filters != null && !filters.isEmpty()) {
            // Implementation of filters
        }
        
        return visibleProjects;
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