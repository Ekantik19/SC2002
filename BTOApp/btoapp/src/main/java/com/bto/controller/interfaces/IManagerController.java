package com.bto.controller.interfaces;

import java.util.Date;
import java.util.List;

import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.enums.FlatType;

/**
 * Interface for Manager Controller in the BTO Management System.
 * Defines methods for project management and officer registration approval.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IManagerController {
    
    /**
     * Creates a new BTO project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatTypes The types of flats available in the project
     * @param numberOfUnits The number of units for each flat type
     * @param sellingPrices The selling prices for each flat type
     * @param openingDate The application opening date
     * @param closingDate The application closing date
     * @param manager The manager creating the project
     * @param officerSlots The number of officer slots for the project
     * @return The created project if successful, null otherwise
     */
    Project createProject(String projectName, String neighborhood, 
                        List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                        List<Double> sellingPrices, Date openingDate, 
                        Date closingDate, HDBManager manager, int officerSlots);
    
    /**
     * Updates an existing BTO project.
     * 
     * @param projectId The ID of the project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots for the project
     * @param manager The manager updating the project
     * @return true if the update was successful, false otherwise
     */
    boolean updateProject(String projectId, String projectName, 
                        String neighborhood, Date openingDate, 
                        Date closingDate, int officerSlots, 
                        HDBManager manager);
    
    /**
     * Deletes a BTO project.
     * 
     * @param projectId The ID of the project to delete
     * @param manager The manager deleting the project
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteProject(String projectId, HDBManager manager);
    
    /**
     * Toggles the visibility of a project.
     * 
     * @param projectId The ID of the project to toggle visibility for
     * @param visible The new visibility status
     * @param manager The manager toggling the visibility
     * @return true if the visibility was successfully toggled, false otherwise
     */
    boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager);
    
    /**
     * Gets all projects created by a specific manager.
     * 
     * @param manager The manager to get projects for
     * @return A list of projects created by the manager
     */
    List<Project> getProjectsByManager(HDBManager manager);
    
    /**
     * Gets all projects in the system.
     * 
     * @return A list of all projects
     */
    List<Project> getAllProjects();
    
    /**
     * Approves an officer's registration for a project.
     * 
     * @param officer The officer whose registration to approve
     * @param manager The manager approving the registration
     * @return true if the approval was successful, false otherwise
     */
    boolean approveOfficerRegistration(HDBOfficer officer, HDBManager manager);
    
    /**
     * Rejects an officer's registration for a project.
     * 
     * @param officer The officer whose registration to reject
     * @param manager The manager rejecting the registration
     * @return true if the rejection was successful, false otherwise
     */
    boolean rejectOfficerRegistration(HDBOfficer officer, HDBManager manager);
    
    /**
     * Gets all officers with pending registration for a specific project.
     * 
     * @param projectId The ID of the project to get pending officers for
     * @return A list of officers with pending registration for the project
     */
    List<HDBOfficer> getPendingOfficersForProject(String projectId);
    
    /**
     * Gets all officers with approved registration for a specific project.
     * 
     * @param projectId The ID of the project to get approved officers for
     * @return A list of officers with approved registration for the project
     */
    List<HDBOfficer> getApprovedOfficersForProject(String projectId);
}