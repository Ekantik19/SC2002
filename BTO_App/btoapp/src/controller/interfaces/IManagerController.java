package controller.interfaces;

import java.util.List;
import model.HDBManager;
import model.HDBOfficer;

/**
 * Interface for Manager Controller in the BTO Management System.
 * Defines methods for project management and officer registration approval.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IManagerController {
    
    
    /**
     * Approves an officer's registration for a project.
     * 
     * @param officer The officer whose registration to approve
     * @param manager The manager approving the registration
     * @return true if the approval was successful, false otherwise
     */
    boolean approveOfficerRegistration(HDBOfficer officer, HDBManager manager);
    
    /**
     * Gets all officers with pending registration for a specific project.
     * 
     * @param projectId The ID of the project to get pending officers for
     * @return A list of officers with pending registration for the project
     */
    List<HDBOfficer> getPendingOfficersForProject(String projectId);
}