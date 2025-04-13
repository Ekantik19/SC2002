package view;

import java.util.Scanner;

import controller.AuthenticationController;
import model.User;
import view.abstracts.ARenderView;
import view.interfaces.IBTOView;

/**
 * LoginView handles the user login interface for the BTO Management System.
 * It provides a command-line interface for users to authenticate.
 * 
 * @author Your Name
 * @version 1.0
 */
public class LoginView extends ARenderView implements IBTOView {
    
    private AuthenticationController authController;
    private User loggedInUser;
    private Scanner scanner;
    
    /**
     * Constructor for LoginView.
     * 
     * @param authController The authentication controller for login operations
     */
    public LoginView(AuthenticationController authController) {
        this.authController = authController;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Displays the login interface and handles user authentication.
     * Returns the authenticated user if successful.
     * 
     * @return The authenticated user, or null if authentication failed
     */
    // public User displayAndGetUser() {
    //     boolean loginSuccessful = false;
        
    //     while (!loginSuccessful) {
    //         printHeader("BTO MANAGEMENT SYSTEM LOGIN");
            
    //         System.out.print("Enter NRIC: ");
    //         String nric = scanner.nextLine();
            
    //         System.out.print("Enter Password: ");
    //         String password = scanner.nextLine();
            
    //         // Try to login
    //         loggedInUser = authController.login(nric, password);
            
    //         if (loggedInUser != null) {
    //             showMessage("Login successful! Welcome, " + loggedInUser.getName());
    //             loginSuccessful = true;
    //         } else {
    //             showError("Invalid credentials. Please try again.");
                
    //             System.out.print("Would you like to try again? (Y/N): ");
    //             String tryAgain = scanner.nextLine();
    //             if (!tryAgain.equalsIgnoreCase("Y")) {
    //                 return null;
    //             }
    //         }
    //     }
        
    //     return loggedInUser;
    // }

    public User displayAndGetUser() {
        boolean loginSuccessful = false;
        
        while (!loginSuccessful) {
            printHeader("BTO MANAGEMENT SYSTEM LOGIN");
            
            System.out.print("Enter NRIC: ");
            String nric = scanner.nextLine();
            System.out.println("DEBUG: User entered NRIC: '" + nric + "'");
            
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            System.out.println("DEBUG: User entered password: '" + password + "'");
            
            // Try to login
            System.out.println("DEBUG: Attempting login with authController...");
            loggedInUser = authController.login(nric, password);
            
            if (loggedInUser != null) {
                System.out.println("DEBUG: Login successful! User: " + loggedInUser.getName());
                showMessage("Login successful! Welcome, " + loggedInUser.getName());
                loginSuccessful = true;
            } else {
                System.out.println("DEBUG: Login failed - returned null user");
                showError("Invalid credentials. Please try again.");
                
                System.out.print("Would you like to try again? (Y/N): ");
                String tryAgain = scanner.nextLine();
                System.out.println("DEBUG: User response to try again: " + tryAgain);
                if (!tryAgain.equalsIgnoreCase("Y")) {
                    return null;
                }
            }
        }
        
        return loggedInUser;
    }
    
    @Override
    public void display() {
        displayAndGetUser();
    }
    
    @Override
    public void refreshData() {
        // No data to refresh for login view
    }
    
    @Override
    public boolean handleNavigation(int option) {
        // Login view doesn't have navigation options
        return false;
    }
    
    @Override
    public void showMessage(String message) {
        System.out.println("\n>>> " + message);
    }
    
    @Override
    public void showError(String error) {
        System.out.println("\n!!! ERROR: " + error);
    }
}