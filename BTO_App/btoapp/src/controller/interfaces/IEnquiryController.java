package controller.interfaces;

import java.util.List;

import enquiry.Enquiry;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;

/**
 * Interface for Enquiry Controller in the BTO Management System.
 * Defines methods to manage enquiries about BTO projects.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IEnquiryController {
    
    /**
     * Creates a new enquiry.
     * 
     * @param applicant The applicant submitting the enquiry
     * @param projectId The ID of the project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return The created enquiry if successful, null otherwise
     */
    Enquiry createEnquiry(Applicant applicant, String projectId, String enquiryText);
    
    /**
     * Updates an existing enquiry.
     * 
     * @param enquiryId The ID of the enquiry to update
     * @param newEnquiryText The new text for the enquiry
     * @param applicant The applicant updating the enquiry
     * @return true if the update was successful, false otherwise
     */
    boolean updateEnquiry(String enquiryId, String newEnquiryText, Applicant applicant);
    
    /**
     * Deletes an enquiry.
     * 
     * @param enquiryId The ID of the enquiry to delete
     * @param applicant The applicant deleting the enquiry
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteEnquiry(String enquiryId, Applicant applicant);
    
    /**
     * Replies to an enquiry as an officer.
     * 
     * @param enquiryId The ID of the enquiry to reply to
     * @param replyText The text of the reply
     * @param officer The officer replying to the enquiry
     * @return true if the reply was successful, false otherwise
     */
    boolean replyToEnquiryAsOfficer(String enquiryId, String replyText, HDBOfficer officer);
    
    /**
     * Replies to an enquiry as a manager.
     * 
     * @param enquiryId The ID of the enquiry to reply to
     * @param replyText The text of the reply
     * @param manager The manager replying to the enquiry
     * @return true if the reply was successful, false otherwise
     */
    boolean replyToEnquiryAsManager(String enquiryId, String replyText, HDBManager manager);
    
    /**
     * Gets an enquiry by its ID.
     * 
     * @param enquiryId The ID of the enquiry to retrieve
     * @return The requested enquiry if found, null otherwise
     */
    Enquiry getEnquiryById(String enquiryId);
    
    /**
     * Gets all enquiries by a specific applicant.
     * 
     * @param applicant The applicant who submitted the enquiries
     * @return A list of enquiries submitted by the specified applicant
     */
    List<Enquiry> getEnquiriesByApplicant(Applicant applicant);
    
    /**
     * Gets all enquiries for a specific project.
     * 
     * @param projectId The ID of the project the enquiries are about
     * @return A list of enquiries about the specified project
     */
    List<Enquiry> getEnquiriesByProject(String projectId);
    
    /**
     * Gets all enquiries for a specific project accessible to an officer.
     * 
     * @param projectId The ID of the project the enquiries are about
     * @param officer The officer requesting the enquiries
     * @return A list of enquiries about the specified project
     */
    List<Enquiry> getEnquiriesForOfficer(String projectId, HDBOfficer officer);
    
    /**
     * Gets all enquiries across all projects accessible to a manager.
     * 
     * @param manager The manager requesting the enquiries
     * @return A list of all enquiries for projects managed by the specified manager
     */
    List<Enquiry> getAllEnquiriesForManager(HDBManager manager);
    
    /**
     * Gets a list of answered enquiries for a project.
     * 
     * @param projectId The ID of the project the enquiries are about
     * @return A list of answered enquiries for the specified project
     */
    List<Enquiry> getAnsweredEnquiries(String projectId);
    
    /**
     * Gets a list of unanswered enquiries for a project.
     * 
     * @param projectId The ID of the project the enquiries are about
     * @return A list of unanswered enquiries for the specified project
     */
    List<Enquiry> getUnansweredEnquiries(String projectId);
}