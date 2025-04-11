package com.bto.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IApplicationController;
import com.bto.datamanager.ApplicantDataManager;
import com.bto.datamanager.ApplicationDataManager;
import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.Project;
import com.bto.model.enums.ApplicationStatus;
import com.bto.model.enums.FlatType;
import com.bto.service.EligibilityCheckerService;

/**
 * Controller for managing BTO applications in the system.
 * Implements IApplicationController and extends ABaseController.
 */
public class ApplicationController extends ABaseController implements IApplicationController {
    
    private ApplicationDataManager applicationDataManager;
    private ApplicantDataManager applicantDataManager;
    private EligibilityCheckerService eligibilityService;
    
    /**
     * Constructor for ApplicationController.
     * 
     * @param applicationDataManager The data manager for application operations
     * @param applicantDataManager The data manager for applicant operations
     * @param eligibilityService The service for checking applicant eligibility
     */
    public ApplicationController(
            ApplicationDataManager applicationDataManager, 
            ApplicantDataManager applicantDataManager,
            EligibilityCheckerService eligibilityService) {
        this.applicationDataManager = applicationDataManager;
        this.applicantDataManager = applicantDataManager;
        this.eligibilityService = eligibilityService;
    }
    
    @Override
    public Application submitApplication(Applicant applicant, Project project, FlatType flatType) {
        // Validate input parameters
        if (!validateInputForSubmission(applicant, project, flatType)) {
            return null;
        }
        
        // Create new application
        Application application = new Application(null, applicant, project, flatType);
        
        // Add to data manager
        if (applicationDataManager.addApplication(application)) {
            // Update applicant's current application
            applicant.setCurrentApplication(application);
            applicantDataManager.updateApplicant(applicant);
            
            // Add application to project
            project.addApplication(application);
            
            return application;
        }
        
        return null;
    }
    
    @Override
    public Application viewApplication(String applicationId) {
        // Validate input
        if (!validateNotNullOrEmpty(applicationId, "Application ID")) {
            return null;
        }
        
        return applicationDataManager.getApplicationById(applicationId);
    }
    
    @Override
    public boolean requestWithdrawal(String applicationId, Applicant applicant) {
        // Get the application and validate ownership
        Application application = getAndValidateApplicationOwnership(applicationId, applicant);
        if (application == null) {
            return false;
        }
        
        // Request withdrawal
        boolean requested = application.requestWithdrawal();
        
        // Update application in data manager if withdrawal was requested
        if (requested) {
            applicationDataManager.updateApplication(application);
        }
        
        return requested;
    }
    
    @Override
    public List<Application> getApplicationsByProject(Project project) {
        // Validate input
        if (!validateNotNull(project, "Project")) {
            return new ArrayList<>();
        }
        
        return applicationDataManager.getApplicationsByProject(project.getProjectName());
    }
    
    @Override
    public List<Application> getApplicationsByApplicant(Applicant applicant) {
        // Validate input
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        return applicationDataManager.getApplicationsByApplicant(applicant.getNric());
    }
    
    @Override
    public List<Application> getApplicationsByStatus(Project project, ApplicationStatus status) {
        // Validate input
        if (!validateNotNull(project, "Project") || !validateNotNull(status, "Status")) {
            return new ArrayList<>();
        }
        
        // Get all applications for the project
        List<Application> projectApplications = getApplicationsByProject(project);
        
        // Filter by status
        return projectApplications.stream()
                .filter(app -> app.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean approveApplication(String applicationId, HDBManager manager) {
        // Get the application and validate manager authorization
        Application application = getAndValidateManagerAuthorization(applicationId, manager);
        if (application == null) {
            return false;
        }
        
        // Check if there are available units for the selected flat type
        if (!application.getProject().hasAvailableUnits(application.getSelectedFlatType())) {
            System.out.println("No available units for the selected flat type.");
            return false;
        }
        
        // Approve the application
        boolean approved = application.approve();
        
        // Update application in data manager if approval was successful
        if (approved) {
            applicationDataManager.updateApplication(application);
        }
        
        return approved;
    }
    
    @Override
    public boolean rejectApplication(String applicationId, HDBManager manager) {
        // Get the application and validate manager authorization
        Application application = getAndValidateManagerAuthorization(applicationId, manager);
        if (application == null) {
            return false;
        }
        
        // Reject the application
        boolean rejected = application.reject();
        
        // Update application in data manager if rejection was successful
        if (rejected) {
            // Clear the current application reference from the applicant
            clearCurrentApplicationReference(application);
            applicationDataManager.updateApplication(application);
        }
        
        return rejected;
    }
    
    @Override
    public boolean approveWithdrawal(String applicationId, HDBManager manager) {
        // Get the application and validate manager authorization
        Application application = getAndValidateManagerAuthorization(applicationId, manager);
        if (application == null) {
            return false;
        }
        
        // Check if withdrawal was requested
        if (!application.isWithdrawalRequested()) {
            System.out.println("No withdrawal request for this application.");
            return false;
        }
        
        // Approve the withdrawal
        boolean approved = application.approveWithdrawal();
        
        // Update application in data manager if approval was successful
        if (approved) {
            // Clear the current application reference from the applicant
            clearCurrentApplicationReference(application);
            applicationDataManager.updateApplication(application);
        }
        
        return approved;
    }
    
    @Override
    public boolean rejectWithdrawal(String applicationId, HDBManager manager) {
        // Get the application and validate manager authorization
        Application application = getAndValidateManagerAuthorization(applicationId, manager);
        if (application == null) {
            return false;
        }
        
        // Check if withdrawal was requested
        if (!application.isWithdrawalRequested()) {
            System.out.println("No withdrawal request for this application.");
            return false;
        }
        
        // Reject the withdrawal
        boolean rejected = application.rejectWithdrawal();
        
        // Update application in data manager if rejection was successful
        if (rejected) {
            applicationDataManager.updateApplication(application);
        }
        
        return rejected;
    }
    
    /**
     * Validates input parameters for application submission.
     * 
     * @param applicant The applicant submitting the application
     * @param project The project being applied for
     * @param flatType The type of flat selected
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputForSubmission(Applicant applicant, Project project, FlatType flatType) {
        // Validate input parameters
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNull(project, "Project") ||
            !validateNotNull(flatType, "Flat Type")) {
            return false;
        }
        
        // Check if the applicant already has an active application
        if (applicant.hasActiveApplication()) {
            System.out.println("Applicant already has an active application.");
            return false;
        }
        
        // Check if project is open for applications
        if (!project.isOpenForApplications()) {
            System.out.println("Project is not currently open for applications.");
            return false;
        }
        
        // Check applicant eligibility for the flat type
        if (!eligibilityService.isEligibleForFlatType(applicant, flatType)) {
            System.out.println("Applicant is not eligible for the selected flat type.");
            return false;
        }
        
        // Check if the project offers the selected flat type
        boolean flatTypeAvailable = project.getFlatTypeInfoList().stream()
                .anyMatch(info -> info.getFlatType() == flatType);
        
        if (!flatTypeAvailable) {
            System.out.println("Selected flat type is not available in this project.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets an application and validates that it belongs to the specified applicant.
     * 
     * @param applicationId The ID of the application
     * @param applicant The applicant claiming ownership
     * @return The application if found and valid, null otherwise
     */
    private Application getAndValidateApplicationOwnership(String applicationId, Applicant applicant) {
        // Validate input
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(applicant, "Applicant")) {
            return null;
        }
        
        // Get the application
        Application application = applicationDataManager.getApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return null;
        }
        
        // Check if the application belongs to the applicant
        if (!application.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("Application does not belong to this applicant.");
            return null;
        }
        
        return application;
    }
    
    /**
     * Gets an application and validates that the specified manager is authorized to modify it.
     * 
     * @param applicationId The ID of the application
     * @param manager The manager claiming authorization
     * @return The application if found and valid, null otherwise
     */
    private Application getAndValidateManagerAuthorization(String applicationId, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Get the application
        Application application = applicationDataManager.getApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return null;
        }
        
        // Check if the manager is in charge of the project
        if (!application.getProject().getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project.");
            return null;
        }
        
        return application;
    }
    
    /**
     * Clears the current application reference from an applicant.
     * 
     * @param application The application to clear from the applicant
     */
    private void clearCurrentApplicationReference(Application application) {
        Applicant applicant = application.getApplicant();
        if (applicant.getCurrentApplication() != null && 
            applicant.getCurrentApplication().getApplicationId().equals(application.getApplicationId())) {
            applicant.setCurrentApplication(null);
            applicantDataManager.updateApplicant(applicant);
        }
    }
}