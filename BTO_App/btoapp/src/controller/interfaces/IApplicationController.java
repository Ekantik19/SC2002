package controller.interfaces;

import java.util.List;
import model.Applicant;
import model.Application;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

/**
 * Interface for Application Controller in the BTO Management System.
 * Defines methods to manage BTO application submissions, status changes, 
 * withdrawals, and retrieval of application information.
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
    boolean approveApplication(String applicationId, model.HDBManager manager);
    
    /**
     * Rejects an application.
     * 
     * @param applicationId The ID of the application to reject
     * @param manager The manager rejecting the application
     * @return true if the rejection was successful, false otherwise
     */
    boolean rejectApplication(String applicationId, model.HDBManager manager);
    
    
    /**
     * Approves a withdrawal request.
     * 
     * @param applicationId The ID of the application to approve withdrawal for
     * @param manager The manager approving the withdrawal
     * @return true if the withdrawal approval was successful, false otherwise
     */
    boolean approveWithdrawal(String applicationId, model.HDBManager manager);
    
    /**
     * Rejects a withdrawal request.
     * 
     * @param applicationId The ID of the application to reject withdrawal for
     * @param manager The manager rejecting the withdrawal
     * @return true if the withdrawal rejection was successful, false otherwise
     */
    boolean rejectWithdrawal(String applicationId, model.HDBManager manager);
}