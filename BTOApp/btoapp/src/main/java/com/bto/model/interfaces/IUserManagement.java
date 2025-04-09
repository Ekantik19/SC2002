package com.bto.model.interfaces;

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
    
    /**
     * Updates the user's profile.
     * 
     * @param name The user's name
     * @param age The user's age
     * @param maritalStatus The user's marital status
     * @return true if the profile was successfully updated, false otherwise
     */
    boolean updateProfile(String name, int age, String maritalStatus);
    
    /**
     * Logs out the current user.
     * 
     * @return true if logout was successful, false otherwise
     */
    boolean logout();
}