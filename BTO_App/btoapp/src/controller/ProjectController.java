package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IProjectController;
import datamanager.ProjectDataManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.FlatType;
/**
 * Controller for managing BTO projects in the system.
 * Implements IProjectController and extends ABaseController.
 * 
 * Responsible for comprehensive project management operations including:
 * - Creating new projects
 * - Updating existing projects
 * - Deleting projects
 * - Managing project visibility
 * - Registering officers for projects
 * - Retrieving project information
 * 
 * @author Your Name
 * @version 1.0
 */
public class ProjectController extends ABaseController implements IProjectController {
    
    private ProjectDataManager projectDataManager;
    
    /**
     * Constructor for ProjectController.
     * 
     * @param projectDataManager The data manager for project operations
     */
    public ProjectController(ProjectDataManager projectDataManager) {
        this.projectDataManager = projectDataManager;
    }

    /**
    * Creates a new BTO project with specified details.
    * 
    * Validates project creation parameters and ensures:
    * - No overlapping project periods for the manager
    * - Proper project initialization
    * 
    * @param projectName Name of the project
    * @param neighborhood Geographical location of the project
    * @param flatTypes List of flat types available in the project
    * @param numberOfUnits List of unit counts for each flat type
    * @param sellingPrices List of selling prices for each flat type
    * @param openingDate Project application opening date
    * @param closingDate Project application closing date
    * @param manager HDB Manager creating the project
    * @param officerSlots Number of officer slots for the project
    * @return The created Project object, or null if creation fails
    */
    @Override
    public Project createProject(String projectName, String neighborhood, 
                                List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                                List<Double> sellingPrices, Date openingDate, 
                                Date closingDate, HDBManager manager, int officerSlots) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectName, "Project Name") || 
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") ||
            flatTypes == null || numberOfUnits == null || sellingPrices == null ||
            openingDate == null || closingDate == null || manager == null) {
            System.out.println("Project creation validation failed");
            return null;
        }
        
        // Check if manager can create project (only one project per application period)
        List<Project> managerProjects = getProjectsByManager(manager);
        boolean canCreateProject = managerProjects.stream()
            .noneMatch(p -> isOverlappingPeriod(p, openingDate, closingDate));
        
        if (!canCreateProject) {
            System.out.println("Manager cannot create multiple projects in the same application period.");
            return null;
        }
        
        // Create project through manager or directly
        Project project = null;
        try {
            System.out.println("Create project through manager: " + manager.getName());
            project = manager.createProject(projectName, neighborhood, 
                                           flatTypes, numberOfUnits, 
                                           sellingPrices, openingDate, 
                                           closingDate, officerSlots);
        } catch (Exception e) {
            System.out.println("Error creating project through manager: " + e.getMessage());
            e.printStackTrace();
            
            // Create project directly
            project = new Project(projectName, neighborhood, flatTypes, numberOfUnits, 
                                 sellingPrices, openingDate, closingDate, manager, officerSlots);
        }
        
        // Add project to data manager
        if (project != null) {
            System.out.println("Project created: " + project.getProjectName());
            boolean added = projectDataManager.addProject(project);
            
        } else {
            System.out.println("Failed to create project");
        }
        
        return project;
    }
    
    /**
     * Updates an existing project with date overlap prevention.
     * 
     * @param projectId The ID (name) of the project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots
     * @param manager The manager updating the project
     * @return true if the project was successfully updated, false otherwise
     */
    @Override
    public boolean updateProject(String projectId, String projectName, 
                                String neighborhood, Date openingDate, 
                                Date closingDate, int officerSlots, 
                                HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") ||
            !validateNotNullOrEmpty(projectName, "Project Name") ||
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") ||
            openingDate == null || closingDate == null || manager == null) {
            System.out.println("Invalid input parameters for project update");
            return false;
        }
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found for update: " + projectId);
            return false;
        }
        
        // Check if the manager is authorized to update this project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println(" Manager not authorized to update this project");
            return false;
        }
        
        // Check for date overlaps with other projects managed by this manager
        List<Project> managerProjects = getProjectsByManager(manager);
        boolean hasOverlap = managerProjects.stream()
            .filter(p -> !p.getProjectName().equals(projectId)) // Exclude current project
            .anyMatch(p -> isOverlappingPeriod(p, openingDate, closingDate));
        
        if (hasOverlap) {
            System.out.println(" Cannot update project with overlapping dates");
            return false;
        }
        
        // Attempt to update the project
        boolean updated = false;
        try {
            // Delegate update to manager
            updated = manager.updateProject(project, projectName, neighborhood, 
                                        openingDate, closingDate, officerSlots);
        } catch (Exception e) {
            System.out.println(" Error updating project through manager: " + e.getMessage());
            
            // Update project directly
            try {
                project.setProjectName(projectName);
                project.setNeighborhood(neighborhood);
                project.setApplicationOpeningDate(openingDate);
                project.setApplicationClosingDate(closingDate);
                project.setOfficerSlots(officerSlots);
                updated = true;
            } catch (Exception ex) {
                System.out.println(" Error updating project directly: " + ex.getMessage());
                updated = false;
            }
        }
        
        // If update successful, save to data manager
        if (updated) {
            boolean saved = projectDataManager.updateProject(project);
            System.out.println("Project update saved. ");
        }
        
        return updated;
    }
    /**
    * Deletes a project from the system.
    * 
    * Handles complex deletion process including:
    * - Validating manager authorization
    * - Updating application statuses
    * - Removing project from data managers
    * 
    * @param projectId Unique identifier of the project to delete
    * @param manager HDB Manager deleting the project
    * @return true if project is successfully deleted, false otherwise
    */
    @Override
    public boolean deleteProject(String projectId, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || manager == null) {
            return false;
        }
        
        System.out.println("Attempting to delete project: " + projectId);
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println(" Project not found for deletion: " + projectId);
            return false;
        }
        
        // Check if manager is authorized to delete this project
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println(" Manager not authorized to delete this project");
            return false;
        }
        
        /**
         * IMPORTANT: Before deleting the project, directly update ApplicationList.txt
         * to set all applications for this project to UNSUCCESSFUL
         */
        try {
            System.out.println(" Updating applications for project: " + projectId);
            
            // Read the application file directly
            String applicationFilePath = utils.FilePathConfig.APPLICATION_LIST_PATH;
            List<String> fileLines = new ArrayList<>();
            boolean headerProcessed = false;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(applicationFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!headerProcessed) {
                        // Keep the header unchanged
                        fileLines.add(line);
                        headerProcessed = true;
                        continue;
                    }
                    
                    // For each application line, check if it belongs to this project
                    String[] parts = line.split("\t");
                    if (parts.length >= 3) {
                        String lineProjectName = parts[1].trim();
                        
                        // If this application is for the project being deleted,
                        // update its status to UNSUCCESSFUL
                        if (lineProjectName.equals(projectId)) {
                            System.out.println(" Found application for project " + projectId + ": " + line);
                            
                            // Construct updated line with UNSUCCESSFUL status
                            StringBuilder updatedLine = new StringBuilder();
                            updatedLine.append(parts[0]); // NRIC
                            updatedLine.append("\t").append(parts[1]); // Project Name
                            updatedLine.append("\t").append("UNSUCCESSFUL"); // Set status to UNSUCCESSFUL
                            
                            // Add remaining parts if they exist
                            for (int i = 3; i < parts.length; i++) {
                                updatedLine.append("\t").append(parts[i]);
                            }
                            
                            // Add the updated line
                            fileLines.add(updatedLine.toString());
                            System.out.println(" Updated application to UNSUCCESSFUL: " + updatedLine.toString());
                        } else {
                            // Keep other applications unchanged
                            fileLines.add(line);
                        }
                    } else {
                        // Add any malformed lines as-is
                        fileLines.add(line);
                    }
                }
            }
            
            // Write back the updated application file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(applicationFilePath))) {
                for (String line : fileLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            
            System.out.println("Successfully updated applications to UNSUCCESSFUL for deleted project");
            
        } catch (IOException e) {
            System.out.println("ERROR: Failed to update applications for deleted project: " + e.getMessage());
            e.printStackTrace();
            // Continue with project deletion even if application update fails
        }
        
        // First remove from manager's list
        boolean managerDeleted = manager.deleteProject(project);
        
        // Then remove from data manager and ensure it saves to file
        boolean dataManagerDeleted = projectDataManager.removeProject(projectId);
        
        if (managerDeleted && dataManagerDeleted) {
            System.out.println("Project successfully deleted.");
        }
        
        return managerDeleted && dataManagerDeleted;
    }

    /**
    * Toggles the visibility of a project.
    * 
    * Allows managers to show or hide projects from applicants.
    * 
    * @param projectId Unique identifier of the project
    * @param visible New visibility status
    * @param manager HDB Manager changing project visibility
    * @return true if visibility is successfully changed, false otherwise
    */
    @Override
    public boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || manager == null) {
            return false;
        }
        
        System.out.println(" Toggling visibility for project: " + projectId + " to: " + visible);
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println(" Project not found for visibility toggle: " + projectId);
            return false;
        }
        
        // Check if manager is authorized
        if (project.getManagerInCharge() == null || 
            !project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println(" Manager not authorized to toggle project visibility");
            return false;
        }
        
        // Toggle visibility
        boolean toggled = false;
        try {
            toggled = manager.toggleProjectVisibility(project, visible);
        } catch (Exception e) {
            System.out.println(" Error toggling visibility through manager: " + e.getMessage());
            
            // Update visibility directly
            project.setVisible(visible);
            toggled = projectDataManager.updateProject(project);
            System.out.println(" Project visibility toggled directly: " + toggled);
        }
        
        return toggled;
    }
    /**
    * Registers an HDB Officer for a specific project.
    * 
    * Validates officer eligibility and project registration requirements.
    * 
    * @param projectId Unique identifier of the project
    * @param officer HDB Officer to be registered
    * @return true if officer registration is successful, false otherwise
    */
    @Override
    public boolean registerOfficerForProject(String projectId, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || officer == null) {
            return false;
        }
        
        System.out.println(" Registering officer for project: " + projectId);
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println(" Project not found for officer registration: " + projectId);
            return false;
        }
        
        // Check officer eligibility
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().getProjectName().equals(projectId)) {
            System.out.println(" Officer cannot register for a project they're applying to");
            return false;
        }
        
        // Attempt officer registration
        boolean registered = false;
        try {
            registered = officer.registerForProject(project);
        } catch (Exception e) {
            System.out.println(" Error registering officer through officer method: " + e.getMessage());
            
            // Try direct registration
            registered = project.addOfficer(officer);
            if (registered) {
                projectDataManager.updateProject(project);
            }
        }
        
        System.out.println(" Officer registration result: " + registered);
        return registered;
    }

    /**
    * Retrieves a project by its unique identifier.
    * 
    * @param projectId Unique identifier of the project
    * @return The Project object, or null if not found
    */
    @Override
    public Project getProjectById(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return null;
        }
        
        Project project = projectDataManager.getProjectByName(projectId);
        if (project == null) {
            System.out.println(" Project not found by ID: " + projectId);
        } else {
            System.out.println(" Retrieved project: " + project.getProjectName());
        }
        
        return project;
    }

    /**
    * Retrieves all projects in the system.
    * 
    * @return List of all Project objects
    */
    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = projectDataManager.getAllProjects();
        return projects;
    }
    
    /**
    * Retrieves projects managed by a specific HDB Manager.
    * 
    * @param manager HDB Manager whose projects are to be retrieved
    * @return List of Project objects managed by the manager
    */
    @Override
    public List<Project> getProjectsByManager(HDBManager manager) {
        // Validate input
        if (manager == null) {
            System.out.println("Project received non-existent manager");
            return new ArrayList<>();
        }
        
        List<Project> projects = projectDataManager.getAllProjects().stream()
            .filter(p -> p.getManagerInCharge() != null && 
                   p.getManagerInCharge().getNric().equals(manager.getNric()))
            .collect(Collectors.toList());
        
        System.out.println("Found " + projects.size() + 
                          " projects for manager: " + manager.getName());
        
        return projects;
    }

    /**
    * Retrieves visible projects for a specific applicant.
    * 
    * Considers applicant's eligibility criteria such as:
    * - Marital status
    * - Age
    * - Project availability
    * 
    * @param applicant Applicant seeking project information
    * @return List of visible and eligible Project objects
    */
    @Override
    public List<Project> getVisibleProjectsForApplicant(Applicant applicant) {

        // For HDB Officers, print debug info about their assignments
    if (applicant instanceof HDBOfficer) {
        HDBOfficer officer = (HDBOfficer) applicant;
        System.out.println("Officer check - Name: " + officer.getName());
        System.out.println("Officer check - Assigned project: " + 
                        (officer.getAssignedProject() != null ? 
                        officer.getAssignedProject().getProjectName() : "none"));
        System.out.println("Officer check - Registration approved: " + 
                        officer.isRegistrationApproved());
    }

        // Validate input
        if (applicant == null) {
            System.out.println("There is no such applicant");
            return new ArrayList<>();
        }
        
        // Get projects based on applicant's eligibility
        boolean isSingle = !applicant.isMarried();
        int age = applicant.getAge();
        
        System.out.println("Getting visible projects for " + applicant.getName() + 
                         " (isSingle=" + isSingle + ", age=" + age + ")");
        
        // Get all projects first for debugging
        List<Project> allProjects = projectDataManager.getAllProjects();
        System.out.println("Total projects available: " + allProjects.size());
        
        // For each project, print detailed eligibility check
        List<Project> eligibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            System.out.println("\nChecking eligibility for project: " + project.getProjectName());
            
            // Check if applicant is an officer handling this project
            if (applicant instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) applicant;
                if (officer.isAssignedToProject(project)) {
                    System.out.println("Project failed officer eligibility check - Officer is handling this project");
                    continue;
                }
            }
            
            // Check visibility
            if (!project.isVisible()) {
                System.out.println("Project is not set to be visible by manager in charge. " + project.isVisible());
                continue;
            }
            
            // Check application period
            Date now = new Date();
            boolean inPeriod = now.after(project.getApplicationOpeningDate()) && 
                              now.before(project.getApplicationClosingDate());
            
            System.out.println("Date check - Current: " + now + 
                              ", Opening: " + project.getApplicationOpeningDate() +
                              ", Closing: " + project.getApplicationClosingDate() +
                              ", In period: " + inPeriod);
            
            if (!inPeriod) {
                System.out.println("Project failed application period check");
                continue;
            }
            
            // Check eligibility based on marital status and age
            boolean eligible = false;
            
            if (isSingle) {
                // Single applicants must be 35+ and project must have 2-Room flats
                if (age < 35) {
                    System.out.println("Single applicant too young (age: " + age + ")");
                    continue;
                }
                
                boolean has2Room = project.getFlatTypeInfoList().stream()
                    .anyMatch(info -> {
                        boolean matches = info.getFlatType() == FlatType.TWO_ROOM && 
                                      info.getNumberOfUnits() > 0;
                        System.out.println("Checking for 2-Room - FlatType: " + 
                                          info.getFlatType().getDisplayName() + 
                                          ", Units: " + info.getNumberOfUnits() +
                                          ", Matches: " + matches);
                        return matches;
                    });
                
                if (!has2Room) {
                    System.out.println("No available 2-Room flats for single applicant");
                    continue;
                }
                
                eligible = true;
            } else {
                // Married applicants must be 21+
                if (age < 21) {
                    System.out.println("Married applicant too young (age: " + age + ")");
                    continue;
                }
                
                eligible = true;
            }
            
            if (eligible) {
                System.out.println("Project is eligible for applicant");
                eligibleProjects.add(project);
            }
        }
        
        System.out.println(" Found " + eligibleProjects.size() + " eligible projects");
        return eligibleProjects;
    }
    
    /**
    * Retrieves approved officers for a specific project.
    * 
    * @param projectId Unique identifier of the project
    * @return List of approved HDB Officers for the project
    */
    @Override
    public List<HDBOfficer> getApprovedOfficersForProject(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found: " + projectId);
            return new ArrayList<>();
        }
        
        List<HDBOfficer> officers = project.getAssignedOfficers();
        System.out.println("Found " + officers.size() + 
                          " officers for project: " + projectId);
        
        return officers;
    }
    
    /**
    * Retrieves the number of remaining officer slots for a project.
    * 
    * @param projectId Unique identifier of the project
    * @return Number of remaining officer slots
    */
    @Override
    public int getRemainingOfficerSlots(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return 0;
        }
        
        Project project = getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found: " + projectId);
            return 0;
        }
        
        int remainingSlots = project.getRemainingOfficerSlots();
        System.out.println("Remaining officer slots for " + projectId + ": " + remainingSlots);
        
        return remainingSlots;
    }
    
    /**
     * Checks if a project's application period overlaps with given dates.
     * 
     * @param project The project to check
     * @param newOpeningDate The new project's opening date
     * @param newClosingDate The new project's closing date
     * @return true if periods overlap, false otherwise
     */
    private boolean isOverlappingPeriod(Project project, Date newOpeningDate, Date newClosingDate) {
        if (project == null || newOpeningDate == null || newClosingDate == null) {
            return false;
        }
        
        boolean overlapping = !(newClosingDate.before(project.getApplicationOpeningDate()) || 
                              newOpeningDate.after(project.getApplicationClosingDate()));
        
        if (overlapping) {
            System.out.println("Detected overlapping application period with project: " + 
                              project.getProjectName());
        }
        
        return overlapping;
    }
}