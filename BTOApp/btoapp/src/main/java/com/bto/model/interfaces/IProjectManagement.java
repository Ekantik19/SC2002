package com.bto.model.interfaces;

import java.util.Date;
import java.util.List;

import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.enums.FlatType;

/**
 * Interface for managing project operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IProjectManagement {

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
     * @param manager The manager in charge of the project
     * @param officerSlots The number of officer slots for the project
     * @return true if the project was successfully created, false otherwise
     */
    boolean createProject(String projectName, String neighborhood, List<FlatType> flatTypes, 
                         List<Integer> numberOfUnits, List<Double> sellingPrices, 
                         Date openingDate, Date closingDate, HDBManager manager, int officerSlots);
    
    /**
     * Updates an existing BTO project.
     * 
     * @param project The project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param flatTypes The new types of flats available in the project
     * @param numberOfUnits The new number of units for each flat type
     * @param sellingPrices The new selling prices for each flat type
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots for the project
     * @return true if the project was successfully updated, false otherwise
     */
    boolean updateProject(Project project, String projectName, String neighborhood, 
                         List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                         List<Double> sellingPrices, Date openingDate, Date closingDate, 
                         int officerSlots);
    
    /**
     * Deletes a BTO project.
     * 
     * @param project The project to delete
     * @return true if the project was successfully deleted, false otherwise
     */
    boolean deleteProject(Project project);
    
    /**
     * Toggles the visibility of a BTO project.
     * 
     * @param project The project to toggle visibility for
     * @param visible The new visibility status
     * @return true if the visibility was successfully toggled, false otherwise
     */
    boolean toggleProjectVisibility(Project project, boolean visible);
    
    /**
     * Adds an HDB officer to a project.
     * 
     * @param project The project to add the officer to
     * @param officer The officer to add
     * @return true if the officer was successfully added, false otherwise
     */
    boolean addOfficerToProject(Project project, HDBOfficer officer);
    
    /**
     * Removes an HDB officer from a project.
     * 
     * @param project The project to remove the officer from
     * @param officer The officer to remove
     * @return true if the officer was successfully removed, false otherwise
     */
    boolean removeOfficerFromProject(Project project, HDBOfficer officer);
    
    /**
     * Gets a list of all projects.
     * 
     * @return A list of all projects
     */
    List<Project> getAllProjects();
    
    /**
     * Gets a list of projects by manager.
     * 
     * @param manager The manager to get projects for
     * @return A list of projects managed by the specified manager
     */
    List<Project> getProjectsByManager(HDBManager manager);
    
    /**
     * Gets a list of projects by neighborhood.
     * 
     * @param neighborhood The neighborhood to get projects for
     * @return A list of projects in the specified neighborhood
     */
    List<Project> getProjectsByNeighborhood(String neighborhood);
    
    /**
     * Gets a list of projects by flat type.
     * 
     * @param flatType The flat type to get projects for
     * @return A list of projects offering the specified flat type
     */
    List<Project> getProjectsByFlatType(FlatType flatType);

}
