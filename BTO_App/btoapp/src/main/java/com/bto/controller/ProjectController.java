package com.bto.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.User;

/**
 * Controller for handling project-related operations.
 */
public class ProjectController {
    private DataManager dataManager;
    private AuthController authController;
    
    /**
     * Constructor for ProjectController.
     * 
     * @param dataManager The data manager to be used
     * @param authController The auth controller to be used
     */
    public ProjectController(DataManager dataManager, AuthController authController) {
        this.dataManager = dataManager;
        this.authController = authController;
    }
    
    /**
     * Create a new BTO project using a manager instance.
     * 
     * @param manager The HDB Manager creating the project
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatTypes Map of flat types and their unit counts
     * @param openingDate The opening date for applications
     * @param closingDate The closing date for applications
     * @param officerSlots The number of officer slots for this project
     * @return true if project creation is successful, false otherwise
     */
    public boolean createProject(HDBManager manager, String projectName, String neighborhood, 
                              Map<String, Integer> flatTypes, Date openingDate, Date closingDate, 
                              int officerSlots) {
        // Create the project using the manager's method
        Project project = manager.createProject(projectName, neighborhood, openingDate, closingDate, flatTypes);

        // If project creation was successful, set officer slots and return true
        if (project != null) {
            project.setMaxOfficerSlots(officerSlots);
            dataManager.saveData();
            return true;
        }

        return false;
    }

    /**
     * Edit a project's details.
     * 
     * @param user The user attempting to edit the project
     * @param projectName The name of the project to edit
     * @param field The field to edit
     * @param newValue The new value for the field
     * @return true if edit is successful, false otherwise
     */
    public boolean editProject(User user, String projectName, String field, String newValue) {
        // Only HDB Managers can edit projects
        if (!(user instanceof HDBManager)) {
            return false;
        }
    
        Project project = getProjectByName(projectName);
        
        if (project == null || 
            project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(user.getUserID())) {
            return false;
        }
        
        switch (field.toLowerCase()) {
            case "neighborhood":
                project.setNeighborhood(newValue);
                break;
            case "projectname":
                project.setProjectName(newValue);
                break;
            default:
                return false;
        }
        
        dataManager.saveData();
        return true;
    }
    
    /**
     * Delete a project.
     * 
     * @param manager The HDB Manager deleting the project
     * @param projectName The name of the project to delete
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteProject(HDBManager manager, String projectName) {
        // Find the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Check if this manager is in charge of the project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
            return false;
        }
        
        // Remove the project
        List<Project> allProjects = dataManager.getAllProjects();
        allProjects.remove(project);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Toggle the visibility of a project.
     * 
     * @param manager The HDB Manager attempting to toggle visibility
     * @param projectName The name of the project to toggle
     * @param visibility The new visibility state (true for visible, false for hidden)
     * @return true if the visibility was successfully toggled, false otherwise
     */
    public boolean toggleProjectVisibility(HDBManager manager, String projectName, boolean visibility) {
        // Find the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Check if this manager is in charge of the project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
            return false;
        }
        
        // Toggle the project's visibility
        project.setVisible(visibility);
        
        // Save the changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Process an officer's registration for a project.
     * 
     * @param managerID The ID of the manager approving/rejecting the registration
     * @param officerID The ID of the officer
     * @param projectName The name of the project
     * @param approved Whether to approve or reject the registration
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processOfficerRegistration(String managerID, String officerID, String projectName, boolean approved) {
        // Find the manager by ID
        User manager = null;
        for (User user : dataManager.getAllUsers()) {
            if (user instanceof HDBManager && user.getUserID().equals(managerID)) {
                manager = user;
                break;
            }
        }
        
        // Check if the user is a manager
        if (!(manager instanceof HDBManager)) {
            return false;
        }
        
        // Find the project by name
        Project project = getProjectByName(projectName);
        if (project == null) {
            return false;
        }
        
        // Find the officer by ID
        HDBOfficer officer = null;
        for (User user : dataManager.getAllUsers()) {
            if (user instanceof HDBOfficer && user.getUserID().equals(officerID)) {
                officer = (HDBOfficer) user;
                break;
            }
        }
        
        if (officer == null) {
            return false;
        }
        
        // Process the registration
        if (approved) {
            return approveOfficerRegistration((HDBManager)manager, projectName, officerID);
        } else {
            // For rejection, simply remove the officer from the project
            return removeOfficerFromProject((HDBManager)manager, projectName, officerID);
        }
    }
    
    /**
     * Get available projects for a specific user.
     * 
     * @param user The user to get available projects for
     * @return List of projects available for the user
     */
    public List<Project> getAvailableProjects(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        
        List<Project> allProjects = dataManager.getAllProjects();
        List<Project> availableProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // For HDB Manager, show all projects regardless of visibility
            if (user instanceof HDBManager) {
                availableProjects.add(project);
                continue;
            }
            
            // For HDB Officer, show assigned project and visible projects
            if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                if ((officer.getAssignedProject() != null && 
                     officer.getAssignedProject().getProjectName().equals(project.getProjectName())) ||
                    project.isVisible()) {
                    availableProjects.add(project);
                }
                continue;
            }
            
            // For Applicant, filter based on visibility and eligibility
            if (user instanceof Applicant && project.isVisible()) {
                Applicant applicant = (Applicant) user;
                boolean isEligible = false;
                
                if ("Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 21) {
                    // Married applicants 21+ can apply for any flat type
                    isEligible = true;
                } else if (!"Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 35) {
                    // Single applicants 35+ can only apply for 2-Room
                    isEligible = project.hasFlatType("2-Room");
                }
                
                if (isEligible) {
                    availableProjects.add(project);
                }
                
                // Also add projects the applicant has already applied for
                Application currentApplication = ((Applicant)user).getCurrentApplication();
                if (currentApplication != null && 
                    currentApplication.getProject().getProjectName().equals(project.getProjectName()) &&
                    !availableProjects.contains(project)) {
                    availableProjects.add(project);
                }
            }
        }
        
        return availableProjects;
    }

    /**
     * Get a list of officers with pending registrations for projects managed by a specific manager.
     * 
     * @param manager The HDB Manager viewing the registrations
     * @param projectName The name of the project (optional, if null returns for all manager's projects)
     * @return List of officers with pending registrations
     */
    public List<HDBOfficer> getPendingOfficerRegistrations(HDBManager manager, String projectName) {
        List<HDBOfficer> pendingRegistrations = new ArrayList<>();
        
        // If projectName is provided, filter for that specific project
        if (projectName != null) {
            Project project = getProjectByName(projectName);
            
            if (project == null) {
                return pendingRegistrations;
            }
            
            // Check if the manager is in charge of the project
            if (project.getManagerInCharge() == null || 
                !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
                return pendingRegistrations;
            }
            
            // Filter officers with pending registration status for this project
            return project.getAssignedOfficers().stream()
                .filter(officer -> HDBOfficer.STATUS_PENDING.equals(officer.getRegistrationStatus()))
                .collect(Collectors.toList());
        }
        
        // If no projectName provided, get all pending registrations for manager's projects
        List<Project> managerProjects = getManagerProjects(manager);
        
        for (Project project : managerProjects) {
            for (HDBOfficer officer : project.getAssignedOfficers()) {
                if (HDBOfficer.STATUS_PENDING.equals(officer.getRegistrationStatus())) {
                    pendingRegistrations.add(officer);
                }
            }
        }
        
        return pendingRegistrations;
    }

    /**
     * Register an officer for a project using project name.
     * 
     * @param officer The HDB Officer attempting to register
     * @param projectName The name of the project to register for
     * @return true if registration is successful, false otherwise
     */
    public boolean registerOfficerForProject(HDBOfficer officer, String projectName) {
        // Find the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // 1. Check if officer is already assigned to a project
        if (officer.getAssignedProject() != null) {
            return false;
        }
        
        // 2. Check if project has available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // 3. Check if officer has already applied as an applicant
        boolean hasAppliedAsApplicant = project.getApplications().stream()
            .anyMatch(app -> app.getApplicant().getUserID().equals(officer.getUserID()));
        
        if (hasAppliedAsApplicant) {
            return false;
        }
        
        // 4. Add officer to project
        project.getAssignedOfficers().add(officer);
        
        // 5. Set officer's assigned project and registration status
        officer.setAssignedProject(project);
        officer.setRegistrationStatus(HDBOfficer.STATUS_PENDING);
        
        // 6. Save changes
        dataManager.saveData();
        
        return true;
    }

    /**
     * Get a project by its name.
     * 
     * @param projectName The name of the project to retrieve
     * @return The project with the specified name, or null if not found
     */
    public Project getProjectByName(String projectName) {
        // Get all projects from the data manager
        List<Project> allProjects = dataManager.getAllProjects();
        
        // Search for the project with the specified name
        for (Project project : allProjects) {
            if (project.getProjectName().equals(projectName)) {
                return project;
            }
        }
        
        // Return null if not found
        return null;
    }

    /**
     * Remove an officer from a project.
     * 
     * @param manager The HDB Manager removing the officer
     * @param projectName The name of the project
     * @param officerID The ID of the officer to remove
     * @return true if removal is successful, false otherwise
     */
    public boolean removeOfficerFromProject(HDBManager manager, String projectName, String officerID) {
        // Find the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Check if this manager is in charge of the project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
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
        dataManager.saveData();
        
        return true;
    }

    /**
     * Get all projects visible to a manager.
     * 
     * @param manager The HDB Manager requesting the projects
     * @return List of all projects visible to the manager
     */
    public List<Project> getAllProjects(HDBManager manager) {
        // Managers can see all projects
        return dataManager.getAllProjects();
    }

    /**
     * Get projects created by a specific HDB Manager.
     * 
     * @param manager The HDB Manager whose projects to retrieve
     * @return List of projects created by the manager
     */
    public List<Project> getManagerProjects(HDBManager manager) {
        List<Project> allProjects = dataManager.getAllProjects();
        
        return allProjects.stream()
            .filter(project -> project.getManagerInCharge() != null && 
                            project.getManagerInCharge().getUserID().equals(manager.getUserID()))
            .collect(Collectors.toList());
    }

    /**
     * Approve an officer's registration for a project.
     * 
     * @param manager The HDB Manager approving the registration
     * @param projectName The name of the project
     * @param officerUserID The user ID of the officer being approved
     * @return true if approval is successful, false otherwise
     */
    public boolean approveOfficerRegistration(HDBManager manager, String projectName, String officerUserID) {
        // Find the project
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Check if the manager is in charge of the project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
            return false;
        }
        
        // Find the officer in the project's assigned officers
        HDBOfficer officerToApprove = project.getAssignedOfficers().stream()
            .filter(officer -> officer.getUserID().equals(officerUserID) && 
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
        dataManager.saveData();
        
        return true;
    }

    /**
     * Get a list of officers with approved registration status for a specific project.
     * 
     * @param manager The HDB Manager viewing the approved officers
     * @param projectName The name of the project
     * @return List of approved officers for the project
     */
    public List<HDBOfficer> getApprovedOfficers(HDBManager manager, String projectName) {
        // Find the project
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return new ArrayList<>();
        }
        
        // Check if the manager is in charge of the project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
            return new ArrayList<>();
        }
        
        // Filter and return officers with approved registration status for this project
        return project.getAssignedOfficers().stream()
            .filter(officer -> HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus()))
            .collect(Collectors.toList());
    }

    /**
     * Get the remaining units for a specific flat type in a project.
     * 
     * @param projectName The name of the project
     * @param flatType The type of flat (e.g., "2-Room")
     * @return The number of remaining units, or -1 if project not found
     */
    public int getRemainingUnits(String projectName, String flatType) {
        Project project = getProjectByName(projectName);
        if (project == null) {
            return -1; // Project not found
        }
        return project.getRemainingUnits(flatType); // Delegate to Project's method
    }

    /**
     * Get the registration status of an officer for a specific project.
     * 
     * @param officer The HDB Officer whose status to check
     * @param projectName The name of the project
     * @return The registration status, or null if not registered for this project
     */
    public String getOfficerRegistrationStatus(HDBOfficer officer, String projectName) {
        // Get the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return null;
        }
        
        // Check if officer is assigned to this project
        if (officer.getAssignedProject() == null || 
            !officer.getAssignedProject().getProjectName().equals(projectName)) {
            return null;
        }
        
        // Return the officer's registration status
        return officer.getRegistrationStatus();
    }

    /**
     * Get the list of projects an officer is assigned to.
     * 
     * @param officer The HDB Officer whose projects to retrieve
     * @return A list of project names the officer is assigned to
     */
    public List<String> getOfficerProjects(HDBOfficer officer) {
        List<String> projectNames = new ArrayList<>();
        
        // If officer is assigned to a project, add its name
        if (officer.getAssignedProject() != null) {
            projectNames.add(officer.getAssignedProject().getProjectName());
        }
        
        return projectNames;
    }

    /**
     * Get details of a specific project for an HDB Officer.
     * Officers can view project details regardless of visibility setting.
     * 
     * @param officer The HDB Officer requesting project details
     * @param projectName The name of the project
     * @return The project if found and officer has access, null otherwise
     */
    public Project getProjectDetails(HDBOfficer officer, String projectName) {
        // Find the project by name
        Project project = getProjectByName(projectName);
        
        if (project == null) {
            return null;
        }
        
        // Officers should be able to view project details regardless of visibility
        // if they are assigned to the project
        if (officer.getAssignedProject() != null && 
            officer.getAssignedProject().getProjectName().equals(projectName)) {
            return project;
        }
        
        // If not assigned to this project, follow normal visibility rules
        if (project.isVisible()) {
            return project;
        }
        
        return null;
    }
    
    /**
     * Get projects filtered by neighborhood.
     * 
     * @param neighborhood The neighborhood to filter by
     * @param currentUser The current user making the request
     * @return List of projects in the specified neighborhood
     */
    public List<Project> getProjectsByNeighborhood(String neighborhood, User currentUser) {
        List<Project> availableProjects = getAvailableProjects(currentUser);
        
        return availableProjects.stream()
            .filter(project -> project.getNeighborhood().toLowerCase().contains(neighborhood.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get projects filtered by flat type.
     * 
     * @param flatType The flat type to filter by
     * @param currentUser The current user making the request
     * @return List of projects offering the specified flat type
     */
    public List<Project> getProjectsByFlatType(String flatType, User currentUser) {
        List<Project> availableProjects = getAvailableProjects(currentUser);
        
        return availableProjects.stream()
            .filter(project -> project.hasFlatType(flatType))
            .collect(Collectors.toList());
    }
}