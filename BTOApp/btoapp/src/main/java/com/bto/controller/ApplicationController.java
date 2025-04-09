package com.bto.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IApplicationController;
import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.enums.ApplicationStatus;
import com.bto.model.enums.FlatType;
import com.bto.service.EligibilityCheckerService;

/**
 * Controller for managing applications in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ApplicationController extends ABaseController implements IApplicationController {
    
    private Map<String, Application> applicationMap;
    private EligibilityCheckerService eligibilityService;
    
    /**
     * Constructor for ApplicationController.
     */
    public ApplicationController() {
        this.applicationMap = new HashMap<>();
        this.eligibilityService = new EligibilityCheckerService();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Application submitApplication(Applicant applicant, Project project, FlatType flatType) {
        // Validate input
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNull(project, "Project") || 
            !validateNotNull(flatType, "Flat Type")) {
            return null;
        }
        
        // Check if project is open for applications
        if (!project.isOpenForApplications()) {
            System.out.println("Project is not open for applications");
            return null;
        }
        
        // Check if applicant already has an active application
        if (applicant.hasActiveApplication()) {
            System.out.println("Applicant already has an active application");
            return null;
        }
        
        // Check eligibility
        if (!eligibilityService.isEligibleForFlatType(applicant, flatType)) {
            System.out.println("Applicant is not eligible for the selected flat type");
            return null;
        }
        
        // Generate application ID
        String applicationId = generateApplicationId(applicant.getNric(), project.getProjectName());
        
        // Create application
        Application application = new Application(applicationId, applicant, project, flatType);
        
        // Add to maps
        applicationMap.put(applicationId, application);
        project.addApplication(application);
        applicant.setCurrentApplication(application);
        
        return application;
    }
    
    /**
     * Helper method to generate an application ID.
     * 
     * @param nric The NRIC of the applicant
     * @param projectName The name of the project
     * @return A unique application ID
     */
    private String generateApplicationId(String nric, String projectName) {
        // Simple ID generation - in a real system, this would be more sophisticated
        return "APP-" + nric.substring(1, 8) + "-" + 
               projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Application viewApplication(String applicationId) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID")) {
            return null;
        }
        
        return applicationMap.get(applicationId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestWithdrawal(String applicationId, Applicant applicant) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        // Check if this is the applicant's application
        if (!application.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("Unauthorized to request withdrawal for this application");
            return false;
        }
        
        return application.requestWithdrawal();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Application> getApplicationsByProject(Project project) {
        if (!validateNotNull(project, "Project")) {
            return new ArrayList<>();
        }
        
        return project.getApplications();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Application> getApplicationsByApplicant(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        List<Application> applications = new ArrayList<>();
        if (applicant.getCurrentApplication() != null) {
            applications.add(applicant.getCurrentApplication());
        }
        
        return applications;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Application> getApplicationsByStatus(Project project, ApplicationStatus status) {
        if (!validateNotNull(project, "Project") || !validateNotNull(status, "Status")) {
            return new ArrayList<>();
        }
        
        List<Application> filteredApplications = new ArrayList<>();
        for (Application application : project.getApplications()) {
            if (application.getStatus() == status) {
                filteredApplications.add(application);
            }
        }
        
        return filteredApplications;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean approveApplication(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        return manager.approveApplication(application);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rejectApplication(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        return manager.rejectApplication(application);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bookFlat(String applicationId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        return officer.bookFlat(application);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean approveWithdrawal(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        return manager.approveWithdrawal(application);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rejectWithdrawal(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        Application application = applicationMap.get(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }
        
        return manager.rejectWithdrawal(application);
    }
}