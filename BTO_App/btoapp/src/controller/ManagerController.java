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