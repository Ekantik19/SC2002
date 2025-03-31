package com.bto;

import java.util.Scanner;

import com.bto.controller.ApplicationController;
import com.bto.controller.AuthController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.controller.ReportController;
import com.bto.model.DataManager;
import com.bto.view.LoginView;

/**
 * Main application class that coordinates the entire BTO Management System.
 */
public class App {
    private Scanner scanner;
    private DataManager dataManager;
    private AuthController authController;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private ReportController reportController;
    
    private LoginView loginView;
    
    /**
     * Constructor to initialize the application.
     */
    public App() {
        scanner = new Scanner(System.in);
        
        // Create DataManager using factory method instead of singleton
        dataManager = DataManager.createDataManager();
        
        // Initialize controllers with DataManager
        authController = new AuthController(dataManager);
        projectController = new ProjectController(dataManager, authController);
        applicationController = new ApplicationController(dataManager, authController);
        enquiryController = new EnquiryController(dataManager);
        reportController = new ReportController(dataManager);
        
        // Initialize views
        loginView = new LoginView(authController);
    }
    
    /**
     * Start the application.
     */
    public void start() {
        System.out.println("Starting BTO Management System...");
        
        // Load data
        dataManager.loadData();
        
        // Main application loop
        boolean running = true;
        while (running) {
            clearScreen();
            
            // Show login screen if not logged in
            if (authController.getCurrentUser() == null) {
                // Use renderApp(0) instead of displayLoginScreen()
                loginView.renderApp(0);
                
                // Check if user logged in successfully
                if (authController.getCurrentUser() == null) {
                    // No user logged in yet, continue showing login screen
                    continue;
                }
            }
            
            // Check if user has chosen to exit
            System.out.println("\nDo you want to exit the application? (Y/N)");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("Y")) {
                running = false;
            }
        }
        
        // Save data before exiting
        dataManager.saveData();
        System.out.println("Exiting BTO Management System. Goodbye!");
    }
    
    /**
     * Clear the console screen.
     */
    private void clearScreen() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process process = pb.inheritIO().start();
                process.waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback if clearing screen fails
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }

    }
    
    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

}