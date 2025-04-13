package model.interfaces;

import java.util.List;

import model.Applicant;
import enquiry.Enquiry;
import model.Project;

/**
 * Interface for managing enquiry operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IEnquiryManagement {
    
    /**
     * Creates a new enquiry.
     * 
     * @param applicant The applicant submitting the enquiry
     * @param project The project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return true if the enquiry was successfully created, false otherwise
     */
    boolean createEnquiry(Applicant applicant, Project project, String enquiryText);
    
    /**
     * Updates an existing enquiry.
     * 
     * @param enquiry The enquiry to update
     * @param newEnquiryText The new text for the enquiry
     * @return true if the enquiry was successfully updated, false otherwise
     */
    boolean updateEnquiry(Enquiry enquiry, String newEnquiryText);
    
    /**
     * Deletes an enquiry.
     * 
     * @param enquiry The enquiry to delete
     * @return true if the enquiry was successfully deleted, false otherwise
     */
    boolean deleteEnquiry(Enquiry enquiry);
    
    /**
     * Replies to an enquiry.
     * 
     * @param enquiry The enquiry to reply to
     * @param replyText The text of the reply
     * @param responderNric The NRIC of the person responding to the enquiry
     * @return true if the reply was successfully added, false otherwise
     */
    boolean replyToEnquiry(Enquiry enquiry, String replyText, String responderNric);
    
    /**
     * Gets a list of enquiries by applicant.
     * 
     * @param applicant The applicant who submitted the enquiries
     * @return A list of enquiries submitted by the specified applicant
     */
    List<Enquiry> getEnquiriesByApplicant(Applicant applicant);
    
    /**
     * Gets a list of enquiries by project.
     * 
     * @param project The project the enquiries are about
     * @return A list of enquiries about the specified project
     */
    List<Enquiry> getEnquiriesByProject(Project project);
    
    /**
     * Gets a list of all enquiries.
     * 
     * @return A list of all enquiries in the system
     */
    List<Enquiry> getAllEnquiries();
    
    /**
     * Gets a list of enquiries by status (answered/unanswered).
     * 
     * @param answered true to get answered enquiries, false to get unanswered enquiries
     * @return A list of enquiries filtered by answered status
     */
    List<Enquiry> getEnquiriesByStatus(boolean answered);
}