package com.bto.controller;

import java.util.ArrayList;
import java.util.List;

import com.bto.controller.abstracts.ABaseController;
import com.bto.enquiry.Enquiry;
import com.bto.model.Application;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;

/**
 * Controller for HDB Officer operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class OfficerController extends ABaseController {
    
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private ReportController reportController;
    
    /**
     * Constructor for OfficerController.
     * 
     * @param projectController The project controller to use
     * @param applicationController The application controller to use
     * @param enquiryController The enquiry controller to use
     * @param reportController The report controller to use
     */
    public OfficerController(
            ProjectController projectController,
            ApplicationController applicationController,
            EnquiryController enquiryController,
            ReportController reportController) {
        this.projectController = projectController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.reportController = reportController;
    }
    
    /**
     * Registers an officer for a project.
     * 
     * @param projectId The ID of the project to register for
     * @param officer The officer to register
     * @return true if registration was successful, false otherwise
     */
    public boolean registerForProject(String projectId, HDBOfficer officer) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        return projectController.registerOfficerForProject(projectId, officer);
    }
    
    /**
     * Gets the project the officer is assigned to.
     * 
     * @param officer The officer to get the assigned project for
     * @return The assigned project, or null if the officer is not assigned to any project
     */
    public Project getAssignedProject(HDBOfficer officer) {
        if (!validateNotNull(officer, "Officer")) {
            return null;
        }
        
        return officer.getAssignedProject();
    }
    
    /**
     * Checks if the officer is assigned to a specific project.
     * 
     * @param projectId The ID of the project to check
     * @param officer The officer to check
     * @return true if the officer is assigned to the project, false otherwise
     */
    public boolean isAssignedToProject(String projectId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        return officer.isAssignedToProject(project);
    }
    
    /**
     * Books a flat for an approved application.
     * 
     * @param applicationId The ID of the application to book a flat for
     * @param officer The officer booking the flat
     * @return true if the booking was successful, false otherwise
     */
    public boolean bookFlat(String applicationId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        return applicationController.bookFlat(applicationId, officer);
    }
    
    /**
     * Generates a receipt for a flat booking.
     * 
     * @param applicationId The ID of the application to generate a receipt for
     * @param officer The officer generating the receipt
     * @return The generated receipt if successful, null otherwise
     */
    public Receipt generateBookingReceipt(String applicationId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return null;
        }
        
        return reportController.generateBookingReceipt(applicationId, officer);
    }
    
    /**
     * Gets applications for the officer's assigned project.
     * 
     * @param officer The officer to get applications for
     * @return A list of applications for the officer's assigned project
     */
    public List<Application> getProjectApplications(HDBOfficer officer) {
        if (!validateNotNull(officer, "Officer")) {
            return new ArrayList<>();
        }
        
        Project assignedProject = officer.getAssignedProject();
        if (assignedProject == null) {
            return new ArrayList<>();
        }
        
        return applicationController.getApplicationsByProject(assignedProject);
    }
    
    /**
     * Gets enquiries for the officer's assigned project.
     * 
     * @param officer The officer to get enquiries for
     * @return A list of enquiries for the officer's assigned project
     */
    public List<Enquiry> getProjectEnquiries(HDBOfficer officer) {
        if (!validateNotNull(officer, "Officer")) {
            return new ArrayList<>();
        }
        
        Project assignedProject = officer.getAssignedProject();
        if (assignedProject == null) {
            return new ArrayList<>();
        }
        
        return enquiryController.getEnquiriesForOfficer(assignedProject.getProjectName(), officer);
    }
    
    /**
     * Gets unanswered enquiries for the officer's assigned project.
     * 
     * @param officer The officer to get unanswered enquiries for
     * @return A list of unanswered enquiries for the officer's assigned project
     */
    public List<Enquiry> getUnansweredEnquiries(HDBOfficer officer) {
        if (!validateNotNull(officer, "Officer")) {
            return new ArrayList<>();
        }
        
        Project assignedProject = officer.getAssignedProject();
        if (assignedProject == null) {
            return new ArrayList<>();
        }
        
        return enquiryController.getUnansweredEnquiries(assignedProject.getProjectName());
    }
    
    /**
     * Replies to an enquiry as an officer.
     * 
     * @param enquiryId The ID of the enquiry to reply to
     * @param replyText The text of the reply
     * @param officer The officer replying to the enquiry
     * @return true if the reply was successful, false otherwise
     */
    public boolean replyToEnquiry(String enquiryId, String replyText, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(replyText, "Reply Text") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        return enquiryController.replyToEnquiryAsOfficer(enquiryId, replyText, officer);
    }
    
    /**
     * Gets an application by its ID.
     * 
     * @param applicationId The ID of the application to retrieve
     * @return The requested application if found, null otherwise
     */
    public Application getApplicationById(String applicationId) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID")) {
            return null;
        }
        
        return applicationController.viewApplication(applicationId);
    }
    
    /**
     * Gets an application by applicant's NRIC.
     * 
     * @param applicantNric The NRIC of the applicant
     * @param officer The officer retrieving the application
     * @return The application if found, null otherwise
     */
    public Application getApplicationByApplicantNric(String applicantNric, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(applicantNric, "Applicant NRIC") || 
            !validateNotNull(officer, "Officer")) {
            return null;
        }
        
        return officer.retrieveApplicationByNric(applicantNric);
    }
}