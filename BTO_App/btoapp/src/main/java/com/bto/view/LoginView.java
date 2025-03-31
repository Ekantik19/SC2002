/**
 * The LoginView class handles the login interface in the BTO Management System.
 * This class extends ARenderView.
 * 
 * @version 1.0
 */
package com.bto.view;

import com.bto.controller.AuthController;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

public class LoginView extends ARenderView {
    private AuthController authController;

    /**
     * Constructs a new LoginView with a reference to an AuthController.
     *
     * @param authController The AuthController to associate with this view
     */
    public LoginView(AuthController authController) {
        super();
        this.authController = authController;
    }

    /**
     * Renders the login interface based on selection.
     * 
     * @param selection The interface section to render
     */
    @Override
    public void renderApp(int selection) {
        switch (selection) {
            case 0:
                // Main login menu
                renderChoice();
                displayLoginOptions();
                break;
                
            case 1:
                // Login process
                renderChoice();
                loginProcess();
                break;
                
            case 2:
                // Password change process
                renderChoice();
                System.out.println("Password Reset");
                changePasswordProcess();
                break;
                
            default:
                System.out.println("Invalid selection. Returning to main login menu.");
                delay(2);
                renderApp(0);
                break;
        }
    }

    /**
     * Displays the login options menu
     */
    private void displayLoginOptions() {
        System.out.println("Choose an option:");
        System.out.println("1. Login");
        System.out.println("2. Reset Password");
        System.out.println("3. Exit System");
        
        int choice = getInputInt("Enter choice: ", 3);
        
        switch (choice) {
            case 1:
                renderApp(1);
                break;
            case 2:
                renderApp(2);
                break;
            case 3:
                System.out.println("Exiting system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                delay(1);
                renderApp(0);
                break;
        }
    }

    /**
     * Handles the login process
     */
    private void loginProcess() {
        String userID = getInputString("Enter NRIC: ");
        String password = getInputString("Enter Password: ");
        
        User user = authController.login(userID, password);
        
        if (user != null) {
            System.out.println("Login successful!");
            delay(1, "Loading BTO Management System...");
            authController.navigateToMainMenu(user);
        } else {
            System.out.println("Invalid NRIC or password.");
            delay(2);
            renderApp(0);
        }
    }

    /**
     * Handles the password change process
     */
    private void changePasswordProcess() {
        String userID = getInputString("Enter NRIC: ");
        String currentPassword = getInputString("Enter current password: ");
        
        // Verify current credentials
        User user = authController.login(userID, currentPassword);
        
        if (user == null) {
            System.out.println("Invalid NRIC or password.");
            delay(2);
            renderApp(0);
            return;
        }
        
        String newPassword = getInputString("Enter new password: ");
        String confirmPassword = getInputString("Confirm new password: ");
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            delay(2);
            renderApp(2);
            return;
        }
        
        boolean success = authController.changePassword(user, currentPassword, newPassword);
        
        if (success) {
            System.out.println("Password updated successfully!");
            delay(2);
            renderApp(0);
        } else {
            System.out.println("Failed to update password. Please try again.");
            delay(2);
            renderApp(2);
        }
    }

    /**
     * Renders the title for the login view
     */
    @Override
    public void renderChoice() {
        printBTOHeader("Login System");
    }
}