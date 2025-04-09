package com.bto.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IProjectController;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.enums.FlatType;
import com.bto.service.EligibilityCheckerService;

/**
 * Controller for managing projects in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ProjectController extends ABaseController implements IProjectController {
    
    private Map<String, Project> projectMap;
    private EligibilityCheckerService eligibilityService;
    
    /**
     * Constructor for ProjectController.
     */
    public ProjectController() {
        this.projectMap = new HashMap<>();
        this.eligibilityService = new EligibilityCheckerService();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Project createProject(String projectName, String neighborhood, 
                               List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                               List<Double> sellingPrices, Date openingDate, 
                               Date closingDate, HDBManager manager, int officerSlots) {
        
        // Validate inputs
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
        
        // Check if flat types, units and prices have the same size
        if (flatTypes.size() != numberOfUnits.size() || flatTypes.size() != sellingPrices.size()) {
            System.out.println("Flat types, number of units, and selling prices must have the same size");
            return null;
        }
        
        // Check if project with same name already exists
        for (Project existingProject : projectMap.values()) {
            if (existingProject.getProjectName().equalsIgnoreCase(projectName)) {
                System.out.println("Project with this name already exists");
                return null;
            }
        }
        
        // Create project using the manager's createProject method
        Project project = manager.createProject(projectName, neighborhood, flatTypes, 
                                               numberOfUnits, sellingPrices, 
                                               openingDate, closingDate, officerSlots);
        
        if (project != null) {
            // Generate a project ID
            String projectId = generateProjectId(projectName);
            
            // Add to project map
            projectMap.put(projectId, project);
        }
        
        return project;
    }
    
    /**
     * Helper method to generate a project ID.
     * 
     * @param projectName The name of the project
     * @return A unique project ID
     */
    private String generateProjectId(String projectName) {
        // Simple ID generation - in a real system, this would be more sophisticated
        return "PRJ-" + projectName.substring(0, Math.min(5, projectName.length())).toUpperCase() + 
               "-" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateProject(String projectId, String projectName, 
                               String neighborhood, Date openingDate, 
                               Date closingDate, int officerSlots, 
                               HDBManager manager) {
        
        // Validate inputs
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(projectName, "Project Name") || 
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") || 
            !validateNotNull(openingDate, "Opening Date") || 
            !validateNotNull(closingDate, "Closing Date") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Get project
        Project project = projectMap.get(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }
        
        // Update project using the manager's updateProject method
        return manager.updateProject(project, projectName, neighborhood, 
                                    openingDate, closingDate, officerSlots);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(String projectId, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Get project
        Project project = projectMap.get(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }
        
        // Delete project using the manager's deleteProject method
        boolean deleted = manager.deleteProject(project);
        
        if (deleted) {
            // Remove from project map
            projectMap.remove(projectId);
        }
        
        return deleted;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Get project
        Project project = projectMap.get(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }
        
        // Toggle visibility using the manager's toggleProjectVisibility method
        return manager.toggleProjectVisibility(project, visible);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerOfficerForProject(String projectId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        // Get project
        Project project = projectMap.get(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }
        
        // Register officer using the officer's registerForProject method
        return officer.registerForProject(project);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean approveOfficerRegistration(String officerNric, HDBManager manager) {
        if (!validateNotNullOrEmpty(officerNric, "Officer NRIC") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Find the officer
        HDBOfficer officer = null;
        for (Project project : manager.getCreatedProjects()) {
            for (HDBOfficer registeredOfficer : project.getAssignedOfficers()) {
                if (registeredOfficer.getNric().equals(officerNric) && 
                    !registeredOfficer.isRegistrationApproved()) {
                    officer = registeredOfficer;
                    break;
                }
            }
            if (officer != null) break;
        }
        
        if (officer == null) {
            System.out.println("Officer not found or already approved");
            return false;
        }
        
        // Approve registration using the manager's approveOfficerRegistration method
        return manager.approveOfficerRegistration(officer);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rejectOfficerRegistration(String officerNric, HDBManager manager) {
        if (!validateNotNullOrEmpty(officerNric, "Officer NRIC") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Find the officer
        HDBOfficer officer = null;
        for (Project project : manager.getCreatedProjects()) {
            for (HDBOfficer registeredOfficer : project.getAssignedOfficers()) {
                if (registeredOfficer.getNric().equals(officerNric) && 
                    !registeredOfficer.isRegistrationApproved()) {
                    officer = registeredOfficer;
                    break;
                }
            }
            if (officer != null) break;
        }
        
        if (officer == null) {
            System.out.println("Officer not found or already approved");
            return false;
        }
        
        // Reject registration using the manager's rejectOfficerRegistration method
        return manager.rejectOfficerRegistration(officer);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Project getProjectById(String projectId) {
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return null;
        }
        
        return projectMap.get(projectId);
    }
    //add hava doc
    public Project getProjectByName(String projectName) {
        if (!validateNotNullOrEmpty(projectName, "Project Name")) {
            return null;
        }
        
        return projectMap.values().stream()
            .filter(p -> p.getProjectName().equalsIgnoreCase(projectName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getAllProjects() {
        return new ArrayList<>(projectMap.values());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getProjectsByManager(HDBManager manager) {
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        return manager.getCreatedProjects();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getProjectsByNeighborhood(String neighborhood) {
        if (!validateNotNullOrEmpty(neighborhood, "Neighborhood")) {
            return new ArrayList<>();
        }
        
        return projectMap.values().stream()
            .filter(p -> p.getNeighborhood().equalsIgnoreCase(neighborhood))
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getProjectsByFlatType(FlatType flatType) {
        if (!validateNotNull(flatType, "Flat Type")) {
            return new ArrayList<>();
        }
        
        return projectMap.values().stream()
            .filter(p -> {
                for (Project.FlatTypeInfo info : p.getFlatTypeInfoList()) {
                    if (info.getFlatType() == flatType) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getVisibleProjectsForApplicant(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        // Check if applicant is eligible for BTO
        if (!eligibilityService.isEligibleForBTO(applicant)) {
            return new ArrayList<>();
        }
        
        return projectMap.values().stream()
            .filter(p -> p.isVisible() && p.isOpenForApplications())
            .filter(p -> {
                // Check if there's at least one flat type the applicant is eligible for
                for (Project.FlatTypeInfo info : p.getFlatTypeInfoList()) {
                    if (eligibilityService.isEligibleForFlatType(applicant, info.getFlatType())) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }
}