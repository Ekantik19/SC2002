package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IBookingController;
import datamanager.ApplicationDataManager;
import datamanager.ProjectDataManager;
import java.util.Date;
import model.Applicant;
import model.Application;
import model.HDBOfficer;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

/**
 * Controller for managing flat bookings in the BTO system.
 * Implements IBookingController and extends ABaseController.
 * 
 * 
package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IBookingController;
import datamanager.ApplicationDataManager;
import datamanager.ProjectDataManager;
import java.util.Date;
import model.Applicant;
import model.Application;
import model.HDBOfficer;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

/**
 * Controller for managing flat bookings in the BTO (Build-To-Order) system.
 * 
 * Responsible for:
 * - Booking flats for successful applicants
 * - Updating flat availability
 * - Managing the booking process for HDB Officers
 * 
 * @author Your Name
 * @version 1.0
 */
public class BookingController extends ABaseController implements IBookingController {
    
    private ApplicationDataManager applicationDataManager;
    private ProjectDataManager projectDataManager;
    
    /**
     * Constructor for BookingController.
     * 
     * @param applicationDataManager The data manager for application operations
     * @param projectDataManager The data manager for project operations
     */
    public BookingController(ApplicationDataManager applicationDataManager, ProjectDataManager projectDataManager) {
        this.applicationDataManager = applicationDataManager;
        this.projectDataManager = projectDataManager;
    }

    /**
     * Books a flat for an applicant through an HDB Officer.
     * 
     * Validates booking conditions such as:
     * - Applicant has not already booked a flat
     * - Officer is assigned to the project
     * - Application is in SUCCESSFUL status
     * - Flat type is still available
     * 
     * @param applicationId Unique identifier of the application
     * @param officer HDB Officer processing the booking
     * @return true if the flat is successfully booked, false otherwise
     */
    @Override
    public boolean bookFlat(String applicationId, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        // Retrieve the application
        Application application = applicationDataManager.getApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return false;
        }
        
        // Retrieve the applicant
        Applicant applicant = application.getApplicant();
        
        // Check if applicant has already booked a flat
        if (applicant.hasBookedFlat()) {
            System.out.println("Applicant has already booked a flat and cannot book another.");
            return false;
        }
        
        // Check if officer is assigned to the project
        Project project = application.getProject();
        if (!officer.isAssignedToProject(project)) {
            System.out.println("Officer is not assigned to this project.");
            return false;
        }
        
        // Check if application is in SUCCESSFUL status
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Application is not in SUCCESSFUL status. Cannot book flat.");
            return false;
        }
        
        // Check if the flat type is still available
        FlatType flatType = application.getSelectedFlatType();
        if (!project.hasAvailableUnits(flatType)) {
            System.out.println("No available units for the selected flat type.");
            return false;
        }

        System.out.println("DEBUG: Attempting to book flat for application: " + applicationId);
        
        // Book the flat
        boolean booked = application.bookFlat();
        
        if (booked) {
            application.setBookingDate(new Date());
            // Update applicant's booked flat information
            applicant.setBookedFlatType(flatType);
            applicant.setBookedProject(project);
            
            // Update flat availability
            updateFlatAvailability(project.getProjectName(), flatType);
            
            // Save changes
            applicationDataManager.updateAndSaveApplication(application);
            projectDataManager.updateProject(project);
        }
        
        System.out.println("DEBUG: Booking result: " + booked);
        return booked;
    }
    
    /**
     * Updates the availability of flats for a specific project and flat type.
     * 
     * Decrements the number of available units for the specified flat type
     * and updates the project's flat type information.
     * 
     * @param projectId Unique identifier of the project
     * @param flatType Type of flat being booked
     * @return true if flat availability is successfully updated, false otherwise
     */
    @Override
    public boolean updateFlatAvailability(String projectId, FlatType flatType) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(flatType, "Flat Type")) {
            return false;
        }
        
        // Get the project
        Project project = projectDataManager.getProjectByName(projectId);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }
        
        // Update available units
        boolean updated = false;
        for (Project.FlatTypeInfo flatTypeInfo : project.getFlatTypeInfoList()) {
            if (flatTypeInfo.getFlatType() == flatType && flatTypeInfo.getNumberOfUnits() > 0) {
                // Decrement units by 1, not all units
                int newUnits = flatTypeInfo.getNumberOfUnits() - 1;
                double price = flatTypeInfo.getSellingPrice();
                
                // Remove old flat type info
                project.getFlatTypeInfoList().remove(flatTypeInfo);
                
                // Add new flat type info with updated units
                project.addFlatType(flatType, newUnits, price);
                
                updated = true;
                break;
            }
        }
        
        // Update project in data manager
        if (updated) {
            projectDataManager.updateProject(project);
        } else {
            System.out.println("No available units to update.");
        }
        
        return updated;
    }
}