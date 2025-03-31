package com.bto.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an applicant who can apply for BTO projects.
 */
public class Applicant extends User {
    private Application currentApplication;
    private List<Enquiry> enquiries;
    
    /**
     * Constructor for Applicant.
     * @param userID The unique identifier for the user (NRIC)
     * @param password The user's password
     * @param age The user's age
     * @param maritalStatus The user's marital status
     */
    public Applicant(String userID, String password, int age, String maritalStatus) {
        super(userID, password, age, maritalStatus);
        this.enquiries = new ArrayList<>();
    }
    
    /**
     * Apply for a BTO project.
     * 
     * @param project The project to apply for
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(Project project) {
        // Check if already has an application
        if (currentApplication != null) {
            return false;
        }
        
        // Check eligibility based on age and marital status
        boolean eligible = isEligibleForProject(project);
        if (!eligible) {
            return false;
        }
        
        // Create new application
        currentApplication = new Application(this, project);
        return true;
    }
    
    /**
     * Check if the applicant is eligible for the project based on age and marital status.
     * 
     * @param project The project to check eligibility for
     * @return true if eligible, false otherwise
     */
    private boolean isEligibleForProject(Project project) {
        // Singles must be 35 or older and can only apply for 2-Room
        if ("Single".equals(getMaritalStatus())) {
            if (getAge() < 35) {
                return false;
            }
            
            // Check if project has 2-Room flats
            return project.hasFlatType("2-Room");
        }
        // Married must be 21 or older
        else if ("Married".equals(getMaritalStatus())) {
            if (getAge() < 21) {
                return false;
            }
            
            // Married can apply for any flat type
            return true;
        }
        
        return false;
    }
    
    /**
     * View the current application status.
     * 
     * @return The status of the current application, or "No Application" if none exists
     */
    public String viewApplicationStatus() {
        if (currentApplication == null) {
            return "No Application";
        }
        
        return currentApplication.getStatus();
    }
    
    /**
     * Request withdrawal of the current application.
     * 
     * @return true if withdrawal request is submitted, false otherwise
     */
    public boolean requestWithdrawal() {
        if (currentApplication == null) {
            return false;
        }
        
        // Set application status to WITHDRAWAL_REQUESTED
        currentApplication.setWithdrawalRequested(true);
        return true;
    }
    
/**
 * Edit an enquiry submitted by this applicant.
 * 
 * @param enquiryID The ID of the enquiry to edit (as a String)
 * @param text The new text for the enquiry
 * @return true if edit is successful, false otherwise
 */
public boolean editEnquiry(String enquiryId, String text) {
    for (Enquiry enquiry : enquiries) {
        if (enquiry.getEnquiryId().equals(enquiryId)) {
            enquiry.updateEnquiry(text);
            return true;
        }
    }
    
    return false;
}
    
    /**
     * Implementation of viewProjects from User.
     * Filters projects based on visibility and applicant eligibility.
     * 
     * @param filters Optional filters to apply
     * @return List of visible and eligible projects
     */
    @Override
    public List<Project> viewProjects(Map<String, Object> filters) {
        //List<Project> allProjects = DataManager.getInstance().getAllProjects();
        List<Project> allProjects = dataManager.getAllProjects();
        
        // Filter projects that are visible to applicants
        List<Project> visibleProjects = allProjects.stream()
            .filter(Project::isVisible)
            .collect(Collectors.toList());
        
        // Apply additional filters if provided
        if (filters != null && !filters.isEmpty()) {
            // Implementation of filters
        }
        
        return visibleProjects;
    }
    
    // Getters and setters
    public Application getCurrentApplication() {
        return currentApplication;
    }
    
    public void setCurrentApplication(Application currentApplication) {
        this.currentApplication = currentApplication;
    }
    
    public List<Enquiry> getEnquiries() {
        return enquiries;
    }
    
    public void addEnquiry(Enquiry enquiry) {
        this.enquiries.add(enquiry);
    }

    /////////////
    /**
     * Check if the applicant is married.
     * 
     * @return true if married, false if single
     */
    public boolean isMarried() {
        return "Married".equals(getMaritalStatus());
    }
}