package com.bto.controller;

import com.bto.model.Applicant;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.User;
import com.bto.view.MainMenuView;

/**
 * Controller for handling authentication and user management.
 */
public class AuthController {
    private DataManager dataManager;
    private User currentUser;
    
    // /**
    //  * Constructor for AuthController.
    //  */
    // public AuthController() {
    //     this.dataManager = DataManager.getInstance();
    // }
    
    /**
     * Constructor for AuthController.
     * 
     * @param dataManager The data manager to be used
     */
    public AuthController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Attempt to login a user.
     * 
     * @param userID The user ID to authenticate
     * @param password The password to verify
     * @return User object if login successful, null otherwise
     */
    public User login(String userID, String password) {
        User user = dataManager.authenticate(userID, password);
        if (user != null) {
            currentUser = user;
            return user;
        }
        return null;
    }
    
    /**
     * Log out the current user.
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Change the current user's password.
     * 
     * @param oldPassword The old password for verification
     * @param newPassword The new password to set
     * @return true if password change is successful, false otherwise
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null || !currentUser.authenticate(oldPassword)) {
            return false;
        }
        
        // Use the updated changePassword method with both parameters
        boolean success = currentUser.changePassword(oldPassword, newPassword);
        if (success) {
            dataManager.saveData();
        }
        return success;
    }

    /**
     * Change a specific user's password.
     * 
     * @param user The user whose password to change
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password change is successful, false otherwise
     */
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (user == null || !user.authenticate(currentPassword)) {
            return false;
        }
        
        // Use the updated changePassword method with both parameters
        boolean success = user.changePassword(currentPassword, newPassword);
        if (success) {
            dataManager.saveData();
        }
        return success;
    }
    
    /**
     * Get the current logged-in user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Navigate to the main menu for the specified user.
     * 
     * @param user The user to navigate to the main menu for
     */
    public void navigateToMainMenu(User user) {
        if (user != null) {
            // Set current user if not already set
            if (currentUser == null) {
                currentUser = user;
            }
            
            // Create and display main menu
            MainMenuView mainMenuView = new MainMenuView(user, this, dataManager);
            mainMenuView.renderApp(0);
        }
    }
    
    /**
     * Check if the current user is an applicant.
     * 
     * @return true if current user is an applicant, false otherwise
     */
    public boolean isApplicant() {
        return currentUser instanceof Applicant;
    }
    
    /**
     * Check if the current user is an HDB officer.
     * 
     * @return true if current user is an HDB officer, false otherwise
     */
    public boolean isOfficer() {
        return currentUser instanceof HDBOfficer;
    }
    
    /**
     * Check if the current user is an HDB manager.
     * 
     * @return true if current user is an HDB manager, false otherwise
     */
    public boolean isManager() {
        return currentUser instanceof HDBManager;
    }
}