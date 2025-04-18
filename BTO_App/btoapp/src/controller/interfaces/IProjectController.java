package controller.interfaces;

import java.util.Date;
import java.util.List;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.FlatType;

/**
 * Interface for Project Controller in the BTO Management System.
 * Defines methods to manage BTO projects.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IProjectController {
    
    /**
     * Creates a new project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatTypes The types of flats available in the project
     * @param numberOfUnits The number of units for each flat type
     * @param sellingPrices The selling prices for each flat type
     * @param openingDate The application opening date
     * @param closingDate The application closing date
     * @param manager The manager in charge of the project
     * @param officerSlots The number of officer slots for the project
     * @return The created project if successful, null otherwise
     */
    Project createProject(String projectName, String neighborhood, 
                         List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                         List<Double> sellingPrices, Date openingDate, 
                         Date closingDate, HDBManager manager, int officerSlots);
    
    /**
     * Updates an existing project.
     * 
     * @param projectId The ID of the project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots
     * @param manager The manager updating the project
     * @return true if the update was successful, false otherwise
     */
    boolean updateProject(String projectId, String projectName, 
                         String neighborhood, Date openingDate, 
                         Date closingDate, int officerSlots, 
                         HDBManager manager);
    
    /**
     * Deletes a project.
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
     * Registers an officer for a project.
     * 
     * @param projectId The ID of the project to register for
     * @param officer The officer to register
     * @return true if the registration was successful, false otherwise
     */
    boolean registerOfficerForProject(String projectId, HDBOfficer officer);
    
    /**
     * Gets a project by its ID.
     * 
     * @param projectId The ID of the project to retrieve
     * @return The requested project if found, null otherwise
     */
    Project getProjectById(String projectId);
    
    /**
     * Gets all projects.
     * 
     * @return A list of all projects
     */
    List<Project> getAllProjects();
    
    /**
     * Gets projects by manager.
     * 
     * @param manager The manager to get projects for
     * @return A list of projects managed by the specified manager
     */
    List<Project> getProjectsByManager(HDBManager manager);
    
    /**
     * Gets projects visible to an applicant.
     * 
     * @param applicant The applicant to get visible projects for
     * @return A list of projects visible to the applicant
     */
    List<Project> getVisibleProjectsForApplicant(Applicant applicant);
    
    /**
     * Gets all approved officers for a project.
     * 
     * @param projectId The ID of the project
     * @return A list of approved officers for the project
     */
    List<HDBOfficer> getApprovedOfficersForProject(String projectId);
    
    /**
     * Gets the count of remaining officer slots for a project.
     * 
     * @param projectId The ID of the project
     * @return The number of remaining officer slots
     */
    int getRemainingOfficerSlots(String projectId);
}