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
    private String bookedFlatType;
    private String bookedProject;
    
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
     * @param flatType The type of flat to apply for
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(Project project, String flatType) {
        // Check if already has an application
        if (currentApplication != null) {
            return false;
        }
        
        // Check eligibility based on age and marital status
        boolean eligible = isEligibleForFlatType(flatType);
        if (!eligible) {
            return false;
        }
        
        // Check if project has the requested flat type
        if (!project.hasFlatType(flatType)) {
            return false;
        }
        
        // Create new application
        currentApplication = new Application(this, project);
        currentApplication.setFlatType(flatType);
        
        // Add the application to project
        project.addApplication(currentApplication);
        
        // Add to data manager
        if (dataManager != null) {
            dataManager.addApplication(currentApplication);
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Check if the applicant is eligible for the specified flat type based on age and marital status.
     * 
     * @param flatType The type of flat to check eligibility for
     * @return true if eligible, false otherwise
     */
    public boolean isEligibleForFlatType(String flatType) {
        // Singles must be 35 or older and can only apply for 2-Room
        if (!"Married".equalsIgnoreCase(getMaritalStatus())) {
            if (getAge() < 35) {
                return false;
            }
            
            // Singles can only apply for 2-Room
            return "2-Room".equalsIgnoreCase(flatType);
        }
        // Married must be 21 or older and can apply for any flat type
        else {
            if (getAge() < 21) {
                return false;
            }
            
            // Married can apply for 2-Room or 3-Room
            return "2-Room".equalsIgnoreCase(flatType) || "3-Room".equalsIgnoreCase(flatType);
        }
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
        
        // Set withdrawal requested flag
        currentApplication.setWithdrawalRequested(true);
        
        // Save changes
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
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
        List<Project> allProjects = dataManager != null ? dataManager.getAllProjects() : new ArrayList<>();
        
        // Filter projects that are visible to applicants
        List<Project> visibleProjects = allProjects.stream()
            .filter(Project::isVisible)
            .collect(Collectors.toList());
        
        // Filter by applicant eligibility
        List<Project> eligibleProjects = visibleProjects.stream()
            .filter(this::isEligibleForProject)
            .collect(Collectors.toList());
        
        // Apply additional filters if provided
        if (filters != null && !filters.isEmpty()) {
            if (filters.containsKey("neighborhood")) {
                String neighborhood = (String) filters.get("neighborhood");
                eligibleProjects = eligibleProjects.stream()
                    .filter(p -> p.getNeighborhood().toLowerCase().contains(neighborhood.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (filters.containsKey("flatType")) {
                String flatType = (String) filters.get("flatType");
                eligibleProjects = eligibleProjects.stream()
                    .filter(p -> p.hasFlatType(flatType))
                    .collect(Collectors.toList());
            }
        }
        
        // Add the project the applicant has already applied for (even if visibility is off)
        if (currentApplication != null) {
            Project appliedProject = currentApplication.getProject();
            if (!eligibleProjects.contains(appliedProject)) {
                eligibleProjects.add(appliedProject);
            }
        }
        
        return eligibleProjects;
    }
    
    /**
     * Check if the applicant is eligible for the project based on age and marital status.
     * 
     * @param project The project to check eligibility for
     * @return true if eligible, false otherwise
     */
    private boolean isEligibleForProject(Project project) {
        // Singles must be 35 or older and can only apply for 2-Room
        if (!"Married".equalsIgnoreCase(getMaritalStatus())) {
            if (getAge() < 35) {
                return false;
            }
            
            // Check if project has 2-Room flats
            return project.hasFlatType("2-Room");
        }
        // Married must be 21 or older
        else {
            if (getAge() < 21) {
                return false;
            }
            
            // Check if project has either 2-Room or 3-Room flats
            return project.hasFlatType("2-Room") || project.hasFlatType("3-Room");
        }
    }
    
    /**
     * Submit an enquiry about a project.
     * 
     * @param project The project to submit enquiry for
     * @param enquiryText The text of the enquiry
     * @return The created Enquiry object, or null if submission fails
     */
    @Override
    public Enquiry submitEnquiry(Project project, String enquiryText) {
        Enquiry enquiry = new Enquiry(this, project, enquiryText);
        
        // Add to project
        project.addEnquiry(enquiry);
        
        // Add to applicant's list
        this.enquiries.add(enquiry);
        
        // Save to data manager
        if (dataManager != null) {
            dataManager.saveEnquiry(enquiry);
        }
        
        return enquiry;
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
    
    public String getBookedFlatType() {
        return bookedFlatType;
    }
    
    public void setBookedFlatType(String bookedFlatType) {
        this.bookedFlatType = bookedFlatType;
    }
    
    public String getBookedProject() {
        return bookedProject;
    }
    
    public void setBookedProject(String bookedProject) {
        this.bookedProject = bookedProject;
    }
}