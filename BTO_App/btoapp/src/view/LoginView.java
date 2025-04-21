package view;

import controller.AuthenticationController;
import java.util.Scanner;
import model.User;
import view.abstracts.ARenderView;
import view.interfaces.ViewInterface;

/**
 * LoginView handles the user login interface for the BTO Management System.
 */
public class LoginView extends ARenderView implements ViewInterface{
    
    private AuthenticationController authController;
    private Scanner scanner;
    
    /**
     * Constructor for LoginView.
     * 
     * @param authController The authentication controller to use
     */
    public LoginView(AuthenticationController authController) {
        this.authController = authController;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Displays the login form and attempts to authenticate the user.
     * 
     * @return The authenticated user, or null if authentication fails
     */
    public User displayAndGetUser() {
        printHeader("BTO MANAGEMENT SYSTEM LOGIN");
        
        // Get NRIC
        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine();
        
        // Get password
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        User user = authController.authenticateAndLinkApplications(nric, password);
        
        if (user != null) {
            System.out.println(">>> Login successful! Welcome, " + user.getName());
            return user;
        } else {
            System.out.println("Login failed!");
            showError("Invalid credentials. Please try again.");
            return null;
        }
    }
    
    /**
     * Shows a message to the user.
     *
     * @param message The message to display
     */
    @Override
    public void showMessage(String message) {
        System.out.println("\n>>> " + message);
    }
    
    /**
     * Shows an error message to the user.
     *
     * @param error The error message to display
     */
    @Override
    public void showError(String error) {
        System.out.println("\n!!! ERROR: " + error);
    }
}