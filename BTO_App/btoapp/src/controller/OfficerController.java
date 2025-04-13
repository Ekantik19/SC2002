package controller;

import java.util.ArrayList;
import java.util.List;

import controller.abstracts.ABaseController;
import controller.interfaces.IOfficerController;
import datamanager.ApplicationDataManager;
import datamanager.OfficerDataManager;
import model.Application;
import model.HDBOfficer;
import model.Project;
import model.Receipt;
import model.enums.ApplicationStatus;

/**
 * Controller for managing HDB Officer operations in the BTO system.
 * Focuses on officer registrations, flat booking, and receipt generation.
 */
public class OfficerController extends ABaseController implements IOfficerController {
    
    private OfficerDataManager officerDataManager;
    private ApplicationDataManager applicationDataManager;
    
    /**
     * Constructor for OfficerController.
     * 
     * @param officerDataManager The data manager for officer operations
     * @param applicationDataManager The data manager for application operations
     */
    public OfficerController(OfficerDataManager officerDataManager, ApplicationDataManager applicationDataManager) {
        this.officerDataManager = officerDataManager;
        this.applicationDataManager = applicationDataManager;
    }
    
    @Override
    public boolean registerForProject(HDBOfficer officer, Project project) {
        // Validate input parameters
        if (!validateNotNull(officer, "Officer") || !validateNotNull(project, "Project")) {
            return false;
        }
        
        // Check if officer is already registered for a project
        if (officer.getAssignedProject() != null) {
            System.out.println("Officer is already registered for a project.");
            return false;
        }
        
        // Check if officer is applying for this project as an Applicant
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().getProjectName().equals(project.getProjectName())) {
            System.out.println("Officer cannot register for a project they are applying to.");
            return false;
        }
        
        // Check if project has available officer slots
        if (project.getRemainingOfficerSlots() <= 0) {
            System.out.println("No available officer slots for this project.");
            return false;
        }
        
        // Register for project
        boolean registered = officer.registerForProject(project);
        
        // Update officer in data manager
        if (registered) {
            officerDataManager.updateOfficer(officer);
        }
        
        return registered;
    }
    
    @Override
    public boolean getRegistrationStatus(HDBOfficer officer) {
        // Validate input
        if (!validateNotNull(officer, "Officer")) {
            return false;
        }
        
        return officer.isRegistrationApproved();
    }
    
    @Override
    public boolean bookFlat(Application application, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNull(application, "Application") || !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        // Check if officer is assigned to the project
        if (!officer.isAssignedToProject(application.getProject())) {
            System.out.println("Officer is not assigned to this project.");
            return false;
        }
        
        // Check if application is in SUCCESSFUL status
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Application is not in SUCCESSFUL status. Cannot book flat.");
            return false;
        }
        
        // Book flat
        boolean booked = officer.bookFlat(application);
        
        // Update application in data manager if booking was successful
        if (booked) {
            applicationDataManager.updateApplication(application);
        }
        
        return booked;
    }
    
    @Override
    public Receipt generateBookingReceipt(Application application, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNull(application, "Application") || !validateNotNull(officer, "Officer")) {
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
        
        return officer.generateBookingReceipt(application);
    }
    
    @Override
    public Application retrieveApplicationByNric(String applicantNric, HDBOfficer officer) {
        // Validate input parameters
        if (!validateNotNullOrEmpty(applicantNric, "Applicant NRIC") || !validateNotNull(officer, "Officer")) {
            return null;
        }
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            System.out.println("Officer is not assigned to any project.");
            return null;
        }
        
        // Retrieve application
        return officer.retrieveApplicationByNric(applicantNric);
    }
    
    @Override
    public List<Project> getAssignedProjects(HDBOfficer officer) {
        // Validate input
        if (!validateNotNull(officer, "Officer")) {
            return new ArrayList<>();
        }
        
        List<Project> assignedProjects = new ArrayList<>();
        
        // Add the assigned project if any
        if (officer.isProjectAssigned() && officer.getAssignedProject() != null) {
            assignedProjects.add(officer.getAssignedProject());
        }
        
        return assignedProjects;
    }
}