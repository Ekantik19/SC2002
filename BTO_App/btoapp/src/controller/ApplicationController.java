package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IApplicationController;
import datamanager.ApplicantDataManager;
import datamanager.ApplicationDataManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.Applicant;
import model.Application;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;
import service.EligibilityCheckerService;

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
        System.out.println("DEBUG: Starting application submission for " + applicant.getName());

        if (applicant instanceof HDBOfficer) {
        HDBOfficer officer = (HDBOfficer) applicant;
            if (officer.isAssignedToProject(project)) {
                System.out.println("DEBUG: Officer is trying to apply for a project they're handling");
                return null;
            }
        }   
        
        // Validate input parameters
        if (!validateInputForSubmission(applicant, project, flatType)) {
            System.out.println("DEBUG: Input validation failed");
            return null;
        }
        
        System.out.println("DEBUG: Input validation passed");
        
        // Create new application with a generated ID
        System.out.println("DEBUG: Creating application object");
        String applicationId = generateApplicationId(applicant.getNric(), project.getProjectName());
        Application application = new Application(applicationId, applicant, project, flatType);
        
        System.out.println("DEBUG: Application object created with ID: " + application.getApplicationId());
        
        // Add to data manager
        System.out.println("DEBUG: Adding application to data manager");
        boolean added = applicationDataManager.addApplication(application);
        System.out.println("DEBUG: Data manager result: " + (added ? "success" : "failed"));
        
        if (added) {
            // Update applicant's current application
            System.out.println("DEBUG: Updating applicant's current application");
            applicant.setCurrentApplication(application);
            boolean applicantUpdated = applicantDataManager.updateApplicant(applicant);
            System.out.println("DEBUG: Applicant update result: " + (applicantUpdated ? "success" : "failed"));
            
            // Add application to project
            System.out.println("DEBUG: Adding application to project");
            project.addApplication(application);
            
            return application;
        }
        
        System.out.println("DEBUG: Application submission failed");
        return null;
    }
    
    /**
     * Generates an application ID based on NRIC and project name.
     * 
     * @param nric The applicant's NRIC
     * @param projectName The project name
     * @return A unique application ID
     */
    private String generateApplicationId(String nric, String projectName) {
        String nricPart = nric.substring(1, 8);
        String projectPart = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        return "APP-" + nricPart + "-" + projectPart;
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
        // Get all applications for the project
        List<Application> projectApplications = getApplicationsByProject(project);
        
        System.out.println("DEBUG: getApplicationsByStatus - Found " + projectApplications.size() + 
                        " applications for project " + project.getProjectName());
        
        // List all applications for this project with their status
        System.out.println("DEBUG: Applications in project " + project.getProjectName() + ":");
        for (Application app : projectApplications) {
            System.out.println("DEBUG: - App ID: " + app.getApplicationId() + 
                            ", Applicant: " + app.getApplicant().getName() + 
                            " (" + app.getApplicant().getNric() + ")" +
                            ", Status: " + app.getStatus());
        }
        
        // Filter by status
        List<Application> result = projectApplications.stream()
                .filter(app -> app.getStatus() == status)
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: After filtering, found " + result.size() + 
                        " applications with status " + status);
        return result;
    }

    @Override
    public boolean approveApplication(String applicationId, HDBManager manager) {
        System.out.println("DEBUG: Starting approval of application: " + applicationId);
        
        // Get the application and validate manager authorization
        Application application = getAndValidateManagerAuthorization(applicationId, manager);
        if (application == null) {
            System.out.println("DEBUG: Application validation failed for: " + applicationId);
            return false;
        }
        
        // Check if there are available units for the selected flat type
        if (!application.getProject().hasAvailableUnits(application.getSelectedFlatType())) {
            System.out.println("DEBUG: No available units for the selected flat type.");
            return false;
        }
        
        // Log status before approval for debugging
        System.out.println("DEBUG: Application status before approval: " + application.getStatus());
        
        // Approve the application
        boolean approved = application.approve();
        
        // Log status after approval for debugging
        System.out.println("DEBUG: Application status after approval: " + application.getStatus());
        
        // Update application in memory and file if approval was successful
        if (approved) {
            System.out.println("DEBUG: Updating application " + applicationId + " in data manager with status: " + application.getStatus());
            
            // Update in memory
            boolean updated = applicationDataManager.updateApplication(application);
            System.out.println("DEBUG: In-memory update result: " + (updated ? "success" : "failed"));
            
            if (updated) {
                // Use the direct file update method instead of saving all applications
                boolean fileUpdated = applicationDataManager.updateApplicationStatusInFile(applicationId, ApplicationStatus.SUCCESSFUL);
                System.out.println("DEBUG: File update result: " + (fileUpdated ? "success" : "failed"));
                return fileUpdated;
            }
        } else {
            System.out.println("DEBUG: Application approval failed");
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
            
            // Update in memory
            boolean updated = applicationDataManager.updateApplication(application);
            
            if (updated) {
                // Update just this application's status in the file
                return applicationDataManager.updateApplicationStatusInFile(applicationId, ApplicationStatus.UNSUCCESSFUL);
            }
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
        if (!validateNotNull(applicant, "Applicant")) {
            System.out.println("DEBUG: Applicant is null");
            return false;
        }
        
        if (!validateNotNull(project, "Project")) {
            System.out.println("DEBUG: Project is null");
            return false;
        }
        
        if (!validateNotNull(flatType, "Flat Type")) {
            System.out.println("DEBUG: Flat Type is null");
            return false;
        }
        
        // Check if the applicant already has an active application
        if (applicant.hasActiveApplication()) {
            System.out.println("DEBUG: Applicant already has an active application.");
            return false;
        }
        
        // Check if project is open for applications
        if (!project.isOpenForApplications()) {
            System.out.println("DEBUG: Project is not currently open for applications.");
            return false;
        }
        
        // Check applicant eligibility for the flat type
        if (!eligibilityService.isEligibleForFlatType(applicant, flatType)) {
            System.out.println("DEBUG: Applicant is not eligible for the selected flat type.");
            return false;
        }
        
        // Check if the project offers the selected flat type
        boolean flatTypeAvailable = project.getFlatTypeInfoList().stream()
                .anyMatch(info -> info.getFlatType() == flatType);
        
        if (!flatTypeAvailable) {
            System.out.println("DEBUG: Selected flat type is not available in this project.");
            return false;
        }
        
        System.out.println("DEBUG: All validation checks passed");
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