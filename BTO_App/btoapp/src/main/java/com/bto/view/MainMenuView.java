/**
 * The MainMenuView class handles the main menu interface in the BTO Management System.
 * This class extends ARenderView.
 * 
 * @version 1.0
 */
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

public class MainMenuView extends ARenderView {
    private User currentUser;
    private AuthController authController;
    private DataManager dataManager;
    
    /**
     * Constructs a new MainMenuView with the current user, auth controller, and data manager.
     * 
     * @param user The current logged-in user
     * @param authController The auth controller for handling logout
     * @param dataManager The data manager for data persistence
     */
    public MainMenuView(User user, AuthController authController, DataManager dataManager) {
        super();
        this.currentUser = user;
        this.authController = authController;
        this.dataManager = dataManager;
    }
    
    /**
     * Renders the main menu interface and directs to role-specific views.
     * 
     * @param selection The interface section to render
     */
    @Override
    public void renderApp(int selection) {
        switch (selection) {
            case 0:
                // Main menu
                renderChoice();
                routeToRoleSpecificView();
                break;
                
            case 1:
                // Session options after returning from role views
                renderChoice();
                displaySessionOptions();
                break;
                
            default:
                renderChoice();
                System.out.println("Invalid selection. Returning to main menu.");
                delay(1);
                renderApp(0);
                break;
        }
    }
    
    /**
     * Routes to the appropriate view based on user role
     */
    private void routeToRoleSpecificView() {
        // Create controllers with the dataManager
        ApplicationController applicationController = new ApplicationController(dataManager, authController);
        ProjectController projectController = new ProjectController(dataManager, authController);
        EnquiryController enquiryController = new EnquiryController(dataManager);
        ReportController reportController = new ReportController(dataManager);

        if (currentUser instanceof HDBManager) {
            System.out.println("\nWelcome, HDB Manager " + currentUser.getName());
            delay(1, "Loading Manager Interface...");
            ManagerView managerView = new ManagerView(
                (HDBManager) currentUser,
                applicationController,
                projectController,
                enquiryController,
                reportController
            );
            managerView.display();
            renderApp(1); // Show session options after return
        } else if (currentUser instanceof HDBOfficer) {
            System.out.println("\nWelcome, HDB Officer " + currentUser.getName());
            delay(1, "Loading Officer Interface...");
            OfficerView officerView = new OfficerView(
                (HDBOfficer) currentUser,
                applicationController,
                projectController,
                enquiryController
            );
            officerView.display();
            renderApp(1); // Show session options after return
        } else if (currentUser instanceof Applicant) {
            System.out.println("\nWelcome, Applicant " + currentUser.getName());
            delay(1, "Loading Applicant Interface...");
            ApplicantView applicantView = new ApplicantView(
                (Applicant) currentUser,
                applicationController,
                projectController
            );
            applicantView.display();
            renderApp(1); // Show session options after return
        } else {
            System.out.println("Unknown user type. Returning to login...");
            delay(2);
            logout();
        }
    }
    
    /**
     * Displays options after returning from role-specific views
     */
    private void displaySessionOptions() {
        printSingleBorder("Session Options");
        System.out.println("1. Return to Role Menu");
        System.out.println("2. Logout");
        System.out.println("3. Exit System");
        
        int choice = getInputInt("Enter choice: ", 3);
        
        switch (choice) {
            case 1:
                renderApp(0); // Return to role-specific view
                break;
            case 2:
                System.out.println("Logging out...");
                delay(1);
                logout();
                break;
            case 3:
                System.out.println("Exiting system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Returning to session options...");
                delay(1);
                renderApp(1);
                break;
        }
    }
    
    /**
     * Logs out the current user and returns to the login screen
     */
    private void logout() {
        authController.logout();
    }
    
    /**
     * Renders the title for the main menu view
     */
    @Override
    public void renderChoice() {
        String userType = getUserTypeString();
        String userName = currentUser.getName() != null ? currentUser.getName() : currentUser.getUserID();
        printBTOHeader("Main Menu - " + userType + ": " + userName);
    }
    
    /**
     * Gets a string representation of the user's type
     * 
     * @return A string describing the user's role
     */
    private String getUserTypeString() {
        if (currentUser instanceof HDBManager) {
            return "HDB Manager";
        } else if (currentUser instanceof HDBOfficer) {
            return "HDB Officer";
        } else if (currentUser instanceof Applicant) {
            return "Applicant";
        } else {
            return "User";
        }
    }
}