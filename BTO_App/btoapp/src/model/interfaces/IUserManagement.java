package model.interfaces;

/**
 * Interface for managing user operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IUserManagement {
    
    /**
     * Authenticates a user with their credentials.
     * 
     * @param nric The user's NRIC (National Registration Identity Card)
     * @param password The user's password
     * @return true if authentication is successful, false otherwise
     */
    boolean authenticate(String nric, String password);
    
    /**
     * Changes the user's password.
     * 
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if the password was successfully changed, false otherwise
     */
    boolean changePassword(String oldPassword, String newPassword);
    
    /**
     * Validates the NRIC format.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC is valid, false otherwise
     */
    boolean validateNRIC(String nric);
    
}