package com.bto.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bto.model.Applicant;
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
    
    // /**
    //  * Constructor for ProjectController.
    //  */
    // public ProjectController() {
    //     this.dataManager = DataManager.getInstance();
    //     this.authController = new AuthController();
    // }

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
     * Create a new BTO project.
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
        User currentUser = authController.getCurrentUser();
        
        // Only managers can create projects
        if (!(currentUser instanceof HDBManager)) {
            return null;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        return manager.createProject(projectName, neighborhood, openingDate, closingDate, flatTypes);
    }
    
    /**
     * Edit an existing project.
     * 
     * @param projectID The ID of the project to edit
     * @param projectName The new name (or null to keep existing)
     * @param neighborhood The new neighborhood (or null to keep existing)
     * @param openingDate The new opening date (or null to keep existing)
     * @param closingDate The new closing date (or null to keep existing)
     * @param flatTypes The new flat types (or null to keep existing)
     * @return true if edit is successful, false otherwise
     */
public boolean editProject(int projectID, String projectName, String neighborhood,
                             Date openingDate, Date closingDate, Map<String, Integer> flatTypes) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can edit projects
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        return manager.editProject(project, projectName, neighborhood, openingDate, closingDate, flatTypes);
    }
    
    /**
     * Delete a project.
     * 
     * @param projectID The ID of the project to delete
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteProject(int projectID) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can delete projects
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        return manager.deleteProject(project);
    }
    
    /**
     * Toggle the visibility of a project.
     * 
     * @param projectID The ID of the project to toggle visibility for
     * @return true if toggle is successful, false otherwise
     */
    public boolean toggleProjectVisibility(int projectID) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can toggle project visibility
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        return manager.toggleProjectVisibility(project);
    }
    
    /**
     * Register an officer for a project.
     * 
     * @param projectID The ID of the project to register for
     * @return true if registration is successful, false otherwise
     */
    public boolean registerForProject(int projectID) {
        User currentUser = authController.getCurrentUser();
        
        // Only officers can register for projects
        if (!(currentUser instanceof HDBOfficer)) {
            return false;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        return officer.registerForProject(project);
    }
    
    /**
     * Approve or reject an officer's registration for a project.
     * 
     * @param officerID The ID of the officer
     * @param projectID The ID of the project
     * @param approved Whether to approve or reject the registration
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processOfficerRegistration(String officerID, int projectID, boolean approved) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can process officer registrations
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        // Find the officer by ID
        HDBOfficer officer = findOfficerByID(officerID);
        if (officer == null) {
            return false;
        }
        
        return manager.processOfficerRegistration(officer, project, approved);
    }
    
    /**
     * Get a list of all projects filtered by various criteria.
     * 
     * @param filters Map of filters to apply
     * @return List of projects that match the filters
     */
    public List<Project> getProjects(Map<String, Object> filters) {
        User currentUser = authController.getCurrentUser();
        
        if (currentUser == null) {
            return null;
        }
        
        return currentUser.viewProjects(filters);
    }
    
    /**
     * Find a project by its ID.
     * 
     * @param projectID The ID of the project to find
     * @return The project if found, null otherwise
     */
    private Project findProjectByID(int projectID) {
        List<Project> allProjects = dataManager.getAllProjects();
        
        for (Project project : allProjects) {
            if (project.getProjectID() == projectID) {
                return project;
            }
        }
        
        return null;
    }
    
    /**
     * Find an officer by their ID.
     * 
     * @param officerID The ID of the officer to find
     * @return The officer if found, null otherwise
     */
    private HDBOfficer findOfficerByID(String officerID) {
        List<User> allUsers = dataManager.getAllUsers();
        
        for (User user : allUsers) {
            if (user instanceof HDBOfficer && user.getUserID().equals(officerID)) {
                return (HDBOfficer) user;
            }
        }
        
        return null;
    }

    //////////////
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
        
        Map<String, Object> filters = new HashMap<>();
        
        // Filter by visibility (visible projects only)
        filters.put("visible", true);
        
        // If user is an Applicant, filter based on eligibility
        if (user instanceof Applicant) {
            Applicant applicant = (Applicant) user;
            
            // Add user-specific filters (marital status, age, etc.)
            filters.put("maritalStatus", applicant.getMaritalStatus());
            filters.put("age", applicant.getAge());
        }
        
        return getProjects(filters);
    }

        /**
     * Get a list of officers with pending registrations for a list of projects.
     * 
     * @param projects The list of projects to check
     * @return A list of officers with pending registrations
     */
    public List<HDBOfficer> getPendingOfficerRegistrations(List<Project> projects) {
        List<HDBOfficer> pendingRegistrations = new ArrayList<>();
        List<User> allUsers = dataManager.getAllUsers();
        
        for (User user : allUsers) {
            if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                
                // Check if officer has pending registration for any of the projects
                if (officer.getAssignedProject() != null && 
                    officer.getRegistrationStatus() != null && 
                    officer.getRegistrationStatus().equals(HDBOfficer.STATUS_PENDING)) {
                    
                    // Check if the project is in the provided list
                    for (Project project : projects) {
                        if (officer.getAssignedProject().getProjectID() == project.getProjectID()) {
                            pendingRegistrations.add(officer);
                            break;
                        }
                    }
                }
            }
        }
        
        return pendingRegistrations;
    }

        /**
     * Get projects available for officer registration.
     * 
     * @return List of projects that officers can register for
     */
    public List<Project> getProjectsForOfficerRegistration() {
        List<Project> allProjects = dataManager.getAllProjects();
        List<Project> availableProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // Officers can only register for projects that:
            // 1. Are open for applications
            // 2. Have remaining officer slots
            if (project.isOpen() && project.getRemainingOfficerSlots() > 0) {
                availableProjects.add(project);
            }
        }
        
        return availableProjects;
    }

    /**
     * Get a project by its ID.
     * 
     * @param projectID The ID of the project to retrieve
     * @return The project with the specified ID, or null if not found
     */
    public Project getProjectByID(int projectID) {
        return findProjectByID(projectID);
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
    List<Project> filteredProjects = new ArrayList<>();
    
    for (Project project : availableProjects) {
        if (project.getNeighborhood().toLowerCase().contains(neighborhood.toLowerCase())) {
            filteredProjects.add(project);
        }
    }
    
    return filteredProjects;
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
    List<Project> filteredProjects = new ArrayList<>();
    
    for (Project project : availableProjects) {
        if (project.hasFlatType(flatType)) {
            filteredProjects.add(project);
        }
    }
    
    return filteredProjects;
}

    /**
     * Get projects filtered by status.
     * 
     * @param isOpen Filter for open projects
     * @param isUpcoming Filter for upcoming projects
     * @param currentUser The current user making the request
     * @return List of projects matching the specified status
     */
    public List<Project> getProjectsByStatus(boolean isOpen, boolean isUpcoming, User currentUser) {
        List<Project> availableProjects = getAvailableProjects(currentUser);
        List<Project> filteredProjects = new ArrayList<>();
        Date now = new Date();
        
        for (Project project : availableProjects) {
            boolean matchesStatus = false;
            
            if (isOpen && now.after(project.getOpeningDate()) && now.before(project.getClosingDate())) {
                matchesStatus = true;
            } else if (isUpcoming && now.before(project.getOpeningDate())) {
                matchesStatus = true;
            } else if (!isOpen && !isUpcoming && now.after(project.getClosingDate())) {
                // For closed projects (not open and not upcoming)
                matchesStatus = true;
            }
            
            if (matchesStatus) {
                filteredProjects.add(project);
            }
        }
        
        return filteredProjects;
    }

}