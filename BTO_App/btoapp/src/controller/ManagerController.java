package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IManagerController;
import datamanager.ManagerDataManager;
import datamanager.OfficerDataManager;
import datamanager.ProjectDataManager;
import java.util.ArrayList;
import java.util.List;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;

/**
 * Controller for managing HDB Manager operations in the BTO system.
 * Focuses on project management and officer registration approval.
 * 
 * Handles key manager-related functionalities:
 * - Officer registration approval
 * - Managing officer assignments to projects
 * 
 * @author name
 * @version 1.0
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
    
    /**
    * Approves an HDB Officer's registration for a specific project.
    * 
    * Validates that:
    * - The officer has a pending registration
    * - The manager is in charge of the officer's assigned project
    * 
    * @param officer The HDB Officer being registered
    * @param manager The HDB Manager approving the registration
    * @return true if registration is successfully approved, false otherwise
    */
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
    
    /**
    * Retrieves a list of officers with pending registration for a specific project.
    * 
    * @param projectId Unique identifier of the project
    * @return List of HDB Officers awaiting registration approval
    */
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
}