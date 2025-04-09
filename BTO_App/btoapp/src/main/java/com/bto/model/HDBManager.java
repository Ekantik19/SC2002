package com.bto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents an HDB manager who can create and manage BTO projects.
 * HDB Managers have all capabilities of HDB Officers plus additional 
 * project management responsibilities.
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
     * Create a new BTO project with validation for project period.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param openingDate The opening date for applications
     * @param closingDate The closing date for applications
     * @param flatTypes Map of flat types and their unit counts
     * @return The created project, or null if creation fails
     */
    public Project createProject(String projectName, String neighborhood, 
                               Date openingDate, Date closingDate, 
                               Map<String, Integer> flatTypes) {
        // Validate input parameters
        if (projectName == null || neighborhood == null || 
            openingDate == null || closingDate == null || 
            flatTypes == null || flatTypes.isEmpty()) {
            return null;
        }
        
        // Validate project period - manager can only have one project in a period
        if (!isProjectPeriodAvailable(openingDate, closingDate)) {
            return null;
        }
        
        // Create project
        Project project = new Project(projectName, neighborhood, 
                                      openingDate, closingDate, this);
        
        // Add flat types
        for (Map.Entry<String, Integer> entry : flatTypes.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                project.addFlatType(entry.getKey(), entry.getValue());
            }
        }
        
        // Manage project tracking and persistence
        createdProjects.add(project);
        if (dataManager != null) {
            dataManager.addProject(project);
        }
        
        return project;
    }
    
    /**
     * Check if project period is available.
     * Managers can only handle one project within an application period.
     * 
     * @param newOpeningDate Proposed opening date
     * @param newClosingDate Proposed closing date
     * @return true if no overlapping projects exist, false otherwise
     */
    private boolean isProjectPeriodAvailable(Date newOpeningDate, Date newClosingDate) {
        return createdProjects.stream().noneMatch(project -> 
            !(newClosingDate.before(project.getOpeningDate()) || 
              newOpeningDate.after(project.getClosingDate()))
        );
    }
    
    /**
     * Process an application (approve or reject).
     * 
     * @param application The application to process
     * @param approved Whether to approve or reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processApplication(Application application, boolean approved) {
        // Validate project ownership
        Project project = application.getProject();
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Validate application status
        if (!Application.STATUS_PENDING.equals(application.getStatus())) {
            return false;
        }
        
        // If approving, validate flat type availability
        if (approved) {
            // Validate flat type availability for the applicant
            Applicant applicant = application.getApplicant();
            boolean hasFlatType = validateApplicantFlatType(project, applicant);
            
            if (!hasFlatType) {
                return false;
            }
            
            application.updateStatus(Application.STATUS_SUCCESSFUL);
        } else {
            application.updateStatus(Application.STATUS_UNSUCCESSFUL);
        }
        
        // Persist changes
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Process a withdrawal request (approve or reject).
     * 
     * @param application The application with withdrawal request
     * @param approved Whether to approve or reject the withdrawal
     * @return true if processing is successful, false otherwise
     */
    public boolean processWithdrawalRequest(Application application, boolean approved) {
        // Validate project ownership
        Project project = application.getProject();
        if (!createdProjects.contains(project)) {
            return false;
        }
        
        // Validate withdrawal request
        if (!application.isWithdrawalRequested()) {
            return false;
        }
        
        if (approved) {
            // Handle booked application special case
            if (Application.STATUS_BOOKED.equals(application.getStatus())) {
                String flatType = application.getFlatTypeBooked();
                if (flatType != null) {
                    project.incrementUnits(flatType);
                }
            }
            
            // Remove application
            project.getApplications().remove(application);
            application.getApplicant().setCurrentApplication(null);
        } else {
            // Reset withdrawal request flag
            application.setWithdrawalRequested(false);
        }
        
        // Persist changes
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Toggle the visibility of a project.
     * 
     * @param projectName The name of the project to toggle
     * @param visibility New visibility state (true for visible, false for hidden)
     * @return true if toggle is successful, false otherwise
     */
    public boolean toggleProjectVisibility(String projectName, boolean visibility) {
        Project project = findProjectByName(projectName);
        
        if (project == null || !createdProjects.contains(project)) {
            return false;
        }
        
        project.setVisible(visibility);
        
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Get projects created by this manager.
     * 
     * @return List of projects created by this manager
     */
    public List<Project> getCreatedProjects() {
        return new ArrayList<>(createdProjects);
    }
    
    /**
     * Find a project by name in this manager's created projects.
     * 
     * @param projectName The name of the project to find
     * @return The project if found, null otherwise
     */
    private Project findProjectByName(String projectName) {
        return createdProjects.stream()
            .filter(p -> p.getProjectName().equals(projectName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Validate flat type availability for an applicant.
     * 
     * @param project The project
     * @param applicant The applicant
     * @return true if appropriate flat type is available
     */
    private boolean validateApplicantFlatType(Project project, Applicant applicant) {
        if (!"Married".equals(applicant.getMaritalStatus())) {
            // Singles can only apply for 2-Room and must be 35+
            return applicant.getAge() >= 35 && project.hasFlatType("2-Room");
        } else {
            // Married can apply for 2-Room or 3-Room if 21+
            return applicant.getAge() >= 21 && 
                  (project.hasFlatType("2-Room") || project.hasFlatType("3-Room"));
        }
    }
    
    /**
     * Approve an officer registration for a project.
     * 
     * @param projectName The name of the project
     * @param officerID The ID of the officer to approve
     * @return true if approval is successful, false otherwise
     */
    public boolean approveOfficerRegistration(String projectName, String officerID) {
        Project project = findProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Find the officer in the project's assigned officers
        HDBOfficer officerToApprove = project.getAssignedOfficers().stream()
            .filter(officer -> officer.getUserID().equals(officerID) && 
                            HDBOfficer.STATUS_PENDING.equals(officer.getRegistrationStatus()))
            .findFirst()
            .orElse(null);
        
        if (officerToApprove == null) {
            return false;
        }
        
        // Check if project has available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Update officer's registration status
        officerToApprove.setRegistrationStatus(HDBOfficer.STATUS_APPROVED);
        
        // Save changes
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Reject or remove an officer from a project.
     * 
     * @param projectName The name of the project
     * @param officerID The ID of the officer to remove
     * @return true if removal is successful, false otherwise
     */
    public boolean removeOfficerFromProject(String projectName, String officerID) {
        Project project = findProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Find the officer by ID
        HDBOfficer officerToRemove = null;
        for (HDBOfficer officer : project.getAssignedOfficers()) {
            if (officer.getUserID().equals(officerID)) {
                officerToRemove = officer;
                break;
            }
        }
        
        if (officerToRemove == null) {
            return false;
        }
        
        // Remove the officer from the project
        project.getAssignedOfficers().remove(officerToRemove);
        officerToRemove.setAssignedProject(null);
        officerToRemove.setRegistrationStatus(null);
        
        // Save changes
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        return true;
    }
    
    /**
     * Overriding the HDBOfficer registerForProject method.
     * HDB Managers cannot apply for BTO projects as applicants.
     * 
     * @param project The project to register for
     * @return always false, as managers cannot apply for projects
     */
    @Override
    public boolean registerForProject(Project project) {
        return false; // Managers cannot apply for projects
    }
}