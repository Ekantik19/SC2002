package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IApplicantController;
import datamanager.ApplicantDataManager;
import datamanager.ApplicationDataManager;
import model.Applicant;
import model.Application;
import model.Project;
import model.enums.FlatType;

/**
 * Controller for managing applicant-specific operations in the BTO Management System.
 */
public class ApplicantController extends ABaseController implements IApplicantController {
    
    private ApplicantDataManager applicantDataManager;
    private ApplicationDataManager applicationDataManager;
    
    /**
     * Constructor for ApplicantController.
     * 
     * @param applicantDataManager Manager for applicant data operations
     * @param applicationDataManager Manager for application data operations
     */
    public ApplicantController(ApplicantDataManager applicantDataManager, 
                               ApplicationDataManager applicationDataManager) {
        this.applicantDataManager = applicantDataManager;
        this.applicationDataManager = applicationDataManager;
    }
    
    @Override
    public boolean checkApplicantEligibility(Applicant applicant, Project project, FlatType flatType) {
        // Validate input
        if (applicant == null || project == null || flatType == null) {
            return false;
        }
        
        // Check if applicant has an active application
        if (applicant.hasActiveApplication()) {
            System.out.println("Applicant already has an active application.");
            return false;
        }
        
        // Check project application period
        if (!project.isOpenForApplications()) {
            System.out.println("Project is not currently open for applications.");
            return false;
        }
        
        // Age and marital status eligibility
        if (!applicant.isEligibleForBTO()) {
            System.out.println("Applicant does not meet age eligibility requirements.");
            return false;
        }
        
        // Flat type eligibility
        if (!isEligibleForFlatType(applicant, flatType)) {
            System.out.println("Applicant is not eligible for the selected flat type.");
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isEligibleForFlatType(Applicant applicant, FlatType flatType) {
        // Validate input
        if (applicant == null || flatType == null) {
            return false;
        }
        
        // Singles 35+ can only apply for 2-Room
        if (!applicant.isMarried() && applicant.getAge() >= 35) {
            return flatType == FlatType.TWO_ROOM;
        }
        
        // Married applicants 21+ can apply for any flat type
        if (applicant.isMarried() && applicant.getAge() >= 21) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Application submitApplication(Applicant applicant, Project project, FlatType flatType) {
        // Check eligibility first
        if (!checkApplicantEligibility(applicant, project, flatType)) {
            return null;
        }
        
        // Create application
        Application application = new Application(null, applicant, project, flatType);
        
        // Add application to data manager
        if (applicationDataManager.addApplication(application)) {
            // Set current application for applicant
            applicant.setCurrentApplication(application);
            
            // Update applicant in data manager
            applicantDataManager.updateApplicant(applicant);
            
            return application;
        }
        
        return null;
    }
    
    @Override
    public boolean requestApplicationWithdrawal(Applicant applicant) {
        // Validate input
        if (applicant == null) {
            return false;
        }
        
        // Check if applicant has an active application
        Application currentApplication = applicant.getCurrentApplication();
        if (currentApplication == null) {
            System.out.println("No active application to withdraw.");
            return false;
        }
        
        // Request withdrawal
        boolean withdrawalRequested = applicant.requestWithdrawal();
        
        if (withdrawalRequested) {
            // Update application in data manager
            applicationDataManager.updateApplication(currentApplication);
            return true;
        }
        
        return false;
    }
}