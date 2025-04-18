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