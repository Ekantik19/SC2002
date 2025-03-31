package com.bto.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.bto.controller.ApplicationController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.model.Application;
import com.bto.model.Enquiry;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;

/**
 * View for the HDB Officer interface.
 */
public class OfficerView {
    private Scanner scanner;
    private HDBOfficer officer;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    
    /**
     * Constructor for OfficerView with dependency injection.
     * 
     * @param officer The HDB Officer user
     * @param applicationController The application controller
     * @param projectController The project controller
     * @param enquiryController The enquiry controller
     */
    public OfficerView(HDBOfficer officer,
                    ApplicationController applicationController,
                    ProjectController projectController,
                    EnquiryController enquiryController) {
        this.scanner = new Scanner(System.in);
        this.officer = officer;
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.enquiryController = enquiryController;
    }
    
    /**
     * Display the HDB Officer interface.
     */
    public void display() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n==================================");
            System.out.println("    HDB Officer Menu    ");
            System.out.println("==================================");
            System.out.println("1. Register to Handle a Project");
            System.out.println("2. Check Registration Status");
            System.out.println("3. View Projects");
            System.out.println("4. View Project Details");
            System.out.println("5. Process Successful Applications");
            System.out.println("6. View and Reply to Enquiries");
            System.out.println("7. Generate Booking Receipt");
            System.out.println("8. Apply for a Project (as Applicant)");
            System.out.println("9. Create Enquiry");
            System.out.println("10. View My Enquiries");
            System.out.println("11. Change Password");
            System.out.println("12. Back to Main Menu");
            
            System.out.print("\nEnter choice: ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    registerForProject();
                    break;
                case 2:
                    checkRegistrationStatus();
                    break;
                case 3:
                    viewProjects();
                    break;
                case 4:
                    viewProjectDetails();
                    break;
                case 5:
                    processApplications();
                    break;
                case 6:
                    viewAndReplyToEnquiries();
                    break;
                case 7:
                    generateBookingReceipt();
                    break;
                case 8:
                    applyForProject();
                    break;
                case 9:
                    createEnquiry();
                    break;
                case 10:
                    viewEnquiries();
                    break;
                case 11:
                    changePassword();
                    break;
                case 12:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    /**
     * Register to handle a project.
     */
    private void registerForProject() {
        System.out.println("\n==================================");
        System.out.println("    Register to Handle a Project    ");
        System.out.println("==================================");
        
        // Check if already registered for a project
        if (officer.getAssignedProject() != null) {
            System.out.println("You are already registered to handle project: " + 
                               officer.getAssignedProject().getProjectName());
            System.out.println("Status: " + officer.getRegistrationStatus());
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // View available projects
        List<Project> availableProjects = projectController.getProjectsForOfficerRegistration();
        
        if (availableProjects == null || availableProjects.isEmpty()) {
            System.out.println("No projects available for registration at this time.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nAvailable Projects for Registration:");
        System.out.println("ID\tProject Name\tNeighborhood\tAvailable Officer Slots");
        System.out.println("------------------------------------------------------------------");
        
        for (Project project : availableProjects) {
            System.out.printf("%d\t%s\t%s\t%d\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood(),
                             project.getAvailableOfficerSlots());
        }
        
        System.out.println("\nEnter the ID of the project you want to register for (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            System.out.println("Registration cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project selectedProject = projectController.getProjectByID(projectID);
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = officer.registerForProject(selectedProject);
        
        if (success) {
            System.out.println("Registration request submitted successfully!");
            System.out.println("Your registration status is now PENDING. Please wait for approval from the HDB Manager.");
        } else {
            System.out.println("Failed to register for the project. You may have already applied for this project as an applicant.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Check the status of project registration.
     */
    private void checkRegistrationStatus() {
        System.out.println("\n==================================");
        System.out.println("    Registration Status    ");
        System.out.println("==================================");
        
        String status = officer.viewRegistrationStatus();
        System.out.println("Current status: " + status);
        
        if (officer.getAssignedProject() != null) {
            System.out.println("Project: " + officer.getAssignedProject().getProjectName());
            System.out.println("Neighborhood: " + officer.getAssignedProject().getNeighborhood());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View projects with optional filters.
     */
    private void viewProjects() {
        System.out.println("\n==================================");
        System.out.println("    View Projects    ");
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
        
        List<Project> projects = officer.viewProjects(filters);
        
        if (projects == null || projects.isEmpty()) {
            System.out.println("No projects available matching your criteria.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nID\tProject Name\tNeighborhood\tFlat Types\tStatus");
        System.out.println("------------------------------------------------------------------");
        
        for (Project project : projects) {
            StringBuilder flatTypes = new StringBuilder();
            for (String type : project.getFlatTypes().keySet()) {
                flatTypes.append(type).append(": ").append(project.getFlatTypes().get(type)).append(", ");
            }
            if (flatTypes.length() > 2) {
                flatTypes.setLength(flatTypes.length() - 2); // Remove trailing comma and space
            }
            
            String status = project.isVisible() ? "Visible" : "Hidden";
            if (officer.getAssignedProject() != null && 
                project.getProjectID() == officer.getAssignedProject().getProjectID()) {
                status += " (Assigned)";
            }
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood(), 
                             flatTypes.toString(), 
                             status);
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View detailed information about a specific project.
     */
    private void viewProjectDetails() {
        System.out.println("\n==================================");
        System.out.println("    View Project Details    ");
        System.out.println("==================================");
        
        // First check if the officer is assigned to a project
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            System.out.println("You are not currently assigned to any project or your registration is not approved yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project project = officer.getAssignedProject();
        
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getOpeningDate() + " to " + project.getClosingDate());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("HDB Manager: " + project.getHdbManagerInCharge().getName());
        
        System.out.println("\nFlat Types Available:");
        for (String type : project.getFlatTypes().keySet()) {
            System.out.println(type + ": " + project.getFlatTypes().get(type) + " units");
        }
        
        System.out.println("\nOfficer Slots: " + project.getAvailableOfficerSlots() + " available out of " + 
                          project.getMaxOfficerSlots());
        
        System.out.println("\nApplications:");
        List<Application> applications = project.getApplications();
        if (applications == null || applications.isEmpty()) {
            System.out.println("No applications for this project yet.");
        } else {
            System.out.println("Total applications: " + applications.size());
            
            int pending = 0, successful = 0, unsuccessful = 0, booked = 0;
            for (Application app : applications) {
                switch (app.getStatus()) {
                    case Application.STATUS_PENDING:
                        pending++;
                        break;
                    case Application.STATUS_SUCCESSFUL:
                        successful++;
                        break;
                    case Application.STATUS_UNSUCCESSFUL:
                        unsuccessful++;
                        break;
                    case Application.STATUS_BOOKED:
                        booked++;
                        break;
                }
            }
            
            System.out.println("Pending: " + pending);
            System.out.println("Successful: " + successful);
            System.out.println("Unsuccessful: " + unsuccessful);
            System.out.println("Booked: " + booked);
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process successful applications.
     */
    private void processApplications() {
        System.out.println("\n==================================");
        System.out.println("    Process Applications    ");
        System.out.println("==================================");
        
        // Check if assigned to a project and approved
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            System.out.println("You are not currently assigned to any project or your registration is not approved yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project project = officer.getAssignedProject();
        List<Application> successfulApplications = applicationController.getSuccessfulApplications(project);
        
        if (successfulApplications == null || successfulApplications.isEmpty()) {
            System.out.println("No successful applications pending for flat booking.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nSuccessful Applications Pending Booking:");
        System.out.println("ID\tApplicant Name\tNRIC\tAge\tMarital Status");
        System.out.println("------------------------------------------------------------------");
        
        for (Application app : successfulApplications) {
            System.out.printf("%d\t%s\t%s\t%d\t%s\n", 
                             app.getApplicationID(), 
                             app.getApplicant().getName(),
                             app.getApplicant().getUserID(),
                             app.getApplicant().getAge(),
                             app.getApplicant().getMaritalStatus());
        }
        
        System.out.println("\nEnter the ID of the application to process (0 to cancel):");
        int applicationID = getIntInput();
        
        if (applicationID == 0) {
            System.out.println("Processing cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Application selectedApp = null;
        for (Application app : successfulApplications) {
            if (app.getApplicationID() == applicationID) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid application ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display flat types available
        Map<String, Integer> flatTypes = project.getFlatTypes();
        System.out.println("\nAvailable Flat Types:");
        for (String type : flatTypes.keySet()) {
            int available = flatTypes.get(type);
            if (available > 0) {
                System.out.println(type + ": " + available + " units available");
            }
        }
        
        // Get eligible flat types for the applicant
        List<String> eligibleTypes = applicationController.getEligibleFlatTypes(selectedApp.getApplicant());
        
        System.out.println("\nEligible Flat Types for Applicant:");
        for (String type : eligibleTypes) {
            if (flatTypes.containsKey(type) && flatTypes.get(type) > 0) {
                System.out.println(type);
            }
        }
        
        System.out.println("\nEnter flat type for booking (2-Room/3-Room):");
        String flatType = scanner.nextLine();
        
        if (!flatTypes.containsKey(flatType) || flatTypes.get(flatType) <= 0) {
            System.out.println("Invalid flat type or no units available!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        if (!eligibleTypes.contains(flatType)) {
            System.out.println("Applicant is not eligible for this flat type!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Process the booking
        boolean success = applicationController.bookFlat(selectedApp, flatType, officer);
        
        if (success) {
            System.out.println("Flat booked successfully!");
            System.out.println("Application status updated to BOOKED.");
            System.out.println("Flat availability updated.");
        } else {
            System.out.println("Failed to book flat. Please try again.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View and reply to enquiries.
     */
    private void viewAndReplyToEnquiries() {
        System.out.println("\n==================================");
        System.out.println("    View and Reply to Enquiries    ");
        System.out.println("==================================");
        
        // Check if assigned to a project and approved
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            System.out.println("You are not currently assigned to any project or your registration is not approved yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project project = officer.getAssignedProject();
        List<Enquiry> projectEnquiries = enquiryController.getProjectEnquiries(project);
        
        if (projectEnquiries == null || projectEnquiries.isEmpty()) {
            System.out.println("No enquiries for this project yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nEnquiries for Project " + project.getProjectName() + ":");
        System.out.println("ID\tApplicant\tCreated On\tEnquiry\tStatus");
        System.out.println("------------------------------------------------------------------");
        
        for (Enquiry enquiry : projectEnquiries) {
            String status = enquiry.getResponse() == null ? "Pending" : "Responded";
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             enquiry.getEnquiryID(), 
                             enquiry.getUser().getName(),
                             enquiry.getCreationDate(),
                             enquiry.getEnquiryText().length() > 20 ? 
                                enquiry.getEnquiryText().substring(0, 20) + "..." : 
                                enquiry.getEnquiryText(),
                             status);
        }
        
        System.out.println("\nEnter the ID of the enquiry to view/reply (0 to cancel):");
        int enquiryID = getIntInput();
        
        if (enquiryID == 0) {
            System.out.println("Operation cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Enquiry selectedEnquiry = null;
        for (Enquiry enquiry : projectEnquiries) {
            if (enquiry.getEnquiryID() == enquiryID) {
                selectedEnquiry = enquiry;
                break;
            }
        }
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid enquiry ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display enquiry details
        System.out.println("\nEnquiry Details:");
        System.out.println("ID: " + selectedEnquiry.getEnquiryID());
        System.out.println("From: " + selectedEnquiry.getUser().getName() + " (" + 
                           selectedEnquiry.getUser().getUserID() + ")");
        System.out.println("Created On: " + selectedEnquiry.getCreationDate());
        System.out.println("Enquiry: " + selectedEnquiry.getEnquiryText());
        
        if (selectedEnquiry.getResponse() != null) {
            System.out.println("Response: " + selectedEnquiry.getResponse());
            
            System.out.println("\nThis enquiry has already been responded to.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nEnter your response:");
        String response = scanner.nextLine();
        
        if (response.trim().isEmpty()) {
            System.out.println("Response cannot be empty. Operation cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = officer.replyToEnquiry(selectedEnquiry, response);
        
        if (success) {
            System.out.println("Response submitted successfully!");
        } else {
            System.out.println("Failed to submit response. Please try again.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generate a receipt for a flat booking.
     */
    private void generateBookingReceipt() {
        System.out.println("\n==================================");
        System.out.println("    Generate Booking Receipt    ");
        System.out.println("==================================");
        
        // Check if assigned to a project and approved
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            System.out.println("You are not currently assigned to any project or your registration is not approved yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project project = officer.getAssignedProject();
        List<Application> bookedApplications = applicationController.getBookedApplications(project);
        
        if (bookedApplications == null || bookedApplications.isEmpty()) {
            System.out.println("No booked applications to generate receipts for.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nBooked Applications:");
        System.out.println("ID\tApplicant Name\tNRIC\tFlat Type");
        System.out.println("------------------------------------------------------------------");
        
        for (Application app : bookedApplications) {
            System.out.printf("%d\t%s\t%s\t%s\n", 
                             app.getApplicationID(), 
                             app.getApplicant().getName(),
                             app.getApplicant().getUserID(),
                             app.getFlatType());
        }
        
        System.out.println("\nEnter the ID of the application to generate receipt for (0 to cancel):");
        int applicationID = getIntInput();
        
        if (applicationID == 0) {
            System.out.println("Operation cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Application selectedApp = null;
        for (Application app : bookedApplications) {
            if (app.getApplicationID() == applicationID) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid application ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Receipt receipt = officer.generateReceipt(selectedApp);
        
        if (receipt != null) {
            System.out.println("\n==================================");
            System.out.println("         BOOKING RECEIPT          ");
            System.out.println("==================================");
            System.out.println("Receipt ID: " + receipt.getReceiptID());
            System.out.println("Date: " + receipt.getGenerationDate());
            System.out.println("\nApplicant Details:");
            System.out.println("Name: " + selectedApp.getApplicant().getName());
            System.out.println("NRIC: " + selectedApp.getApplicant().getUserID());
            System.out.println("Age: " + selectedApp.getApplicant().getAge());
            System.out.println("Marital Status: " + selectedApp.getApplicant().getMaritalStatus());
            
            System.out.println("\nProject Details:");
            System.out.println("Project: " + selectedApp.getProject().getProjectName());
            System.out.println("Neighborhood: " + selectedApp.getProject().getNeighborhood());
            System.out.println("Flat Type: " + selectedApp.getFlatType());
            
            System.out.println("\nProcessed by HDB Officer: " + officer.getName());
            System.out.println("Officer ID: " + officer.getUserID());
            
            System.out.println("\nReceipt generated successfully!");
        } else {
            System.out.println("Failed to generate receipt. Please ensure the application is in BOOKED status.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Apply for a project as an Applicant.
     */
    private void applyForProject() {
        System.out.println("\n==================================");
        System.out.println("    Apply for a Project (as Applicant)    ");
        System.out.println("==================================");
        
        // Check if handling a project
        if (officer.getAssignedProject() != null) {
            System.out.println("You cannot apply for a project while handling a project as an HDB Officer.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // View available projects
        viewProjects();
        
        System.out.println("Enter the ID of the project you want to apply for (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            System.out.println("Application cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = applicationController.applyForProject(projectID, officer);
        
        if (success) {
            System.out.println("Application submitted successfully!");
        } else {
            System.out.println("Failed to submit application. Please check your eligibility and the project availability.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Create an enquiry for a project.
     */
    private void createEnquiry() {
        System.out.println("\n==================================");
        System.out.println("    Create Enquiry    ");
        System.out.println("==================================");
        
        // View projects to enquire about
        viewProjects();
        
        System.out.println("Enter the ID of the project you want to enquire about (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            System.out.println("Enquiry cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project project = projectController.getProjectByID(projectID);
        
        if (project == null) {
            System.out.println("Invalid project ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Enter your enquiry:");
        String enquiryText = scanner.nextLine();
        
        if (enquiryText.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty. Operation cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = enquiryController.createEnquiry(officer, project, enquiryText);
        
        if (success) {
            System.out.println("Enquiry submitted successfully!");
        } else {
            System.out.println("Failed to submit enquiry. Please try again.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View enquiries made by the officer.
     */
    private void viewEnquiries() {
        System.out.println("\n==================================");
        System.out.println("    View My Enquiries    ");
        System.out.println("==================================");
        
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(officer);
        
        if (userEnquiries == null || userEnquiries.isEmpty()) {
            System.out.println("You have not made any enquiries yet.");
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nYour Enquiries:");
        System.out.println("ID\tProject\tCreated On\tEnquiry\tStatus");
        System.out.println("------------------------------------------------------------------");
        
        for (Enquiry enquiry : userEnquiries) {
            String status = enquiry.getResponse() == null ? "Pending" : "Responded";
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             enquiry.getEnquiryID(), 
                             enquiry.getProject().getProjectName(),
                             enquiry.getCreationDate(),
                             enquiry.getEnquiryText().length() > 20 ? 
                                enquiry.getEnquiryText().substring(0, 20) + "..." : 
                                enquiry.getEnquiryText(),
                             status);
        }
        
        System.out.println("\nEnter the ID of the enquiry to view details (0 to cancel):");
        int enquiryID = getIntInput();
        
        if (enquiryID == 0) {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Enquiry selectedEnquiry = null;
        for (Enquiry enquiry : userEnquiries) {
            if (enquiry.getEnquiryID() == enquiryID) {
                selectedEnquiry = enquiry;
                break;
            }
        }
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid enquiry ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display enquiry details
        System.out.println("\nEnquiry Details:");
        System.out.println("ID: " + selectedEnquiry.getEnquiryID());
        System.out.println("Project: " + selectedEnquiry.getProject().getProjectName());
        System.out.println("Created On: " + selectedEnquiry.getCreationDate());
        System.out.println("Enquiry: " + selectedEnquiry.getEnquiryText());
        
        if (selectedEnquiry.getResponse() != null) {
            System.out.println("Response: " + selectedEnquiry.getResponse());
        } else {
            System.out.println("Status: Pending response");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Change the officer's password.
     */
    private void changePassword() {
        System.out.println("\n==================================");
        System.out.println("    Change Password    ");
        System.out.println("==================================");
        
        System.out.println("Enter current password:");
        String currentPassword = scanner.nextLine();
        
        System.out.println("Enter new password:");
        String newPassword = scanner.nextLine();
        
        System.out.println("Confirm new password:");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = officer.changePassword(currentPassword, newPassword);
        
        if (success) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Please check your current password and try again.");
        }
        
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