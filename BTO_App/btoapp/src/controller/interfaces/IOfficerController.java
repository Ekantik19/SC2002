package controller.interfaces;

import java.util.List;

import model.Application;
import model.HDBOfficer;
import model.Project;
import model.Receipt;

/**
 * Interface for Officer Controller in the BTO Management System.
 * Defines methods to manage HDB Officer registrations, flat booking,
 * and receipt generation.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IOfficerController {
    
    /**
     * Registers an officer for a project.
     * 
     * @param officer The officer to register
     * @param project The project to register for
     * @return true if registration was successful, false otherwise
     */
    boolean registerForProject(HDBOfficer officer, Project project);
    
    /**
     * Gets the registration status of an officer for a project.
     * 
     * @param officer The officer to check
     * @return true if registration is approved, false if pending or not registered
     */
    boolean getRegistrationStatus(HDBOfficer officer);
    
    /**
     * Books a flat for a successful application.
     * 
     * @param application The application to book a flat for
     * @param officer The officer booking the flat
     * @return true if booking was successful, false otherwise
     */
    boolean bookFlat(Application application, HDBOfficer officer);
    
    /**
     * Generates a receipt for a flat booking.
     * 
     * @param application The application to generate a receipt for
     * @param officer The officer generating the receipt
     * @return The generated receipt, or null if not authorized
     */
    Receipt generateBookingReceipt(Application application, HDBOfficer officer);
    
    /**
     * Retrieves an application by the applicant's NRIC.
     * 
     * @param applicantNric The NRIC of the applicant
     * @param officer The officer retrieving the application
     * @return The application if found, null otherwise
     */
    Application retrieveApplicationByNric(String applicantNric, HDBOfficer officer);
    
    /**
     * Gets a list of projects an officer is handling.
     * 
     * @param officer The officer to check
     * @return A list of projects the officer is handling (usually just one)
     */
    List<Project> getAssignedProjects(HDBOfficer officer);
}