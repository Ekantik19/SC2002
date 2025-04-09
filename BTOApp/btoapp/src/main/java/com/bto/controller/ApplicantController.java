package com.bto.controller;

import java.util.ArrayList;
import java.util.List;

import com.bto.controller.abstracts.ABaseController;
import com.bto.enquiry.Enquiry;
import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.Project;
import com.bto.model.enums.FlatType;
import com.bto.service.EligibilityCheckerService;

/**
 * Controller for applicant operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ApplicantController extends ABaseController {
    
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private EligibilityCheckerService eligibilityService;
    
    /**
     * Constructor for ApplicantController.
     * 
     * @param projectController The project controller to use
     * @param applicationController The application controller to use
     * @param enquiryController The enquiry controller to use
     */
    public ApplicantController(
            ProjectController projectController,
            ApplicationController applicationController,
            EnquiryController enquiryController) {
        this.projectController = projectController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.eligibilityService = new EligibilityCheckerService();
    }
    
    /**
     * Gets projects visible to an applicant.
     * 
     * @param applicant The applicant to get visible projects for
     * @return A list of projects visible to the applicant
     */
    public List<Project> getVisibleProjects(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        return projectController.getVisibleProjectsForApplicant(applicant);
    }
    
    /**
     * Submits a BTO application.
     * 
     * @param applicant The applicant submitting the application
     * @param projectId The ID of the project to apply for
     * @param flatType The type of flat to apply for
     * @return The submitted application if successful, null otherwise
     */
    public Application submitApplication(Applicant applicant, String projectId, FlatType flatType) {
        // Validate input
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(flatType, "Flat Type")) {
            return null;
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return null;
        }
        
        // Check if project is visible to the applicant
        if (!project.isVisible()) {
            System.out.println("Project is not visible");
            return null;
        }
        
        // Check if applicant is eligible for the flat type
        if (!eligibilityService.isEligibleForFlatType(applicant, flatType)) {
            System.out.println("Applicant is not eligible for the selected flat type");
            return null;
        }
        
        return applicationController.submitApplication(applicant, project, flatType);
    }
    
    /**
     * Requests a withdrawal of an application.
     * 
     * @param applicationId The ID of the application to withdraw
     * @param applicant The applicant requesting the withdrawal
     * @return true if the withdrawal request was successful, false otherwise
     */
    public boolean requestWithdrawal(String applicationId, Applicant applicant) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        return applicationController.requestWithdrawal(applicationId, applicant);
    }
    
    /**
     * Creates a new enquiry.
     * 
     * @param applicant The applicant submitting the enquiry
     * @param projectId The ID of the project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return The created enquiry if successful, null otherwise
     */
    public Enquiry createEnquiry(Applicant applicant, String projectId, String enquiryText) {
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(enquiryText, "Enquiry Text")) {
            return null;
        }
        
        return enquiryController.createEnquiry(applicant, projectId, enquiryText);
    }
    
    /**
     * Updates an existing enquiry.
     * 
     * @param enquiryId The ID of the enquiry to update
     * @param newEnquiryText The new text for the enquiry
     * @param applicant The applicant updating the enquiry
     * @return true if the update was successful, false otherwise
     */
    public boolean updateEnquiry(String enquiryId, String newEnquiryText, Applicant applicant) {
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(newEnquiryText, "New Enquiry Text") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        return enquiryController.updateEnquiry(enquiryId, newEnquiryText, applicant);
    }
    
    /**
     * Deletes an enquiry.
     * 
     * @param enquiryId The ID of the enquiry to delete
     * @param applicant The applicant deleting the enquiry
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(String enquiryId, Applicant applicant) {
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        return enquiryController.deleteEnquiry(enquiryId, applicant);
    }
    
    /**
     * Gets all enquiries by an applicant.
     * 
     * @param applicant The applicant who submitted the enquiries
     * @return A list of enquiries submitted by the applicant
     */
    public List<Enquiry> getApplicantEnquiries(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        return enquiryController.getEnquiriesByApplicant(applicant);
    }
    
    /**
     * Gets the current application of an applicant.
     * 
     * @param applicant The applicant to get the current application for
     * @return The current application if it exists, null otherwise
     */
    public Application getCurrentApplication(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return null;
        }
        
        return applicant.getCurrentApplication();
    }
    
    /**
     * Checks if an applicant has an active application.
     * 
     * @param applicant The applicant to check
     * @return true if the applicant has an active application, false otherwise
     */
    public boolean hasActiveApplication(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        return applicant.hasActiveApplication();
    }
    
    /**
     * Checks if an applicant is eligible for a specific flat type.
     * 
     * @param applicant The applicant to check
     * @param flatType The flat type to check eligibility for
     * @return true if the applicant is eligible, false otherwise
     */
    public boolean isEligibleForFlatType(Applicant applicant, FlatType flatType) {
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNull(flatType, "Flat Type")) {
            return false;
        }
        
        return eligibilityService.isEligibleForFlatType(applicant, flatType);
    }
    
    /**
     * Checks if an applicant is eligible for BTO.
     * 
     * @param applicant The applicant to check
     * @return true if the applicant is eligible, false otherwise
     */
    public boolean isEligibleForBTO(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        return eligibilityService.isEligibleForBTO(applicant);
    }
    
    /**
     * Gets details of a project.
     * 
     * @param projectId The ID of the project to get details for
     * @return The project if found, null otherwise
     */
    public Project getProjectDetails(String projectId) {
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return null;
        }
        
        return projectController.getProjectById(projectId);
    }
}