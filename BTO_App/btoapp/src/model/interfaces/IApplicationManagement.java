package model.interfaces;

import java.util.List;

import model.Applicant;
import model.Application;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

/**
 * Interface for managing application operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IApplicationManagement {
    
    /**
     * Submits a new BTO application.
     * 
     * @param applicant The applicant submitting the application
     * @param project The project being applied for
     * @param flatType The type of flat being applied for
     * @return true if the application was successfully submitted, false otherwise
     */
    boolean submitApplication(Applicant applicant, Project project, FlatType flatType);
    
    /**
     * Approves a BTO application.
     * 
     * @param application The application to approve
     * @return true if the application was successfully approved, false otherwise
     */
    boolean approveApplication(Application application);
    
    /**
     * Rejects a BTO application.
     * 
     * @param application The application to reject
     * @return true if the application was successfully rejected, false otherwise
     */
    boolean rejectApplication(Application application);
    
    /**
     * Books a flat for an approved application.
     * 
     * @param application The application to book a flat for
     * @return true if the flat was successfully booked, false otherwise
     */
    boolean bookFlat(Application application);
    
    /**
     * Requests a withdrawal of an application.
     * 
     * @param application The application to withdraw
     * @return true if the withdrawal was successfully requested, false otherwise
     */
    boolean requestWithdrawal(Application application);
    
    /**
     * Approves a withdrawal request.
     * 
     * @param application The application to approve withdrawal for
     * @return true if the withdrawal was successfully approved, false otherwise
     */
    boolean approveWithdrawal(Application application);
    
    /**
     * Rejects a withdrawal request.
     * 
     * @param application The application to reject withdrawal for
     * @return true if the withdrawal was successfully rejected, false otherwise
     */
    boolean rejectWithdrawal(Application application);
    
    /**
     * Gets a list of applications by applicant.
     * 
     * @param applicant The applicant to get applications for
     * @return A list of applications submitted by the specified applicant
     */
    List<Application> getApplicationsByApplicant(Applicant applicant);
    
    /**
     * Gets a list of applications by project.
     * 
     * @param project The project to get applications for
     * @return A list of applications for the specified project
     */
    List<Application> getApplicationsByProject(Project project);
    
    /**
     * Gets a list of applications by status.
     * 
     * @param project The project to get applications for
     * @param status The application status to filter by
     * @return A list of applications with the specified status for the given project
     */
    List<Application> getApplicationsByStatus(Project project, ApplicationStatus status);
    
    /**
     * Gets an application by ID.
     * 
     * @param applicationId The ID of the application to retrieve
     * @return The application with the specified ID, or null if not found
     */
    Application getApplicationById(String applicationId);
    
    /**
     * Checks if an applicant has any active applications.
     * 
     * @param applicant The applicant to check
     * @return true if the applicant has active applications, false otherwise
     */
    boolean hasActiveApplication(Applicant applicant);
}