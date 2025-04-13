package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import controller.abstracts.ABaseController;
import controller.interfaces.IManagerController;
import datamanager.ManagerDataManager;
import datamanager.OfficerDataManager;
import datamanager.ProjectDataManager;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.FlatType;

/**
 * Controller for managing HDB Manager operations in the BTO system.
 * Focuses on project management and officer registration approval.
 */
public class ManagerController extends ABaseController implements IManagerController {
    
    private ManagerDataManager managerDataManager;
    private ProjectDataManager projectDataManager;
    private OfficerDataManager officerDataManager;
    
    /**
     * Constructor for ManagerController.
     * 
     * @param managerDataManager The data manager for manager operations
     * @param projectDataManager The data manager for project operations
     * @param officerDataManager The data manager for officer operations
     */
    public ManagerController(ManagerDataManager managerDataManager, 
                           ProjectDataManager projectDataManager,
                           OfficerDataManager officerDataManager) {
        this.managerDataManager = managerDataManager;
        this.projectDataManager = projectDataManager;
        this.officerDataManager = officerDataManager;
    }
    
    @Override
    public Project createProject(String projectName, String neighborhood, 
                               List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                               List<Double> sellingPrices, Date openingDate, 
                               Date closingDate, HDBManager manager, int officerSlots) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectName, "Project Name") || 
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") ||
            !validateNotNull(flatTypes, "Flat Types") ||
            !validateNotNull(numberOfUnits, "Number of Units") ||
            !validateNotNull(sellingPrices, "Selling Prices") ||
            !validateNotNull(openingDate, "Opening Date") ||
            !validateNotNull(closingDate, "Closing Date") ||
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Validate input lists have the same size
        if (flatTypes.size() != numberOfUnits.size() || 
            flatTypes.size() != sellingPrices.size()) {
            System.out.println("Flat types, number of units, and selling prices must have the same size.");
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
        
        // Create project through manager
        Project project = manager.createProject(projectName, neighborhood, 
                                              flatTypes, numberOfUnits, 
                                              sellingPrices, openingDate, 
                                              closingDate, officerSlots);
        
        // Add project to data manager
        if (project != null) {
            projectDataManager.addProject(project);
        }
        
        return project;
    }
    
    @Override
    public boolean updateProject(String projectId, String projectName, 
                               String neighborhood, Date openingDate, 
                               Date closingDate, int officerSlots, 
                               HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") ||
            !validateNotNullOrEmpty(projectName, "Project Name") ||
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") ||
            !validateNotNull(openingDate, "Opening Date") ||
            !validateNotNull(closingDate, "Closing Date") ||
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Find the project
        Project project = projectDataManager.getProjectByName(projectId);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }
        
        // Validate manager is in charge of project
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return false;
        }
        
        // Update project through manager
        boolean updated = manager.updateProject(project, projectName, neighborhood, 
                                              openingDate, closingDate, officerSlots);
        
        // Update in data manager if successful
        if (updated) {
            projectDataManager.updateProject(project);
        }
        
        return updated;
    }
    
    @Override
    public boolean deleteProject(String projectId, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Find the project
        Project project = projectDataManager.getProjectByName(projectId);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }
        
        // Validate manager is in charge of project
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return false;
        }
        
        // Delete project through manager
        boolean deleted = manager.deleteProject(project);
        
        // Remove from data manager if successful
        if (deleted) {
            projectDataManager.removeProject(projectId);
        }
        
        return deleted;
    }
    
    @Override
    public boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Find the project
        Project project = projectDataManager.getProjectByName(projectId);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }
        
        // Validate manager is in charge of project
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return false;
        }
        
        // Toggle visibility through manager
        boolean toggled = manager.toggleProjectVisibility(project, visible);
        
        // Update in data manager if successful
        if (toggled) {
            projectDataManager.updateProject(project);
        }
        
        return toggled;
    }
    
    @Override
    public List<Project> getProjectsByManager(HDBManager manager) {
        // Validate input
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        return projectDataManager.getAllProjects().stream()
            .filter(p -> p.getManagerInCharge().getNric().equals(manager.getNric()))
            .toList();
    }
    
    @Override
    public List<Project> getAllProjects() {
        return projectDataManager.getAllProjects();
    }
    
    @Override
    public boolean approveOfficerRegistration(HDBOfficer officer, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNull(officer, "Officer") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Check if officer has a pending registration
        if (officer.getAssignedProject() == null || officer.isRegistrationApproved()) {
            System.out.println("Officer does not have a pending registration to approve.");
            return false;
        }
        
        // Check if manager is in charge of the project
        Project project = officer.getAssignedProject();
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return false;
        }
        
        // Approve registration through manager
        boolean approved = manager.approveOfficerRegistration(officer);
        
        // Update officer in data manager if successful
        if (approved) {
            officerDataManager.updateOfficer(officer);
            // Also update the project as it now has an assigned officer
            projectDataManager.updateProject(project);
        }
        
        return approved;
    }
    
    @Override
    public boolean rejectOfficerRegistration(HDBOfficer officer, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNull(officer, "Officer") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Check if officer has a pending registration
        if (officer.getAssignedProject() == null || officer.isRegistrationApproved()) {
            System.out.println("Officer does not have a pending registration to reject.");
            return false;
        }
        
        // Check if manager is in charge of the project
        Project project = officer.getAssignedProject();
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return false;
        }
        
        // Reject registration through manager
        boolean rejected = manager.rejectOfficerRegistration(officer);
        
        // Update officer in data manager if successful
        if (rejected) {
            officerDataManager.updateOfficer(officer);
        }
        
        return rejected;
    }
    
    @Override
    public List<HDBOfficer> getPendingOfficersForProject(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        // Get all officers
        List<HDBOfficer> allOfficers = officerDataManager.getAllOfficers();
        
        // Filter officers with pending registration for this project
        return allOfficers.stream()
            .filter(o -> o.getAssignedProject() != null &&
                      o.getAssignedProject().getProjectName().equals(projectId) &&
                      !o.isRegistrationApproved())
            .toList();
    }
    
    @Override
    public List<HDBOfficer> getApprovedOfficersForProject(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        // Get all officers
        List<HDBOfficer> allOfficers = officerDataManager.getAllOfficers();
        
        // Filter officers with approved registration for this project
        return allOfficers.stream()
            .filter(o -> o.getAssignedProject() != null &&
                      o.getAssignedProject().getProjectName().equals(projectId) &&
                      o.isRegistrationApproved())
            .toList();
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
        return !(newClosingDate.before(project.getApplicationOpeningDate()) || 
                 newOpeningDate.after(project.getApplicationClosingDate()));
    }
}