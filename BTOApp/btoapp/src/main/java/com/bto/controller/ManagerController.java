package com.bto.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bto.controller.abstracts.ABaseController;
import com.bto.enquiry.Enquiry;
import com.bto.model.HDBManager;
import com.bto.model.Project;
import com.bto.model.Report;
import com.bto.model.enums.FlatType;

/**
 * Controller for HDB Manager operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ManagerController extends ABaseController {
    
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private ReportController reportController;
    
    /**
     * Constructor for ManagerController.
     * 
     * @param projectController The project controller to use
     * @param applicationController The application controller to use
     * @param enquiryController The enquiry controller to use
     * @param reportController The report controller to use
     */
    public ManagerController(
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
     * Creates a new BTO project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatTypes The types of flats available in the project
     * @param numberOfUnits The number of units for each flat type
     * @param sellingPrices The selling prices for each flat type
     * @param openingDate The application opening date
     * @param closingDate The application closing date
     * @param manager The manager creating the project
     * @param officerSlots The number of officer slots for the project
     * @return The created project if successful, null otherwise
     */
    public Project createProject(String projectName, String neighborhood, 
                                List<FlatType> flatTypes, List<Integer> numberOfUnits, 
                                List<Double> sellingPrices, Date openingDate, 
                                Date closingDate, HDBManager manager, int officerSlots) {
        
        // Validate input
        if (!validateNotNullOrEmpty(projectName, "Project Name") || 
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") || 
            !validateNotNull(flatTypes, "Flat Types") || 
            !validateNotNull(numberOfUnits, "Number of Units") || 
            !validateNotNull(sellingPrices, "Selling Prices") || 
            !validateNotNull(openingDate, "Opening Date") || 
            !validateNotNull(closingDate, "Closing Date") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        return projectController.createProject(projectName, neighborhood, flatTypes, 
                                             numberOfUnits, sellingPrices, 
                                             openingDate, closingDate, 
                                             manager, officerSlots);
    }
    
    /**
     * Updates an existing project.
     * 
     * @param projectId The ID of the project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots
     * @param manager The manager updating the project
     * @return true if the update was successful, false otherwise
     */
    public boolean updateProject(String projectId, String projectName, 
                               String neighborhood, Date openingDate, 
                               Date closingDate, int officerSlots, 
                               HDBManager manager) {
        
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(projectName, "Project Name") || 
            !validateNotNullOrEmpty(neighborhood, "Neighborhood") || 
            !validateNotNull(openingDate, "Opening Date") || 
            !validateNotNull(closingDate, "Closing Date") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return projectController.updateProject(projectId, projectName, neighborhood, 
                                             openingDate, closingDate, 
                                             officerSlots, manager);
    }
    
    /**
     * Deletes a project.
     * 
     * @param projectId The ID of the project to delete
     * @param manager The manager deleting the project
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteProject(String projectId, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return projectController.deleteProject(projectId, manager);
    }
    
    /**
     * Toggles the visibility of a project.
     * 
     * @param projectId The ID of the project to toggle visibility for
     * @param visible The new visibility status
     * @param manager The manager toggling the visibility
     * @return true if the visibility was successfully toggled, false otherwise
     */
    public boolean toggleProjectVisibility(String projectId, boolean visible, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return projectController.toggleProjectVisibility(projectId, visible, manager);
    }
    
    /**
     * Approves an officer's registration.
     * 
     * @param officerNric The NRIC of the officer to approve
     * @param manager The manager approving the registration
     * @return true if the approval was successful, false otherwise
     */
    public boolean approveOfficerRegistration(String officerNric, HDBManager manager) {
        if (!validateNotNullOrEmpty(officerNric, "Officer NRIC") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return projectController.approveOfficerRegistration(officerNric, manager);
    }
    
    /**
     * Rejects an officer's registration.
     * 
     * @param officerNric The NRIC of the officer to reject
     * @param manager The manager rejecting the registration
     * @return true if the rejection was successful, false otherwise
     */
    public boolean rejectOfficerRegistration(String officerNric, HDBManager manager) {
        if (!validateNotNullOrEmpty(officerNric, "Officer NRIC") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return projectController.rejectOfficerRegistration(officerNric, manager);
    }
    
    /**
     * Approves a BTO application.
     * 
     * @param applicationId The ID of the application to approve
     * @param manager The manager approving the application
     * @return true if the approval was successful, false otherwise
     */
    public boolean approveApplication(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return applicationController.approveApplication(applicationId, manager);
    }
    
    /**
     * Rejects a BTO application.
     * 
     * @param applicationId The ID of the application to reject
     * @param manager The manager rejecting the application
     * @return true if the rejection was successful, false otherwise
     */
    public boolean rejectApplication(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return applicationController.rejectApplication(applicationId, manager);
    }
    
    /**
     * Approves a withdrawal request.
     * 
     * @param applicationId The ID of the application to approve withdrawal for
     * @param manager The manager approving the withdrawal
     * @return true if the withdrawal approval was successful, false otherwise
     */
    public boolean approveWithdrawal(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return applicationController.approveWithdrawal(applicationId, manager);
    }
    
    /**
     * Rejects a withdrawal request.
     * 
     * @param applicationId The ID of the application to reject withdrawal for
     * @param manager The manager rejecting the withdrawal
     * @return true if the withdrawal rejection was successful, false otherwise
     */
    public boolean rejectWithdrawal(String applicationId, HDBManager manager) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return applicationController.rejectWithdrawal(applicationId, manager);
    }
    
    /**
     * Gets projects created by the manager.
     * 
     * @param manager The manager to get projects for
     * @return A list of projects created by the manager
     */
    public List<Project> getManagerProjects(HDBManager manager) {
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        return projectController.getProjectsByManager(manager);
    }
    
    /**
     * Gets all enquiries across all projects managed by the manager.
     * 
     * @param manager The manager to get enquiries for
     * @return A list of all enquiries for projects managed by the manager
     */
    public List<Enquiry> getAllEnquiries(HDBManager manager) {
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        return enquiryController.getAllEnquiriesForManager(manager);
    }
    
    /**
     * Replies to an enquiry as a manager.
     * 
     * @param enquiryId The ID of the enquiry to reply to
     * @param replyText The text of the reply
     * @param manager The manager replying to the enquiry
     * @return true if the reply was successful, false otherwise
     */
    public boolean replyToEnquiry(String enquiryId, String replyText, HDBManager manager) {
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(replyText, "Reply Text") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return enquiryController.replyToEnquiryAsManager(enquiryId, replyText, manager);
    }
    
    /**
     * Generates a project booking report.
     * 
     * @param projectId The ID of the project to generate a report for
     * @param reportTitle The title of the report
     * @param manager The manager generating the report
     * @return The generated report if successful, null otherwise
     */
    public Report generateProjectBookingReport(String projectId, String reportTitle, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(reportTitle, "Report Title") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        return reportController.generateProjectBookingReport(projectId, reportTitle, manager);
    }
    
    /**
     * Generates a marital status report.
     * 
     * @param projectId The ID of the project to generate a report for
     * @param maritalStatus The marital status to filter by
     * @param manager The manager generating the report
     * @return The generated report if successful, null otherwise
     */
    public Report generateMaritalStatusReport(String projectId, String maritalStatus, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(maritalStatus, "Marital Status") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        return reportController.generateMaritalStatusReport(projectId, maritalStatus, manager);
    }
    
    /**
     * Generates a flat type report.
     * 
     * @param projectId The ID of the project to generate a report for
     * @param flatType The flat type to filter by
     * @param manager The manager generating the report
     * @return The generated report if successful, null otherwise
     */
    public Report generateFlatTypeReport(String projectId, FlatType flatType, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(flatType, "Flat Type") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        return reportController.generateFlatTypeReport(projectId, flatType, manager);
    }
    
    /**
     * Generates an age range report.
     * 
     * @param projectId The ID of the project to generate a report for
     * @param minAge The minimum age to include
     * @param maxAge The maximum age to include
     * @param manager The manager generating the report
     * @return The generated report if successful, null otherwise
     */
    public Report generateAgeRangeReport(String projectId, int minAge, int maxAge, HDBManager manager) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Validate age range
        if (minAge < 0 || maxAge < minAge) {
            System.out.println("Invalid age range: " + minAge + " - " + maxAge);
            return null;
        }
        
        return reportController.generateAgeRangeReport(projectId, minAge, maxAge, manager);
    }
    
    /**
     * Exports a report to a file.
     * 
     * @param reportId The ID of the report to export
     * @param filePath The path to export the report to
     * @param manager The manager exporting the report
     * @return true if the export was successful, false otherwise
     */
    public boolean exportReport(String reportId, String filePath, HDBManager manager) {
        if (!validateNotNullOrEmpty(reportId, "Report ID") || 
            !validateNotNullOrEmpty(filePath, "File Path") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        return reportController.exportReport(reportId, filePath, manager);
    }
}