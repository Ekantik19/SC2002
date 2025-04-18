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
        System.out.println("DEBUG: User entered NRIC: '" + nric + "'");
        
        // Get password
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.println("DEBUG: User entered password: '" + password + "'");
        
        System.out.println("DEBUG: Attempting login with authController...");
        
        // Use the new method instead of the original authenticate method
        User user = authController.authenticateAndLinkApplications(nric, password);
        
        if (user != null) {
            System.out.println("DEBUG: Login successful! User: " + user.getName());
            System.out.println(">>> Login successful! Welcome, " + user.getName());
            return user;
        } else {
            System.out.println("DEBUG: Login failed!");
            showError("Invalid credentials. Please try again.");
            return null;
        }
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