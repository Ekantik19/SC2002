package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import controller.abstracts.ABaseController;
import controller.interfaces.IProjectController;
import datamanager.ProjectDataManager;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.FlatType;

/**
 * Controller for managing BTO projects in the system.
 * Implements IProjectController and extends ABaseController.
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
            openingDate == null || closingDate == null || manager == null) {
            return false;
        }
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        // Delegate update to manager
        boolean updated = manager.updateProject(project, projectName, neighborhood, 
                                               openingDate, closingDate, officerSlots);
        
        // If update successful, save to data manager
        if (updated) {
            projectDataManager.updateProject(project);
        }
        
        return updated;
    }
    
    @Override
    public boolean deleteProject(String projectId, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || manager == null) {
            return false;
        }
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        // Delegate deletion to manager
        boolean deleted = manager.deleteProject(project);
        
        // If deletion successful, remove from data manager
        if (deleted) {
            projectDataManager.removeProject(projectId);
        }
        
        return deleted;
    }
    
    @Override
    public boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || manager == null) {
            return false;
        }
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        // Delegate visibility toggle to manager
        return manager.toggleProjectVisibility(project, visible);
    }
    
    @Override
    public boolean registerOfficerForProject(String projectId, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || officer == null) {
            return false;
        }
        
        // Find the project
        Project project = getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        // Check officer eligibility
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().getProjectName().equals(projectId)) {
            System.out.println("Officer cannot register for a project they are applying to.");
            return false;
        }
        
        // Attempt officer registration
        return officer.registerForProject(project);
    }
    
    @Override
    public Project getProjectById(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return null;
        }
        
        return projectDataManager.getProjectByName(projectId);
    }
    
    @Override
    public List<Project> getAllProjects() {
        return projectDataManager.getAllProjects();
    }
    
    @Override
    public List<Project> getProjectsByManager(HDBManager manager) {
        // Validate input
        if (manager == null) {
            return new ArrayList<>();
        }
        
        return projectDataManager.getAllProjects().stream()
            .filter(p -> p.getManagerInCharge().getNric().equals(manager.getNric()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> getProjectsByNeighborhood(String neighborhood) {
        // Validate input
        if (!validateNotNullOrEmpty(neighborhood, "Neighborhood")) {
            return new ArrayList<>();
        }
        
        return projectDataManager.getAllProjects().stream()
            .filter(p -> p.getNeighborhood().equalsIgnoreCase(neighborhood))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> getProjectsByFlatType(FlatType flatType) {
        // Validate input
        if (flatType == null) {
            return new ArrayList<>();
        }
        
        return projectDataManager.getAllProjects().stream()
            .filter(p -> p.getFlatTypeInfoList().stream()
                .anyMatch(info -> info.getFlatType() == flatType))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Project> getVisibleProjectsForApplicant(Applicant applicant) {
        // Validate input
        if (applicant == null) {
            return new ArrayList<>();
        }
        
        return projectDataManager.getAllProjects().stream()
            .filter(p -> p.isVisible() && 
                        (applicant.isMarried() || 
                         (applicant.getAge() >= 35 && 
                          p.getFlatTypeInfoList().stream()
                           .anyMatch(info -> info.getFlatType() == FlatType.TWO_ROOM))))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<HDBOfficer> getApprovedOfficersForProject(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        Project project = getProjectById(projectId);
        return project != null ? project.getAssignedOfficers() : new ArrayList<>();
    }
    
    @Override
    public int getRemainingOfficerSlots(String projectId) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return 0;
        }
        
        Project project = getProjectById(projectId);
        return project != null ? project.getRemainingOfficerSlots() : 0;
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

