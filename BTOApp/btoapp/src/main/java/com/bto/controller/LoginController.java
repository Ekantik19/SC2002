package com.bto.controller;

import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.User;
import com.bto.model.enums.UserRole;

/**
 * Controller for handling user login operations in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class LoginController {
    
    private AuthenticationController authController;
    private User currentUser;
    
    /**
     * Constructor for LoginController.
     * 
     * @param authController The authentication controller to use
     */
    public LoginController(AuthenticationController authController) {
        this.authController = authController;
        this.currentUser = null;
    }
    
    /**
     * Attempts to log in a user with the provided credentials.
     * 
     * @param nric The user's NRIC
     * @param password The user's password
     * @return true if login was successful, false otherwise
     */
    public boolean login(String nric, String password) {
        User user = authController.login(nric, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        authController.logout();
        this.currentUser = null;
    }
    
    /**
     * Changes the password for a user.
     * 
     * @param nric The user's NRIC
     * @param oldPassword The user's current password
     * @param newPassword The user's new password
     * @return true if the password was successfully changed, false otherwise
     */
    public boolean changePassword(String nric, String oldPassword, String newPassword) {
        return authController.changePassword(nric, oldPassword, newPassword);
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if the current user is an applicant.
     * 
     * @return true if the current user is an applicant, false otherwise
     */
    public boolean isApplicant() {
        return currentUser != null && currentUser.getRole() == UserRole.APPLICANT;
    }
    
    /**
     * Checks if the current user is an officer.
     * 
     * @return true if the current user is an officer, false otherwise
     */
    public boolean isOfficer() {
        return currentUser != null && 
               (currentUser.getRole() == UserRole.OFFICER || 
                currentUser.getRole() == UserRole.MANAGER);
    }
    
    /**
     * Checks if the current user is a manager.
     * 
     * @return true if the current user is a manager, false otherwise
     */
    public boolean isManager() {
        return currentUser != null && currentUser.getRole() == UserRole.MANAGER;
    }
    
    /**
     * Gets the current user as an applicant.
     * 
     * @return The current user as an applicant, or null if the current user is not an applicant
     */
    public Applicant getCurrentApplicant() {
        if (isApplicant()) {
            return (Applicant) currentUser;
        }
        return null;
    }
    
    /**
     * Gets the current user as an officer.
     * 
     * @return The current user as an officer, or null if the current user is not an officer
     */
    public HDBOfficer getCurrentOfficer() {
        if (isOfficer()) {
            return (HDBOfficer) currentUser;
        }
        return null;
    }
    
    /**
     * Gets the current user as a manager.
     * 
     * @return The current user as a manager, or null if the current user is not a manager
     */
    public HDBManager getCurrentManager() {
        if (isManager()) {
            return (HDBManager) currentUser;
        }
        return null;
    }
    
    /**
     * Validates the format of an NRIC.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC format is valid, false otherwise
     */
    public boolean validateNRICFormat(String nric) {
        return authController.validateNRICFormat(nric);
    }
}