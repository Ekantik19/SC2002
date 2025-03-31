package com.bto.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bto.controller.ApplicationController;
import com.bto.controller.ProjectController;
import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * View class for managing applications in the BTO Management System.
 * This class extends the abstract base view class {@link ARenderView}
 */
public class ApplicationView extends ARenderView {
    private ApplicationController applicationController;
    private ProjectController projectController;
    private User currentUser;
    
    /**
     * Constructs a new ApplicationView with the specified controllers and user.
     * 
     * @param applicationController The controller for application operations
     * @param projectController The controller for project operations
     * @param currentUser The currently logged in user
     */
    public ApplicationView(ApplicationController applicationController, ProjectController projectController, User currentUser) {
        super();
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.currentUser = currentUser;
    }
    
    /**
     * Renders the application view based on the selection.
     * 
     * @param selection The view to render: 
     *                  0 - Main application menu
     *                  1 - View application status
     *                  2 - Apply for project
     *                  3 - Request withdrawal
     *                  4 - Process applications (Manager)
     *                  5 - Process withdrawal requests (Manager)
     *                  6 - Book flat (Officer)
     */
    @Override
    public void renderApp(int selection) {
        switch (selection) {
            case 0:
                // Main application menu
                renderChoice();
                displayApplicationMenu();
                break;
                
            case 1:
                // View application status
                renderChoice();
                viewApplicationStatus();
                break;
                
            case 2:
                // Apply for project
                renderChoice();
                applyForProject();
                break;
                
            case 3:
                // Request withdrawal
                renderChoice();
                requestWithdrawal();
                break;
                
            case 4:
                // Process applications (Manager only)
                renderChoice();
                processApplications();
                break;
                
            case 5:
                // Process withdrawal requests (Manager only)
                renderChoice();
                processWithdrawalRequests();
                break;
                
            case 6:
                // Book flat (Officer only)
                renderChoice();
                bookFlat();
                break;
                
            default:
                System.out.println("Invalid selection");
                delay(1);
                renderApp(0);
                break;
        }
    }
    
    /**
     * Renders the header for the application view.
     */
    @Override
    public void renderChoice() {
        printBTOHeader("Application Management");
    }
    
    /**
     * Displays the main application menu based on user role.
     */
    private void displayApplicationMenu() {
        System.out.println("Application Management Options:");
        
        // Common options for all users
        System.out.println("1. View Application Status");
        
        // Options specific to Applicants
        if (currentUser instanceof Applicant) {
            System.out.println("2. Apply for Project");
            System.out.println("3. Request Withdrawal");
        }
        
        // Options specific to HDB Managers
        if (currentUser instanceof HDBManager) {
            System.out.println("4. Process Applications");
            System.out.println("5. Process Withdrawal Requests");
        }
        
        // Options specific to HDB Officers
        if (currentUser instanceof HDBOfficer) {
            System.out.println("6. Book Flat for Applicant");
        }
        
        System.out.println("0. Return to Main Menu");
        
        int choice = getInputInt("Enter your choice: ");
        
        switch (choice) {
            case 0:
                // Return to main menu
                return;
                
            case 1:
                renderApp(1);
                break;
                
            case 2:
                if (currentUser instanceof Applicant) {
                    renderApp(2);
                } else {
                    System.out.println("Invalid option for your role.");
                    delay(1);
                    renderApp(0);
                }
                break;
                
            case 3:
                if (currentUser instanceof Applicant) {
                    renderApp(3);
                } else {
                    System.out.println("Invalid option for your role.");
                    delay(1);
                    renderApp(0);
                }
                break;
                
            case 4:
                if (currentUser instanceof HDBManager) {
                    renderApp(4);
                } else {
                    System.out.println("Invalid option for your role.");
                    delay(1);
                    renderApp(0);
                }
                break;
                
            case 5:
                if (currentUser instanceof HDBManager) {
                    renderApp(5);
                } else {
                    System.out.println("Invalid option for your role.");
                    delay(1);
                    renderApp(0);
                }
                break;
                
            case 6:
                if (currentUser instanceof HDBOfficer) {
                    renderApp(6);
                } else {
                    System.out.println("Invalid option for your role.");
                    delay(1);
                    renderApp(0);
                }
                break;
                
            default:
                System.out.println("Invalid choice. Please try again.");
                delay(1);
                renderApp(0);
                break;
        }
    }
    
    /**
     * Displays the current user's application status.
     */
    private void viewApplicationStatus() {
        printSingleBorder("Application Status");
        
        String status = applicationController.viewApplicationStatus();
        
        if ("No Application".equals(status)) {
            System.out.println("You have not applied for any BTO projects yet.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        if ("Not an applicant".equals(status)) {
            System.out.println("This function is only available for applicants.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Get applicant's current application details
        Application application = null;
        if (currentUser instanceof Applicant) {
            Applicant applicant = (Applicant) currentUser;
            application = applicant.getCurrentApplication();
        }
        
        if (application == null) {
            System.out.println("Application status: " + status);
            System.out.println("No further details available.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Display application details
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("Project: " + application.getProject().getProjectName());
        System.out.println("Neighborhood: " + application.getProject().getNeighborhood());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        
        System.out.println("Status: " + application.getStatus());
        
        if (Application.STATUS_BOOKED.equals(application.getStatus())) {
            System.out.println("Flat Type Booked: " + application.getFlatTypeBooked());
        }
        
        if (application.isWithdrawalRequested()) {
            System.out.println("Withdrawal Requested: Yes (pending approval)");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Handles the process of applying for a project.
     */
    private void applyForProject() {
        if (!(currentUser instanceof Applicant)) {
            System.out.println("Only applicants can apply for projects.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Check if already has an application
        //String status = applicationController.viewApplicationStatus();
        String status = applicationController.viewApplicationStatus();
        if (!"No Application".equals(status)) {
            System.out.println("You already have an application. Current status: " + status);
            System.out.println("You must withdraw your current application before applying for a new one.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Get available projects
        List<Project> availableProjects = projectController.getAvailableProjects(currentUser);
        
        if (availableProjects.isEmpty()) {
            System.out.println("No projects available for your eligibility criteria.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Available Projects");
        
        System.out.println("┌─────┬──────────────────┬──────────────────┬─────────────┬────────────┐");
        System.out.println("│ ID  │ Project Name     │ Neighborhood     │ Status      │ Flat Types │");
        System.out.println("├─────┼──────────────────┼──────────────────┼─────────────┼────────────┤");
        
        for (Project project : availableProjects) {
            //String status = getProjectStatus(project);
            String projectStatus = getProjectStatus(project);
            String flatTypes = formatFlatTypes(project);
            
            System.out.printf("│ %-3d │ %-16s │ %-16s │ %-11s │ %-10s │\n", 
                             project.getProjectID(),
                             truncateString(project.getProjectName(), 16),
                             truncateString(project.getNeighborhood(), 16),
                             status,
                             flatTypes);
        }
        
        System.out.println("└─────┴──────────────────┴──────────────────┴─────────────┴────────────┘");
        
        int projectID = getInputInt("Enter project ID to apply for (0 to cancel): ");
        
        if (projectID == 0) {
            System.out.println("Application cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Check if project ID is valid
        boolean validProject = false;
        for (Project project : availableProjects) {
            if (project.getProjectID() == projectID) {
                validProject = true;
                break;
            }
        }
        
        if (!validProject) {
            System.out.println("Invalid project ID. Please try again.");
            delay(2);
            renderApp(2);
            return;
        }
        
        // Submit application
        boolean success = applicationController.applyForProject(projectID);
        
        if (success) {
            System.out.println("Application submitted successfully!");
            System.out.println("Your application is now pending approval.");
        } else {
            System.out.println("Failed to submit application. Please check your eligibility and the project availability.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Handles the process of requesting withdrawal for an application.
     */
    private void requestWithdrawal() {
        if (!(currentUser instanceof Applicant)) {
            System.out.println("Only applicants can request withdrawal.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Check if has an application
        String status = applicationController.viewApplicationStatus();
        if ("No Application".equals(status)) {
            System.out.println("You don't have an active application to withdraw from.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Request Withdrawal");
        
        System.out.println("Current application status: " + status);
        System.out.println("\nNote: Withdrawal requests must be approved by HDB Manager.");
        System.out.println("If your application is already in 'BOOKED' status, the flat will be returned to the available pool.");
        
        System.out.print("\nAre you sure you want to withdraw your application? (Y/N): ");
        String confirm = sc.nextLine().trim().toUpperCase();
        
        if (!"Y".equals(confirm)) {
            System.out.println("Withdrawal request cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        boolean success = applicationController.requestWithdrawal();
        
        if (success) {
            System.out.println("Withdrawal request submitted successfully!");
            System.out.println("Your request is now pending approval from HDB Manager.");
        } else {
            System.out.println("Failed to submit withdrawal request. Please try again later.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Handles the process of approving/rejecting applications (Manager only).
     */
    private void processApplications() {
        if (!(currentUser instanceof HDBManager)) {
            System.out.println("Only HDB Managers can process applications.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Get projects created by this manager
        List<Project> managerProjects = manager.getCreatedProjects();
        
        if (managerProjects.isEmpty()) {
            System.out.println("You haven't created any projects yet.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Select Project to Process Applications");
        
        for (int i = 0; i < managerProjects.size(); i++) {
            Project project = managerProjects.get(i);
            int pendingCount = countPendingApplications(project);
            
            System.out.printf("%d. %s - %d pending applications\n", 
                             i + 1, 
                             project.getProjectName(),
                             pendingCount);
        }
        
        int projectChoice = getInputInt("Enter project number (0 to cancel): ");
        
        if (projectChoice == 0 || projectChoice > managerProjects.size()) {
            System.out.println("Operation cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        Project selectedProject = managerProjects.get(projectChoice - 1);
        
        // Get pending applications for this project
        List<Application> applications = selectedProject.getApplications();
        List<Application> pendingApplications = filterApplicationsByStatus(applications, Application.STATUS_PENDING);
        
        if (pendingApplications.isEmpty()) {
            System.out.println("No pending applications for this project.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Pending Applications for " + selectedProject.getProjectName());
        
        System.out.println("┌─────┬──────────────────┬─────────────┬──────────────┬─────────────┐");
        System.out.println("│ ID  │ Applicant        │ NRIC        │ Date Applied │ Eligibility │");
        System.out.println("├─────┼──────────────────┼─────────────┼──────────────┼─────────────┤");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        for (Application app : pendingApplications) {
            String eligibleTypes = getEligibleFlatTypes(app.getApplicant(), selectedProject);
            
            System.out.printf("│ %-3d │ %-16s │ %-11s │ %-12s │ %-11s │\n", 
                             app.getApplicationID(),
                             truncateString(app.getApplicant().getName(), 16),
                             app.getApplicant().getUserID(),
                             dateFormat.format(app.getApplicationDate()),
                             eligibleTypes);
        }
        
        System.out.println("└─────┴──────────────────┴─────────────┴──────────────┴─────────────┘");
        
        int applicationID = getInputInt("Enter application ID to process (0 to cancel): ");
        
        if (applicationID == 0) {
            System.out.println("Operation cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Verify application ID
        Application selectedApp = null;
        for (Application app : pendingApplications) {
            if (app.getApplicationID() == applicationID) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid application ID. Please try again.");
            delay(2);
            renderApp(4);
            return;
        }
        
        // Display application details
        printSingleBorder("Application Details");
        
        System.out.println("Application ID: " + selectedApp.getApplicationID());
        System.out.println("Applicant: " + selectedApp.getApplicant().getName());
        System.out.println("NRIC: " + selectedApp.getApplicant().getUserID());
        System.out.println("Age: " + selectedApp.getApplicant().getAge());
        System.out.println("Marital Status: " + selectedApp.getApplicant().getMaritalStatus());
        System.out.println("Date Applied: " + dateFormat.format(selectedApp.getApplicationDate()));
        
        String eligibleTypes = getEligibleFlatTypes(selectedApp.getApplicant(), selectedProject);
        System.out.println("Eligible Flat Types: " + eligibleTypes);
        
        // Check if there are available flats
        boolean hasAvailableFlats = false;
        if (eligibleTypes.contains("2-Room") && selectedProject.getRemainingUnits("2-Room") > 0) {
            hasAvailableFlats = true;
            System.out.println("2-Room flats available: " + selectedProject.getRemainingUnits("2-Room"));
        }
        
        if (eligibleTypes.contains("3-Room") && selectedProject.getRemainingUnits("3-Room") > 0) {
            hasAvailableFlats = true;
            System.out.println("3-Room flats available: " + selectedProject.getRemainingUnits("3-Room"));
        }
        
        if (!hasAvailableFlats) {
            System.out.println("Warning: No eligible flat types available for this applicant!");
        }
        
        // Process application
        System.out.print("\nApprove this application? (Y/N): ");
        String approve = sc.nextLine().trim().toUpperCase();
        
        boolean success = applicationController.processApplication(selectedApp.getApplicationID(), "Y".equals(approve));
        
        if (success) {
            if ("Y".equals(approve)) {
                System.out.println("Application approved successfully!");
                System.out.println("The applicant can now book a flat with an HDB Officer.");
            } else {
                System.out.println("Application rejected successfully.");
            }
        } else {
            System.out.println("Failed to process application. Please check if there are eligible flat types available.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Handles the process of approving/rejecting withdrawal requests (Manager only).
     */
    private void processWithdrawalRequests() {
        if (!(currentUser instanceof HDBManager)) {
            System.out.println("Only HDB Managers can process withdrawal requests.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Get projects created by this manager
        List<Project> managerProjects = manager.getCreatedProjects();
        
        if (managerProjects.isEmpty()) {
            System.out.println("You haven't created any projects yet.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Find projects with withdrawal requests
        boolean hasWithdrawalRequests = false;
        for (Project project : managerProjects) {
            for (Application app : project.getApplications()) {
                if (app.isWithdrawalRequested()) {
                    hasWithdrawalRequests = true;
                    break;
                }
            }
            if (hasWithdrawalRequests) break;
        }
        
        if (!hasWithdrawalRequests) {
            System.out.println("No withdrawal requests to process.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Process Withdrawal Requests");
        
        System.out.println("┌─────┬──────────────────┬─────────────┬────────────┬───────────────┐");
        System.out.println("│ ID  │ Applicant        │ NRIC        │ Status     │ Project       │");
        System.out.println("├─────┼──────────────────┼─────────────┼────────────┼───────────────┤");
        
        for (Project project : managerProjects) {
            for (Application app : project.getApplications()) {
                if (app.isWithdrawalRequested()) {
                    System.out.printf("│ %-3d │ %-16s │ %-11s │ %-10s │ %-13s │\n", 
                                     app.getApplicationID(),
                                     truncateString(app.getApplicant().getName(), 16),
                                     app.getApplicant().getUserID(),
                                     app.getStatus(),
                                     truncateString(project.getProjectName(), 13));
                }
            }
        }
        
        System.out.println("└─────┴──────────────────┴─────────────┴────────────┴───────────────┘");
        
        int applicationID = getInputInt("Enter application ID to process (0 to cancel): ");
        
        if (applicationID == 0) {
            System.out.println("Operation cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Find the application
        Application selectedApp = null;
        for (Project project : managerProjects) {
            for (Application app : project.getApplications()) {
                if (app.getApplicationID() == applicationID && app.isWithdrawalRequested()) {
                    selectedApp = app;
                    break;
                }
            }
            if (selectedApp != null) break;
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid application ID. Please try again.");
            delay(2);
            renderApp(5);
            return;
        }
        
        // Display application details
        printSingleBorder("Withdrawal Request Details");
        
        System.out.println("Application ID: " + selectedApp.getApplicationID());
        System.out.println("Applicant: " + selectedApp.getApplicant().getName());
        System.out.println("NRIC: " + selectedApp.getApplicant().getUserID());
        System.out.println("Project: " + selectedApp.getProject().getProjectName());
        System.out.println("Current Status: " + selectedApp.getStatus());
        
        if (selectedApp.getFlatTypeBooked() != null) {
            System.out.println("Flat Type Booked: " + selectedApp.getFlatTypeBooked());
            System.out.println("\nNote: Approving this withdrawal will return the flat to the available pool.");
        }
        
        // Process withdrawal request
        System.out.print("\nApprove this withdrawal request? (Y/N): ");
        String approve = sc.nextLine().trim().toUpperCase();
        
        boolean success = applicationController.processWithdrawalRequest(selectedApp.getApplicationID(), "Y".equals(approve));
        
        if (success) {
            if ("Y".equals(approve)) {
                System.out.println("Withdrawal request approved successfully!");
                if (Application.STATUS_BOOKED.equals(selectedApp.getStatus())) {
                    System.out.println("The flat has been returned to the available pool.");
                }
            } else {
                System.out.println("Withdrawal request rejected successfully.");
            }
        } else {
            System.out.println("Failed to process withdrawal request. Please try again later.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Handles the process of booking a flat for an applicant (Officer only).
     */
    private void bookFlat() {
        if (!(currentUser instanceof HDBOfficer)) {
            System.out.println("Only HDB Officers can book flats.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is approved for a project
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            System.out.println("You must be approved to handle a project before booking flats.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        Project project = officer.getAssignedProject();
        
        // Get successful applications for this project
        List<Application> applications = project.getApplications();
        List<Application> successfulApplications = filterApplicationsByStatus(applications, Application.STATUS_SUCCESSFUL);
        
        if (successfulApplications.isEmpty()) {
            System.out.println("No successful applications pending for flat booking.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Book Flat for Successful Applicant");
        
        System.out.println("┌─────┬──────────────────┬─────────────┬──────────────┬─────────────┐");
        System.out.println("│ ID  │ Applicant        │ NRIC        │ Date Applied │ Eligibility │");
        System.out.println("├─────┼──────────────────┼─────────────┼──────────────┼─────────────┤");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        for (Application app : successfulApplications) {
            String eligibleTypes = getEligibleFlatTypes(app.getApplicant(), project);
            
            System.out.printf("│ %-3d │ %-16s │ %-11s │ %-12s │ %-11s │\n", 
                             app.getApplicationID(),
                             truncateString(app.getApplicant().getName(), 16),
                             app.getApplicant().getUserID(),
                             dateFormat.format(app.getApplicationDate()),
                             eligibleTypes);
        }
        
        System.out.println("└─────┴──────────────────┴─────────────┴──────────────┴─────────────┘");
        
        String userID = getInputString("Enter applicant's NRIC (0 to cancel): ");
        
        if ("0".equals(userID)) {
            System.out.println("Operation cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Find the application
        Application selectedApp = null;
        for (Application app : successfulApplications) {
            if (app.getApplicant().getUserID().equals(userID)) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid NRIC or no successful application found for this applicant.");
            delay(2);
            renderApp(6);
            return;
        }
        
        // Display application details and available flat types
        printSingleBorder("Booking Details");
        
        System.out.println("Applicant: " + selectedApp.getApplicant().getName());
        System.out.println("NRIC: " + selectedApp.getApplicant().getUserID());
        System.out.println("Age: " + selectedApp.getApplicant().getAge());
        System.out.println("Marital Status: " + selectedApp.getApplicant().getMaritalStatus());
        
        String eligibleTypes = getEligibleFlatTypes(selectedApp.getApplicant(), project);
        System.out.println("Eligible Flat Types: " + eligibleTypes);
        
        // Display available flats
        System.out.println("\nAvailable Flats:");
        boolean hasAvailableFlats = false;
        
        if (eligibleTypes.contains("2-Room") && project.getRemainingUnits("2-Room") > 0) {
            hasAvailableFlats = true;
            System.out.println("2-Room: " + project.getRemainingUnits("2-Room") + " units available");
        }
        
        if (eligibleTypes.contains("3-Room") && project.getRemainingUnits("3-Room") > 0) {
            hasAvailableFlats = true;
            System.out.println("3-Room: " + project.getRemainingUnits("3-Room") + " units available");
        }
        
        if (!hasAvailableFlats) {
            System.out.println("No eligible flat types available for this applicant!");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Select flat type to book
        System.out.println("\nSelect flat type to book:");
        
        if (eligibleTypes.contains("2-Room") && project.getRemainingUnits("2-Room") > 0) {
            System.out.println("1. 2-Room");
        }
        
        if (eligibleTypes.contains("3-Room") && project.getRemainingUnits("3-Room") > 0) {
            System.out.println("2. 3-Room");
        }
        
        int flatTypeChoice = getInputInt("Enter your choice (0 to cancel): ");
        
        if (flatTypeChoice == 0) {
            System.out.println("Booking cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        String flatType = null;
        
        if (flatTypeChoice == 1 && eligibleTypes.contains("2-Room") && project.getRemainingUnits("2-Room") > 0) {
            flatType = "2-Room";
        } else if (flatTypeChoice == 2 && eligibleTypes.contains("3-Room") && project.getRemainingUnits("3-Room") > 0) {
            flatType = "3-Room";
        }
        
        if (flatType == null) {
            System.out.println("Invalid choice or flat type not available. Please try again.");
            delay(2);
            renderApp(6);
            return;
        }
        
        // Confirm booking
        System.out.print("\nConfirm booking of " + flatType + " flat for " + 
                        selectedApp.getApplicant().getName() + "? (Y/N): ");
        String confirm = sc.nextLine().trim().toUpperCase();
        
        if (!"Y".equals(confirm)) {
            System.out.println("Booking cancelled.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        boolean success = applicationController.bookFlat(userID, flatType);
        
        if (success) {
            System.out.println("Flat booked successfully!");
            System.out.println("The application status has been updated to BOOKED.");
            System.out.println("The " + flatType + " flat has been assigned to " + selectedApp.getApplicant().getName() + ".");
            
            // Generate receipt
            System.out.print("\nWould you like to generate a booking receipt? (Y/N): ");
            String generateReceipt = sc.nextLine().trim().toUpperCase();
            
            if ("Y".equals(generateReceipt)) {
                generateReceipt(userID);
            }
        } else {
            System.out.println("Failed to book flat. Please check flat availability and try again.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Generates a receipt for a booked flat.
     * 
     * @param userID The NRIC of the applicant
     */
    private void generateReceipt(String userID) {
        if (!(currentUser instanceof HDBOfficer)) {
            System.out.println("Only HDB Officers can generate receipts.");
            return;
        }
        
        printSingleBorder("Booking Receipt");
        
        Receipt receipt = applicationController.generateReceipt(userID);
        
        if (receipt == null) {
            System.out.println("Failed to generate receipt. Please verify the application is in BOOKED status.");
            return;
        }
        
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│              HDB FLAT BOOKING RECEIPT               │");
        System.out.println("├─────────────────────────────────────────────────────┤");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        System.out.println("│ Receipt ID: " + receipt.getReceiptID());
        System.out.println("│ Date: " + dateFormat.format(receipt.getGenerationDate()));
        System.out.println("│                                                     │");
        System.out.println("│ Applicant Details:                                  │");
        System.out.println("│ Name: " + receipt.getApplication().getApplicant().getName());
        System.out.println("│ NRIC: " + receipt.getApplication().getApplicant().getUserID());
        System.out.println("│                                                     │");
        System.out.println("│ Project Details:                                    │");
        System.out.println("│ Project: " + receipt.getApplication().getProject().getProjectName());
        System.out.println("│ Neighborhood: " + receipt.getApplication().getProject().getNeighborhood());
        System.out.println("│ Flat Type: " + receipt.getApplication().getFlatTypeBooked());
        System.out.println("│                                                     │");
        System.out.println("│ HDB Officer: " + receipt.getOfficer().getName());
        System.out.println("│                                                     │");
        System.out.println("│ This receipt confirms the successful booking of     │");
        System.out.println("│ your BTO flat. Please keep this for your records.   │");
        System.out.println("└─────────────────────────────────────────────────────┘");
        
        System.out.println("\nReceipt generated successfully!");
    }
    
    /**
     * Counts the number of pending applications for a project.
     * 
     * @param project The project to count applications for
     * @return The number of pending applications
     */
    private int countPendingApplications(Project project) {
        int count = 0;
        for (Application app : project.getApplications()) {
            if (Application.STATUS_PENDING.equals(app.getStatus())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Filters applications by their status.
     * 
     * @param applications The list of applications to filter
     * @param status The status to filter by
     * @return A list of applications with the specified status
     */
    private List<Application> filterApplicationsByStatus(List<Application> applications, String status) {
        List<Application> filteredApplications = new ArrayList<>();
        for (Application app : applications) {
            if (status.equals(app.getStatus())) {
                filteredApplications.add(app);
            }
        }
        return filteredApplications;
    }
    
    /**
     * Gets the status of a project based on its dates.
     * 
     * @param project The project to get status for
     * @return A string representing the project's status
     */
    private String getProjectStatus(Project project) {
        Date now = new Date();
        
        if (now.before(project.getOpeningDate())) {
            return "Upcoming";
        } else if (now.after(project.getClosingDate())) {
            return "Closed";
        } else {
            return "Open";
        }
    }
    
    /**
     * Formats the flat types available in a project into a compact string.
     * 
     * @param project The project to format flat types for
     * @return A string representation of available flat types
     */
    private String formatFlatTypes(Project project) {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> flatTypes = project.getFlatTypes();
        
        if (flatTypes.containsKey("2-Room")) {
            sb.append("2R");
            if (flatTypes.containsKey("3-Room")) {
                sb.append(",3R");
            }
        } else if (flatTypes.containsKey("3-Room")) {
            sb.append("3R");
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the flat types an applicant is eligible for in a project.
     * 
     * @param applicant The applicant to check eligibility for
     * @param project The project to check against
     * @return A string representing eligible flat types
     */
    private String getEligibleFlatTypes(Applicant applicant, Project project) {
        StringBuilder sb = new StringBuilder();
        
        if ("Single".equals(applicant.getMaritalStatus())) {
            // Singles 35+ can only apply for 2-Room
            if (applicant.getAge() >= 35 && project.hasFlatType("2-Room")) {
                sb.append("2-Room");
            }
        } else {
            // Married 21+ can apply for any flat type
            if (applicant.getAge() >= 21) {
                if (project.hasFlatType("2-Room")) {
                    sb.append("2-Room");
                }
                
                if (project.hasFlatType("3-Room")) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append("3-Room");
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Truncates a string to the specified length with ellipsis if needed.
     * 
     * @param str The string to truncate
     * @param length The maximum length
     * @return The truncated string
     */
    private String truncateString(String str, int length) {
        if (str == null) {
            return "";
        }
        
        if (str.length() <= length) {
            return str;
        }
        
        return str.substring(0, length - 3) + "...";
    }
}