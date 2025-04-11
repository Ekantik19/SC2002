package com.bto.controller;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IBookingController;
import com.bto.datamanager.ApplicationDataManager;
import com.bto.datamanager.ProjectDataManager;
import com.bto.model.Application;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;
import com.bto.model.enums.ApplicationStatus;
import com.bto.model.enums.FlatType;

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
        
        // Book the flat
        boolean booked = application.bookFlat();
        
        if (booked) {
            // Update applicant's booked flat information
            application.getApplicant().setBookedFlatType(flatType);
            application.getApplicant().setBookedProject(project);
            
            // Update flat availability
            updateFlatAvailability(project.getProjectName(), flatType);
            
            // Save application changes
            applicationDataManager.updateApplication(application);
        }
        
        return booked;
    }
    
    @Override
    public Receipt generateBookingReceipt(String applicationId, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return null;
        }
        
        // Retrieve the application
        Application application = applicationDataManager.getApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return null;
        }
        
        // Check if officer is assigned to the project
        if (!officer.isAssignedToProject(application.getProject())) {
            System.out.println("Officer is not assigned to this project.");
            return null;
        }
        
        // Check if application is in BOOKED status
        if (application.getStatus() != ApplicationStatus.BOOKED) {
            System.out.println("Application is not in BOOKED status. Cannot generate receipt.");
            return null;
        }
        
        // Generate receipt
        return new Receipt(
            application.getApplicationId(),
            application.getApplicant().getName(),
            application.getApplicant().getNric(),
            application.getApplicant().getAge(),
            application.getApplicant().getMaritalStatus(),
            application.getSelectedFlatType(),
            application.getProject().getProjectName(),
            application.getProject().getNeighborhood()
        );
    }
    
    @Override
    public Application getBookingDetails(String applicationId) {
        // Validate input
        if (!validateNotNullOrEmpty(applicationId, "Application ID")) {
            return null;
        }
        
        Application application = applicationDataManager.getApplicationById(applicationId);
        
        // Only return the application if it's in BOOKED status
        if (application != null && application.getStatus() == ApplicationStatus.BOOKED) {
            return application;
        } else if (application != null) {
            System.out.println("Application is not in BOOKED status.");
        } else {
            System.out.println("Application not found.");
        }
        
        return null;
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
                // Create new flat type info with decremented units
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