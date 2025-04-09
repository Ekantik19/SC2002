package com.bto.controller.interfaces;

import java.util.List;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.Project;
import com.bto.model.enums.ApplicationStatus;
import com.bto.model.enums.FlatType;

/**
 * Interface for Application Controller in the BTO Management System.
 * Defines methods to manage BTO applications.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IApplicationController {
    
    /**
     * Submits a new application.
     * 
     * @param applicant The applicant submitting the application
     * @param project The project being applied for
     * @param flatType The type of flat selected
     * @return The submitted application if successful, null otherwise
     */
    Application submitApplication(Applicant applicant, Project project, FlatType flatType);
    
    /**
     * Views an application by its ID.
     * 
     * @param applicationId The ID of the application to view
     * @return The requested application if found, null otherwise
     */
    Application viewApplication(String applicationId);
    
    /**
     * Requests a withdrawal of an application.
     * 
     * @param applicationId The ID of the application to withdraw
     * @param applicant The applicant requesting the withdrawal
     * @return true if the withdrawal request was successful, false otherwise
     */
    boolean requestWithdrawal(String applicationId, Applicant applicant);
    
    /**
     * Gets all applications for a specific project.
     * 
     * @param project The project to get applications for
     * @return A list of applications for the specified project
     */
    List<Application> getApplicationsByProject(Project project);
    
    /**
     * Gets all applications by a specific applicant.
     * 
     * @param applicant The applicant to get applications for
     * @return A list of applications by the specified applicant
     */
    List<Application> getApplicationsByApplicant(Applicant applicant);
    
    /**
     * Gets applications by status for a specific project.
     * 
     * @param project The project to get applications for
     * @param status The status to filter by
     * @return A list of applications with the specified status for the given project
     */
    List<Application> getApplicationsByStatus(Project project, ApplicationStatus status);
    
    /**
     * Approves an application.
     * 
     * @param applicationId The ID of the application to approve
     * @param manager The manager approving the application
     * @return true if the approval was successful, false otherwise
     */
    boolean approveApplication(String applicationId, com.bto.model.HDBManager manager);
    
    /**
     * Rejects an application.
     * 
     * @param applicationId The ID of the application to reject
     * @param manager The manager rejecting the application
     * @return true if the rejection was successful, false otherwise
     */
    boolean rejectApplication(String applicationId, com.bto.model.HDBManager manager);
    
    /**
     * Books a flat for an approved application.
     * 
     * @param applicationId The ID of the application to book a flat for
     * @param officer The officer booking the flat
     * @return true if the booking was successful, false otherwise
     */
    boolean bookFlat(String applicationId, com.bto.model.HDBOfficer officer);
    
    /**
     * Approves a withdrawal request.
     * 
     * @param applicationId The ID of the application to approve withdrawal for
     * @param manager The manager approving the withdrawal
     * @return true if the withdrawal approval was successful, false otherwise
     */
    boolean approveWithdrawal(String applicationId, com.bto.model.HDBManager manager);
    
    /**
     * Rejects a withdrawal request.
     * 
     * @param applicationId The ID of the application to reject withdrawal for
     * @param manager The manager rejecting the withdrawal
     * @return true if the withdrawal rejection was successful, false otherwise
     */
    boolean rejectWithdrawal(String applicationId, com.bto.model.HDBManager manager);
}