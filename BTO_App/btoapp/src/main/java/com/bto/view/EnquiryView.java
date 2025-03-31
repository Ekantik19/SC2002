package com.bto.view;

import java.util.List;
import java.util.Scanner;

import com.bto.controller.EnquiryController;
import com.bto.model.Enquiry;
import com.bto.model.Project;
import com.bto.model.User;

/**
 * View class for managing enquiries in the BTO Management System.
 * Allows users to create, view, edit, and delete enquiries.
 */
public class EnquiryView {
    private Scanner scanner;
    private EnquiryController enquiryController;
    private User currentUser;

    /**
     * Constructor for EnquiryView.
     *
     * @param scanner Scanner for user input
     * @param enquiryController Controller for managing enquiries
     * @param currentUser The currently logged in user
     */
    public EnquiryView(Scanner scanner, EnquiryController enquiryController, User currentUser) {
        this.scanner = scanner;
        this.enquiryController = enquiryController;
        this.currentUser = currentUser;
    }

    /**
     * Display the main enquiry management menu.
     */
    public void displayEnquiryMenu() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== Enquiry Management ===");
            System.out.println("1. Create New Enquiry");
            System.out.println("2. View My Enquiries");
            System.out.println("3. Edit Enquiry");
            System.out.println("4. Delete Enquiry");
            System.out.println("5. Back to Main Menu");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    createEnquiry();
                    break;
                case "2":
                    viewUserEnquiries();
                    break;
                case "3":
                    editEnquiry();
                    break;
                case "4":
                    deleteEnquiry();
                    break;
                case "5":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    /**
     * Create a new enquiry about a project.
     */
    private void createEnquiry() {
        // Get available projects
        List<Project> availableProjects = enquiryController.getAvailableProjects(currentUser);
        
        if (availableProjects.isEmpty()) {
            System.out.println("No projects available for enquiry.");
            return;
        }
        
        // Display available projects
        System.out.println("\n=== Available Projects ===");
        for (int i = 0; i < availableProjects.size(); i++) {
            Project project = availableProjects.get(i);
            System.out.println((i + 1) + ". " + project.getProjectName() + " - " + project.getNeighborhood());
        }
        
        // Get project selection
        System.out.print("Select a project (enter number): ");
        int projectIndex;
        try {
            projectIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (projectIndex < 0 || projectIndex >= availableProjects.size()) {
                System.out.println("Invalid project selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }
        
        Project selectedProject = availableProjects.get(projectIndex);
        
        // Get enquiry text
        System.out.println("\nEnter your enquiry about " + selectedProject.getProjectName() + ":");
        String enquiryText = scanner.nextLine();
        
        if (enquiryText.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty.");
            return;
        }
        
        // Create the enquiry
        boolean success = enquiryController.createEnquiry(currentUser, selectedProject, enquiryText);
        
        if (success) {
            System.out.println("Enquiry submitted successfully.");
        } else {
            System.out.println("Failed to submit enquiry. Please try again.");
        }
    }
    
    /**
     * View all enquiries created by the current user.
     */
    private void viewUserEnquiries() {
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries.");
            return;
        }
        
        System.out.println("\n=== Your Enquiries ===");
        for (int i = 0; i < userEnquiries.size(); i++) {
            Enquiry enquiry = userEnquiries.get(i);
            System.out.println("ID: " + enquiry.getEnquiryId());
            System.out.println("Project: " + enquiry.getProject().getProjectName());
            System.out.println("Date: " + enquiry.getCreationDate());
            System.out.println("Enquiry: " + enquiry.getEnquiryText());
            
            if (enquiry.getResponse() != null && !enquiry.getResponse().isEmpty()) {
                System.out.println("Response: " + enquiry.getResponse());
            } else {
                System.out.println("Response: No response yet");
            }
            System.out.println("------------------------------");
        }
    }
    
    /**
     * Edit an existing enquiry.
     */
    private void editEnquiry() {
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries to edit.");
            return;
        }
        
        // Display enquiries with IDs
        System.out.println("\n=== Your Enquiries ===");
        for (Enquiry enquiry : userEnquiries) {
            System.out.println("ID: " + enquiry.getEnquiryId() + " - Project: " + 
                              enquiry.getProject().getProjectName() + " - " + 
                              enquiry.getEnquiryText().substring(0, Math.min(30, enquiry.getEnquiryText().length())) + 
                              (enquiry.getEnquiryText().length() > 30 ? "..." : ""));
        }
        
        // Get enquiry ID to edit
        System.out.print("\nEnter the ID of the enquiry you want to edit: ");
        String enquiryId = scanner.nextLine().trim();
        if (enquiryId.isEmpty()) {
            System.out.println("Please enter a valid ID.");
            return;
        }
        
        // Find the enquiry
        Enquiry enquiryToEdit = null;
        for (Enquiry enquiry : userEnquiries) {
            if (enquiry.getEnquiryId().equals(enquiryId)) {
                enquiryToEdit = enquiry;
                break;
            }
        }
        
        if (enquiryToEdit == null) {
            System.out.println("Enquiry not found. Please check the ID and try again.");
            return;
        }
        
        // Get new text
        System.out.println("Current enquiry: " + enquiryToEdit.getEnquiryText());
        System.out.println("Enter new enquiry text:");
        String newText = scanner.nextLine();
        
        if (newText.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty.");
            return;
        }
        
        // Update the enquiry
        boolean success = enquiryController.updateEnquiry(enquiryToEdit, newText);
        
        if (success) {
            System.out.println("Enquiry updated successfully.");
        } else {
            System.out.println("Failed to update enquiry. Please try again.");
        }
    }
    
    /**
     * Delete an existing enquiry.
     */
    private void deleteEnquiry() {
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries to delete.");
            return;
        }
        
        // Display enquiries with IDs
        System.out.println("\n=== Your Enquiries ===");
        for (Enquiry enquiry : userEnquiries) {
            System.out.println("ID: " + enquiry.getEnquiryId() + " - Project: " + 
                              enquiry.getProject().getProjectName() + " - " + 
                              enquiry.getEnquiryText().substring(0, Math.min(30, enquiry.getEnquiryText().length())) + 
                              (enquiry.getEnquiryText().length() > 30 ? "..." : ""));
        }
        
        // Get enquiry ID to delete
        System.out.print("\nEnter the ID of the enquiry you want to delete: ");
        String enquiryId = scanner.nextLine().trim();
        if (enquiryId.isEmpty()) {
            System.out.println("Please enter a valid ID.");
            return;
        }
        
        // Find the enquiry
        Enquiry enquiryToDelete = null;
        for (Enquiry enquiry : userEnquiries) {
            if (enquiry.getEnquiryId().equals(enquiryId)) {
                enquiryToDelete = enquiry;
                break;
            }
        }
        
        if (enquiryToDelete == null) {
            System.out.println("Enquiry not found. Please check the ID and try again.");
            return;
        }
        
        // Confirm deletion
        System.out.print("Are you sure you want to delete this enquiry? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!confirmation.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        
        // Delete the enquiry
        boolean success = enquiryController.deleteEnquiry(enquiryToDelete);
        
        if (success) {
            System.out.println("Enquiry deleted successfully.");
        } else {
            System.out.println("Failed to delete enquiry. Please try again.");
        }
    }
    
    /**
     * Display the interface for viewing and responding to enquiries (for HDB Officers and Managers).
     */
    public void displayEnquiryManagementForStaff() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== Enquiry Management for Staff ===");
            System.out.println("1. View Project Enquiries");
            System.out.println("2. Respond to Enquiry");
            System.out.println("3. Back to Main Menu");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    viewProjectEnquiries();
                    break;
                case "2":
                    respondToEnquiry();
                    break;
                case "3":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    /**
     * View enquiries for projects the staff member is handling.
     */
    private void viewProjectEnquiries() {
        List<Project> staffProjects = enquiryController.getStaffProjects(currentUser);
        
        if (staffProjects.isEmpty()) {
            System.out.println("You are not handling any projects.");
            return;
        }
        
        // Display projects
        System.out.println("\n=== Your Projects ===");
        for (int i = 0; i < staffProjects.size(); i++) {
            Project project = staffProjects.get(i);
            System.out.println((i + 1) + ". " + project.getProjectName() + " - " + project.getNeighborhood());
        }
        
        // Get project selection
        System.out.print("Select a project to view enquiries (enter number): ");
        int projectIndex;
        try {
            projectIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (projectIndex < 0 || projectIndex >= staffProjects.size()) {
                System.out.println("Invalid project selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }
        
        Project selectedProject = staffProjects.get(projectIndex);
        
        // Get and display enquiries for the selected project
        List<Enquiry> projectEnquiries = enquiryController.getProjectEnquiries(selectedProject);
        
        if (projectEnquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            return;
        }
        
        System.out.println("\n=== Enquiries for " + selectedProject.getProjectName() + " ===");
        for (Enquiry enquiry : projectEnquiries) {
            System.out.println("ID: " + enquiry.getEnquiryId());
            System.out.println("From: " + enquiry.getUser().getName() + " (" + enquiry.getUser().getUserID() + ")");
            System.out.println("Date: " + enquiry.getCreationDate());
            System.out.println("Enquiry: " + enquiry.getEnquiryText());
            
            if (enquiry.getResponse() != null && !enquiry.getResponse().isEmpty()) {
                System.out.println("Response: " + enquiry.getResponse());
            } else {
                System.out.println("Response: No response yet");
            }
            System.out.println("------------------------------");
        }
    }
    
    /**
     * Respond to an enquiry.
     */
    private void respondToEnquiry() {
        List<Project> staffProjects = enquiryController.getStaffProjects(currentUser);
        List<Enquiry> pendingEnquiries = enquiryController.getPendingEnquiries(staffProjects);
        
        if (pendingEnquiries.isEmpty()) {
            System.out.println("No pending enquiries to respond to.");
            return;
        }
        
        // Display pending enquiries
        System.out.println("\n=== Pending Enquiries ===");
        for (Enquiry enquiry : pendingEnquiries) {
            System.out.println("ID: " + enquiry.getEnquiryId() + 
                              " - Project: " + enquiry.getProject().getProjectName() +
                              " - From: " + enquiry.getUser().getName() +
                              " - " + enquiry.getEnquiryText().substring(0, Math.min(30, enquiry.getEnquiryText().length())) + 
                              (enquiry.getEnquiryText().length() > 30 ? "..." : ""));
        }
        
        // Get enquiry ID to respond to
        System.out.print("\nEnter the ID of the enquiry you want to respond to: ");
        String enquiryId = scanner.nextLine().trim();
        if (enquiryId.isEmpty()) {
            System.out.println("Please enter a valid ID.");
            return;
        }
        
        // Find the enquiry
        Enquiry enquiryToRespond = null;
        for (Enquiry enquiry : pendingEnquiries) {
            if (enquiry.getEnquiryId().equals(enquiryId)) {
                enquiryToRespond = enquiry;
                break;
            }
        }
        
        if (enquiryToRespond == null) {
            System.out.println("Enquiry not found. Please check the ID and try again.");
            return;
        }
        
        // Display the enquiry details
        System.out.println("\n=== Enquiry Details ===");
        System.out.println("From: " + enquiryToRespond.getUser().getName());
        System.out.println("Project: " + enquiryToRespond.getProject().getProjectName());
        System.out.println("Enquiry: " + enquiryToRespond.getEnquiryText());
        
        // Get response
        System.out.println("\nEnter your response:");
        String response = scanner.nextLine();
        
        if (response.trim().isEmpty()) {
            System.out.println("Response cannot be empty.");
            return;
        }
        
        // Add the response
        boolean success = enquiryController.respondToEnquiry(enquiryToRespond, response);
        
        if (success) {
            System.out.println("Response submitted successfully.");
        } else {
            System.out.println("Failed to submit response. Please try again.");
        }
    }
}