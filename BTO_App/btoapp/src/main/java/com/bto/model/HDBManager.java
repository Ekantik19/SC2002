package com.bto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents an HDB manager who can create and manage BTO projects.
 */
public class HDBManager extends HDBOfficer {
    private List<Project> createdProjects;
    
    /**
     * Constructor for HDBManager.
     * 
     * @param userID The unique identifier for the user (NRIC)
     * @param password The user's password
     * @param age The user's age
     * @param maritalStatus The user's marital status
     */
    public HDBManager(String userID, String password, int age, String maritalStatus) {
        super(userID, password, age, maritalStatus);
        this.createdProjects = new ArrayList<>();
    }
    
    /**
     * Create a new BTO project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param openingDate The opening date for applications
     * @param closingDate The closing date for applications
     * @param flatTypes Map of flat types and their unit counts
     * @return The created project
     */
    public Project createProject(String projectName, String neighborhood, 
                               Date openingDate, Date closingDate, 
                               Map<String, Integer> flatTypes) {
        // Check if already handling a project during the same period
        for (Project project : createdProjects) {
            Date projectOpeningDate = project.getOpeningDate();
            Date projectClosingDate = project.getClosingDate();
            
            // Check for overlap in dates
            if (!(closingDate.before(projectOpeningDate) || openingDate.after(projectClosingDate))) {
                return null; // Overlapping project found
            }
        }
        
        // Generate project ID (in a real system this would be more sophisticated)
        int projectID = (int) (Math.random() * 10000);
        
        // Create new project
        Project project = new Project(projectID, projectName, neighborhood, openingDate, closingDate, this);
        
        // Add flat types
        for (Map.Entry<String, Integer> entry : flatTypes.entrySet()) {
            project.addFlatType(entry.getKey(), entry.getValue());
        }
        
        // Add to created projects
        createdProjects.add(project);
        
        // Add to data manager
        //DataManager.getInstance().addProject(project);
        dataManager.addProject(project);
        
        return project;
    }
    
    /**
     * Edit an existing project.
     * 
     * @param project The project to edit
     * @param projectName The new name (or null to keep existing)
     * @param neighborhood The new neighborhood (or null to keep existing)
     * @param openingDate The new opening date (or null to keep existing)
     * @param closingDate The new closing date (or null to keep existing)
     * @param flatTypes The new flat types (or null to keep existing)
     * @return true if edit is successful, false otherwise
     */
    public boolean editProject(Project project, String projectName, String neighborhood,
                             Date openingDate, Date closingDate, Map<String, Integer> flatTypes) {
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Update fields if provided
        if (projectName != null) {
            project.setProjectName(projectName);
        }
        
        if (neighborhood != null) {
            project.setNeighborhood(neighborhood);
        }
        
        if (openingDate != null) {
            project.setOpeningDate(openingDate);
        }
        
        if (closingDate != null) {
            project.setClosingDate(closingDate);
        }
        
        if (flatTypes != null) {
            // Update flat types
            for (Map.Entry<String, Integer> entry : flatTypes.entrySet()) {
                project.addFlatType(entry.getKey(), entry.getValue());
            }
        }
        
        // Save changes
        //DataManager.getInstance().saveData();
        dataManager.saveData();

        return true;
    }
    
    /**
     * Delete a project.
     * 
     * @param project The project to delete
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Check if project has any applications
        if (!project.getApplications().isEmpty()) {
            return false; // Can't delete project with applications
        }
        
        // Remove from created projects
        createdProjects.remove(project);
        
        // Remove from data manager
        //List<Project> allProjects = DataManager.getInstance().getAllProjects();
        List<Project> allProjects = dataManager.getAllProjects();
        allProjects.remove(project);
        //DataManager.getInstance().saveData();
        dataManager.saveData();

        
        
        
        return true;
    }
    
    /**
     * Toggle the visibility of a project.
     * 
     * @param project The project to toggle visibility for
     * @return true if toggle is successful, false otherwise
     */
    public boolean toggleProjectVisibility(Project project) {
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        project.setVisible(!project.isVisible());
        //DataManager.getInstance().saveData();
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Approve or reject an HDB officer's registration for a project.
     * 
     * @param officer The officer requesting registration
     * @param project The project the officer is registering for
     * @param approved Whether to approve or reject the registration
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processOfficerRegistration(HDBOfficer officer, Project project, boolean approved) {
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Check if officer is registering for this project
        if (officer.getAssignedProject() == null || 
            officer.getAssignedProject().getProjectID() != project.getProjectID() ||
            !HDBOfficer.STATUS_PENDING.equals(officer.getRegistrationStatus())) {
            return false;
        }
        
        if (approved) {
            // Check if there are available slots
            if (project.getRemainingOfficerSlots() <= 0) {
                return false;
            }
            
            officer.setRegistrationStatus(HDBOfficer.STATUS_APPROVED);
            project.addOfficer(officer);
        } else {
            officer.setRegistrationStatus(HDBOfficer.STATUS_REJECTED);
            officer.setAssignedProject(null);
        }
        
        // Save changes
        //DataManager.getInstance().saveData();
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Approve or reject an application for a BTO project.
     * 
     * @param application The application to approve/reject
     * @param approved Whether to approve or reject the application
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processApplication(Application application, boolean approved) {
        Project project = application.getProject();
        
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Check if application is pending
        if (!Application.STATUS_PENDING.equals(application.getStatus())) {
            return false;
        }
        
        if (approved) {
            // Check if there are enough flats available for the applicant's eligibility
            Applicant applicant = application.getApplicant();
            boolean hasFlatType = false;
            
            if ("Single".equals(applicant.getMaritalStatus())) {
                // Singles can only apply for 2-Room
                hasFlatType = project.hasFlatType("2-Room");
            } else {
                // Married can apply for any flat type
                hasFlatType = project.hasFlatType("2-Room") || project.hasFlatType("3-Room");
            }
            
            if (!hasFlatType) {
                return false;
            }
            
            application.updateStatus(Application.STATUS_SUCCESSFUL);
        } else {
            application.updateStatus(Application.STATUS_UNSUCCESSFUL);
        }
        
        // Save changes
        //DataManager.getInstance().saveData();
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Approve or reject a withdrawal request for an application.
     * 
     * @param application The application with a withdrawal request
     * @param approved Whether to approve or reject the withdrawal
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processWithdrawalRequest(Application application, boolean approved) {
        Project project = application.getProject();
        
        // Check if this manager created the project
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Check if withdrawal was requested
        if (!application.isWithdrawalRequested()) {
            return false;
        }
        
        if (approved) {
            // If application was booked, increment available units
            if (Application.STATUS_BOOKED.equals(application.getStatus())) {
                String flatType = application.getFlatTypeBooked();
                Map<String, Integer> flatTypes = project.getFlatTypes();
                int currentCount = flatTypes.get(flatType);
                flatTypes.put(flatType, currentCount + 1);
            }
            
            // Remove application
            project.getApplications().remove(application);
            application.getApplicant().setCurrentApplication(null);
        } else {
            // Just mark as not requested
            application.setWithdrawalRequested(false);
        }
        
        // Save changes
        //DataManager.getInstance().saveData();
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Generate a report of applications with filtering options.
     * 
     * @param filters Map of filters to apply
     * @return A Report object with the filtered data
     */
    public Report generateReport(Map<String, Object> filters) {
        // Create a new report with the specified filters
        return new Report(filters,dataManager);
    }
    
    /**
     * Implementation of viewProjects from User.
     * Managers can see all projects regardless of visibility.
     * 
     * @param filters Optional filters to apply
     * @return List of all projects
     */
    @Override
    public List<Project> viewProjects(Map<String, Object> filters) {
        //List<Project> allProjects = DataManager.getInstance().getAllProjects();
        List<Project> allProjects = dataManager.getAllProjects();
        
        // Apply additional filters if provided
        if (filters != null && !filters.isEmpty()) {
            // Implementation of filters
            
            // Filter for manager's own projects if requested
            if (filters.containsKey("createdByMe") && (Boolean) filters.get("createdByMe")) {
                return createdProjects;
            }
        }
        
        return allProjects;
    }
    
    // Getters and setters
    public List<Project> getCreatedProjects() {
        return createdProjects;
    }

}