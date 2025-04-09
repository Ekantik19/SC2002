package com.bto.view;

import com.bto.controller.ApplicationController;
import com.bto.controller.AuthController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.controller.ReportController;
import com.bto.model.Applicant;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * MainMenuView serves as the central navigation hub for the BTO Management System.
 * Manages user interactions, role-based access, and system navigation.
 */
public class MainMenuView extends ARenderView {
    private User currentUser;
    private AuthController authController;
    private DataManager dataManager;
    
    /**
     * Constructor for MainMenuView.
     * 
     * @param user Currently logged-in user
     * @param authController Authentication controller
     * @param dataManager Data management system
     */
    public MainMenuView(User user, AuthController authController, DataManager dataManager) {
        this.currentUser = user;
        this.authController = authController;
        this.dataManager = dataManager;
    }
    
    /**
     * Primary navigation method for the main menu.
     * 
     * @param selection Navigation selection
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        
        switch (selection) {
            case 0:
                displayMainMenu();
                break;
            case 1:
                displaySessionOptions();
                break;
            default:
                System.out.println("Invalid selection. Returning to main menu.");
                delay(1);
                renderApp(0);
        }
    }
    
    /**
     * Renders the main menu based on user role.
     */
    private void displayMainMenu() {
        printBTOHeader("BTO Management System");
        System.out.println("Welcome, " + getCurrentUserRole() + ": " + currentUser.getUserID());
        
        // Role-specific routing
        if (currentUser instanceof HDBManager) {
            navigateToManagerView();
        } else if (currentUser instanceof HDBOfficer) {
            navigateToOfficerView();
        } else if (currentUser instanceof Applicant) {
            navigateToApplicantView();
        } else {
            System.out.println("Unrecognized user type. Logging out.");
            logout();
        }
    }
    
    /**
     * Navigates to Manager-specific view.
     */
    private void navigateToManagerView() {
        // Create necessary controllers
        ProjectController projectController = new ProjectController(dataManager, authController);
        ApplicationController applicationController = new ApplicationController(dataManager, authController, projectController);
        EnquiryController enquiryController = new EnquiryController(dataManager);
        ReportController reportController = new ReportController(dataManager);
        
        // Create and render manager view
        ManagerView managerView = new ManagerView(
            (HDBManager) currentUser, 
            applicationController, 
            projectController,
            enquiryController, 
            reportController
        );
        managerView.renderApp(0);
        
        // Return to session options
        renderApp(1);
    }
    
    /**
     * Navigates to Officer-specific view.
     */
    private void navigateToOfficerView() {
        // Create necessary controllers
        ProjectController projectController = new ProjectController(dataManager, authController);
        ApplicationController applicationController = new ApplicationController(dataManager, authController, projectController);
        EnquiryController enquiryController = new EnquiryController(dataManager);
        
        // Create and render officer view
        OfficerView officerView = new OfficerView(
            (HDBOfficer) currentUser, 
            applicationController, 
            projectController,
            enquiryController
        );
        officerView.renderApp(0);
        
        // Return to session options
        renderApp(1);
    }
    
    /**
     * Navigates to Applicant-specific view.
     */
    private void navigateToApplicantView() {
        // Create necessary controllers
        ProjectController projectController = new ProjectController(dataManager, authController);
        ApplicationController applicationController = new ApplicationController(dataManager, authController, projectController);
        EnquiryController enquiryController = new EnquiryController(dataManager);
        
        // Create and render applicant view
        ApplicantView applicantView = new ApplicantView(
            (Applicant) currentUser, 
            applicationController, 
            projectController,
            enquiryController
        );
        applicantView.renderApp(0);
        
        // Return to session options
        renderApp(1);
    }
    
    /**
     * Displays session management options.
     */
    private void displaySessionOptions() {
        printSingleBorder("Session Options");
        System.out.println("1. Return to Main Menu");
        System.out.println("2. Change Password");
        System.out.println("3. Logout");
        System.out.println("4. Exit System");
        
        int choice = getInputInt("Select an option: ", 4);
        
        switch (choice) {
            case 1:
                renderApp(0);
                break;
            case 2:
                changePassword();
                break;
            case 3:
                logout();
                break;
            case 4:
                exitSystem();
                break;
        }
    }
    
    /**
     * Changes user password.
     */
    private void changePassword() {
        String oldPassword = getInputString("Enter current password: ");
        String newPassword = getInputString("Enter new password: ");
        
        boolean passwordChanged = authController.changePassword(oldPassword, newPassword);
        
        if (passwordChanged) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Password change failed. Please try again.");
        }
        
        pressEnterToContinue();
        renderApp(1);
    }
    
    /**
     * Logs out the current user.
     */
    private void logout() {
        dataManager.saveData();
        authController.logout();
        System.out.println("Logged out successfully.");
        // Typically would return to login screen, but that's outside this view's scope
    }
    
    /**
     * Exits the system.
     */
    private void exitSystem() {
        System.out.println("Saving system data...");
        dataManager.saveData();
        System.out.println("Thank you for using the BTO Management System.");
        System.exit(0);
    }
    
    /**
     * Gets a string representation of the current user's role.
     * 
     * @return User role as a string
     */
    private String getCurrentUserRole() {
        if (currentUser instanceof HDBManager) return "HDB Manager";
        if (currentUser instanceof HDBOfficer) return "HDB Officer";
        if (currentUser instanceof Applicant) return "Applicant";
        return "Unknown User";
    }
    
    /**
     * Renders the choice menu (required by abstract parent class).
     */
    @Override
    public void renderChoice() {
        displayMainMenu();
    }
}