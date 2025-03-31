package com.bto.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.bto.controller.ApplicationController;
import com.bto.controller.ProjectController;
import com.bto.model.Applicant;
import com.bto.model.Project;

/**
 * View for the applicant interface.
 */
public class ApplicantView {
    private Scanner scanner;
    private Applicant applicant;
    private ApplicationController applicationController;
    private ProjectController projectController;
    
    /**
     * Constructor for ApplicantView.
     * 
     * @param applicant The applicant user
     * @param applicationController The application controller
     * @param projectController The project controller
     */
    public ApplicantView(Applicant applicant, ApplicationController applicationController, ProjectController projectController) {
        this.scanner = new Scanner(System.in);
        this.applicant = applicant;
        this.applicationController = applicationController;
        this.projectController = projectController;
    }
    
    /**
     * Display the applicant interface.
     */
    public void display() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n==================================");
            System.out.println("    Applicant Menu    ");
            System.out.println("==================================");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for a Project");
            System.out.println("3. View Application Status");
            System.out.println("4. Request Withdrawal");
            System.out.println("5. Create Enquiry");
            System.out.println("6. View My Enquiries");
            System.out.println("7. Change Password");
            System.out.println("8. Back to Main Menu");
            
            System.out.print("\nEnter choice: ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    viewAvailableProjects();
                    break;
                case 2:
                    applyForProject();
                    break;
                case 3:
                    viewApplicationStatus();
                    break;
                case 4:
                    requestWithdrawal();
                    break;
                case 5:
                    createEnquiry();
                    break;
                case 6:
                    viewEnquiries();
                    break;
                case 7:
                    changePassword();
                    break;
                case 8:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    /**
     * Display available projects for the applicant.
     */
    private void viewAvailableProjects() {
        System.out.println("\n==================================");
        System.out.println("    Available Projects    ");
        System.out.println("==================================");
        
        // Get filters if any
        Map<String, Object> filters = new HashMap<>();
        
        System.out.println("Do you want to apply filters? (Y/N)");
        String applyFilters = scanner.nextLine();
        
        if (applyFilters.equalsIgnoreCase("Y")) {
            System.out.println("Enter neighborhood (or leave blank for all):");
            String neighborhood = scanner.nextLine();
            if (!neighborhood.isEmpty()) {
                filters.put("neighborhood", neighborhood);
            }
            
            System.out.println("Enter flat type (2-Room/3-Room, or leave blank for all):");
            String flatType = scanner.nextLine();
            if (!flatType.isEmpty()) {
                filters.put("flatType", flatType);
            }
        }
        
        List<Project> projects = projectController.getProjects(filters);
        
        if (projects == null || projects.isEmpty()) {
            System.out.println("No projects available matching your criteria.");
            return;
        }
        
        System.out.println("\nID\tProject Name\tNeighborhood\tFlat Types\tStatus");
        System.out.println("------------------------------------------------------------------");
        
        for (Project project : projects) {
            StringBuilder flatTypes = new StringBuilder();
            for (String type : project.getFlatTypes().keySet()) {
                flatTypes.append(type).append(", ");
            }
            if (flatTypes.length() > 2) {
                flatTypes.setLength(flatTypes.length() - 2); // Remove trailing comma and space
            }
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood(), 
                             flatTypes.toString(), 
                             project.isOpen() ? "Open" : "Closed");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Apply for a project.
     */
    private void applyForProject() {
        System.out.println("\n==================================");
        System.out.println("    Apply for a Project    ");
        System.out.println("==================================");
        
        // Check if already has an application
        String status = applicationController.viewApplicationStatus();
        if (!status.equals("No Application")) {
            System.out.println("You already have an application. Current status: " + status);
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // View available projects
        viewAvailableProjects();
        
        System.out.println("Enter the ID of the project you want to apply for:");
        int projectID = getIntInput();
        
        boolean success = applicationController.applyForProject(projectID);
        
        if (success) {
            System.out.println("Application submitted successfully!");
        } else {
            System.out.println("Failed to submit application. Please check your eligibility and the project availability.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View the status of the current application.
     */
    private void viewApplicationStatus() {
        System.out.println("\n==================================");
        System.out.println("    Application Status    ");
        System.out.println("==================================");
        
        String status = applicationController.viewApplicationStatus();
        System.out.println("Status: " + status);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Request withdrawal from the current application.
     */
    private void requestWithdrawal() {
        System.out.println("\n==================================");
        System.out.println("    Request Withdrawal    ");
        System.out.println("==================================");
        
        // Check if has an application
        String status = applicationController.viewApplicationStatus();
        if (status.equals("No Application")) {
            System.out.println("You don't have an active application to withdraw from.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Are you sure you want to withdraw your application? (Y/N)");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = applicationController.requestWithdrawal();
            
            if (success) {
                System.out.println("Withdrawal request submitted successfully!");
            } else {
                System.out.println("Failed to submit withdrawal request.");
            }
        } else {
            System.out.println("Withdrawal request cancelled.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Create an enquiry for a project.
     */
    private void createEnquiry() {
        // Implementation for creating an enquiry
        System.out.println("Create Enquiry functionality to be implemented.");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View enquiries made by the applicant.
     */
    private void viewEnquiries() {
        // Implementation for viewing enquiries
        System.out.println("View Enquiries functionality to be implemented.");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Change the applicant's password.
     */
    private void changePassword() {
        // Implementation for changing password
        System.out.println("Change Password functionality to be implemented.");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Get integer input from the user.
     * 
     * @return The integer input
     */
    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        }
    }
}