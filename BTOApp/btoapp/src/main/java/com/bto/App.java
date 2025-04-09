package com.bto;

import java.util.Scanner;

import com.bto.controller.AuthenticationController;
import com.bto.controller.LoginController;
import com.bto.datamanager.ApplicantDataManager;
import com.bto.datamanager.ManagerDataManager;
import com.bto.datamanager.OfficerDataManager;
import com.bto.view.LoginView;

/**
 * Main application class for the Build-To-Order (BTO) Management System.
 * Responsible for initializing and starting the application.
 * 
 * @author Your Name
 * @version 1.0
 */
public class App {
    private Scanner scanner;
    private AuthenticationController authController;
    private LoginController loginController;
    
    /**
     * Constructor for the App class.
     * Initializes dependencies and data managers.
     */
    public App() {
        this.scanner = new Scanner(System.in);
        
        // Initialize data managers
        ApplicantDataManager applicantDataManager = new ApplicantDataManager();
        ManagerDataManager managerDataManager = new ManagerDataManager();
        OfficerDataManager officerDataManager = new OfficerDataManager();
        
        // Load user data
        applicantDataManager.loadApplicantData();
        managerDataManager.loadManagerData();
        officerDataManager.loadOfficerData();
        
        // Initialize authentication and login controllers
        this.authController = new AuthenticationController();
        this.loginController = new LoginController(authController);
    }
    
    /**
     * Starts the BTO Management System application.
     */
    public void start() {
        printAppTitle();
        renderMainMenu();
    }
    
    /**
     * Displays the application title.
     */
    private static final void printAppTitle() {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ██████╗ ████████╗ ██████╗                               ║");
        System.out.println("║                     ██╔══██╗╚══██╔══╝██╔═══██╗                              ║");
        System.out.println("║                     ██████╔╝   ██║   ██║   ██║                              ║");
        System.out.println("║                     ██╔══██╗   ██║   ██║   ██║                              ║");
        System.out.println("║                     ██████╔╝   ██║   ╚██████╔╝                              ║");
        System.out.println("║                     ╚═════╝    ╚═╝    ╚═════╝                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Renders the main menu and handles user login.
     */
    private void renderMainMenu() {
        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    System.out.println("Thank you for using the BTO Management System. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Handles user login process.
     */
    private void login() {
        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine().trim();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        
        // Authenticate user
        if (loginController.login(nric, password)) {
            // Navigate to appropriate view based on user role
            LoginView loginView = new LoginView(loginController);
            loginView.render();
        } else {
            System.out.println("Login failed. Invalid NRIC or password.");
        }
    }
    
    /**
     * Gets user input choice with error handling.
     * 
     * @return The user's numeric choice
     */
    private int getUserChoice() {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                return choice;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                System.out.print("Enter your choice: ");
            }
        }
    }
    
    /**
     * Main method to launch the BTO Management System.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            App app = new App();
            app.start();
        } catch (Exception e) {
            System.out.println("Thank for using BTO!");
            e.printStackTrace();
        }
        
    }
}