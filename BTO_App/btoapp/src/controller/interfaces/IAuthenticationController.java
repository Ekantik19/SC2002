package controller.interfaces;

import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.User;
import model.enums.UserRole;

/**
 * Interface for Authentication Controller in the BTO Management System.
 * Defines methods for user authentication and account management.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IAuthenticationController {
    
    /**
     * Authenticates a user with the provided credentials.
     * 
     * @param nric The user's NRIC
     * @param password The user's password
     * @return The authenticated user if successful, null otherwise
     */
    User login(String nric, String password);
    
    /**
     * Changes a user's password.
     * 
     * @param nric The user's NRIC
     * @param oldPassword The user's current password
     * @param newPassword The user's new password
     * @return true if the password was successfully changed, false otherwise
     */
    boolean changePassword(String nric, String oldPassword, String newPassword);
    
    /**
     * Validates the format of an NRIC.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC format is valid, false otherwise
     */
    boolean validateNRICFormat(String nric);
    
    /**
     * Gets a user by their NRIC.
     * 
     * @param nric The NRIC of the user to retrieve
     * @return The user if found, null otherwise
     */
    User getUserByNRIC(String nric);
    
    /**
     * Gets an applicant by their NRIC.
     * 
     * @param nric The NRIC of the applicant to retrieve
     * @return The applicant if found, null otherwise
     */
    Applicant getApplicantByNRIC(String nric);
    
    /**
     * Gets an HDB officer by their NRIC.
     * 
     * @param nric The NRIC of the officer to retrieve
     * @return The officer if found, null otherwise
     */
    HDBOfficer getOfficerByNRIC(String nric);
    
    /**
     * Gets an HDB manager by their NRIC.
     * 
     * @param nric The NRIC of the manager to retrieve
     * @return The manager if found, null otherwise
     */
    HDBManager getManagerByNRIC(String nric);
    
    /**
     * Gets the role of a user by their NRIC.
     * 
     * @param nric The NRIC of the user
     * @return The user's role, or null if the user is not found
     */
    UserRole getUserRole(String nric);
    
    /**
     * Loads user data from a file.
     * 
     * @param filePath The path to the file containing user data
     * @return true if the data was successfully loaded, false otherwise
     */
    boolean loadUserData(String filePath);
    
    /**
     * Saves user data to a file.
     * 
     * @param filePath The path to save the user data to
     * @return true if the data was successfully saved, false otherwise
     */
    boolean saveUserData(String filePath);
}