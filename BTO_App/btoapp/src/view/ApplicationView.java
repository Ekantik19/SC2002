package view;

import controller.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Applicant;
import model.Application;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.Receipt;
import model.User;
import model.enums.ApplicationStatus;
import view.abstracts.ARenderView;
import view.interfaces.*;

/**
 * View for application-related operations in the BTO Management System.
 */
public class ApplicationView extends ARenderView implements ViewInterface {
    
    private User currentUser;
    private ApplicationController applicationController;
    private Scanner scanner;
    private ProjectController projectController;
    private BookingController bookingController;
    
    /**
     * Constructs an ApplicationView with the specified controllers and current user.
     *
     * @param currentUser           the user currently logged in
     * @param applicationController the controller responsible for application logic
     * @param projectController     the controller responsible for project management
     * @param bookingController     the controller responsible for booking management
     */
    public ApplicationView(User currentUser, ApplicationController applicationController, 
                      ProjectController projectController, BookingController bookingController) {
        this.currentUser = currentUser;
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.bookingController=bookingController;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Displays the current user's application.
     */
    public void displayMyApplication() {
        if (!(currentUser instanceof Applicant)) {
            showError("Only applicants can view their applications.");
            return;
        }
        
        Applicant applicant = (Applicant) currentUser;
        
        printHeader("MY APPLICATION");
        
        Application currentApplication = applicant.getCurrentApplication();
        
        if (currentApplication == null) {
            showMessage("You do not have any active applications.");
            return;
        }
        
        displayApplicationDetails(currentApplication);
    }

    /**
     * Displays application management interface for an HDB manager.
     */
    public void displayManageApplications() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can access this view.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("MANAGE APPLICATIONS");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You are not managing any projects.");
            return;
        }
        
        System.out.println("Select a project:");
        int index = 1;
        for (Project project : projects) {
            System.out.printf("%d. %s\n", index++, project.getProjectName());
        }
        
        System.out.print("\nEnter selection (1-" + projects.size() + "): ");
        int projectChoice = getIntInput();
        
        if (projectChoice < 1 || projectChoice > projects.size()) {
            showError("Invalid selection.");
            return;
        }
        
        Project selectedProject = projects.get(projectChoice - 1);
        
        printHeader("APPLICATIONS FOR: " + selectedProject.getProjectName());
        
        // Get applications for the selected project
        List<Application> applications = applicationController.getApplicationsByProject(selectedProject);
        
        if (applications.isEmpty()) {
            showMessage("No applications found for this project.");
            return;
        }
        
        // Filter options
        System.out.println("Filter by status:");
        System.out.println("1. All Applications");
        System.out.println("2. Pending Applications");
        System.out.println("3. Successful Applications");
        System.out.println("4. Unsuccessful Applications");
        System.out.println("5. Booked Applications");
        
        System.out.print("\nEnter selection (1-5): ");
        int filterChoice = getIntInput();
        
        List<Application> filteredApplications;
        
        switch (filterChoice) {
            case 2:
                filteredApplications = applicationController.getApplicationsByStatus(
                    selectedProject, ApplicationStatus.PENDING);
                break;
            case 3:
                filteredApplications = applicationController.getApplicationsByStatus(
                    selectedProject, ApplicationStatus.SUCCESSFUL);
                break;
            case 4:
                filteredApplications = applicationController.getApplicationsByStatus(
                    selectedProject, ApplicationStatus.UNSUCCESSFUL);
                break;
            case 5:
                filteredApplications = applicationController.getApplicationsByStatus(
                    selectedProject, ApplicationStatus.BOOKED);
                break;
            default:
                filteredApplications = applications;
        }
        
        if (filteredApplications.isEmpty()) {
            showMessage("No applications found with the selected filter.");
            return;
        }
        
        displayApplicationsList(filteredApplications);
        
        System.out.print("\nEnter application ID to manage, or 0 to return: ");
        String applicationId = scanner.nextLine();
        
        if (!applicationId.equals("0")) {
            Application selectedApplication = null;
            for (Application app : filteredApplications) {
                if (app.getApplicationId().equals(applicationId)) {
                    selectedApplication = app;
                    break;
                }
            }
            
            if (selectedApplication != null) {
                displayManageApplication(selectedApplication, manager);
            } else {
                showError("Invalid application ID.");
            }
        }
    }
    
    /**
     * Displays the withdrawal request interface.
     */
    public void displayWithdrawalRequest() {
        if (!(currentUser instanceof Applicant)) {
            showError("Only applicants can request application withdrawals.");
            return;
        }
        
        Applicant applicant = (Applicant) currentUser;
        
        printHeader("REQUEST APPLICATION WITHDRAWAL");
        
        Application currentApplication = applicant.getCurrentApplication();
        
        if (currentApplication == null) {
            showError("You do not have any active applications to withdraw.");
            return;
        }
        
        // Check if withdrawal is already requested
        if (currentApplication.isWithdrawalRequested()) {
            showMessage("You have already requested a withdrawal for this application. Please wait for approval.");
            return;
        }
        
        System.out.println("Are you sure you want to request withdrawal of your application for " +
                          currentApplication.getSelectedFlatType().getDisplayName() + " in " +
                          currentApplication.getProject().getProjectName() + "? (Y/N)");
        
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean requested = applicationController.requestWithdrawal(
                currentApplication.getApplicationId(), applicant);
            
            if (requested) {
                showMessage("Withdrawal request submitted successfully.");
            } else {
                showError("Failed to submit withdrawal request. Please try again later.");
            }
        } else {
            showMessage("Withdrawal request cancelled.");
        }
    }
    
    /**
     * Displays the interface for processing flat bookings.
     */
    public void displayProcessBooking() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can process flat bookings.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            showError("You are not assigned to any project yet.");
            return;
        }
        
        Project project = officer.getAssignedProject();
        printHeader("PROCESS FLAT BOOKING: " + project.getProjectName());

        System.out.println("Processing flat booking for project: " + project.getProjectName());

        // Get all applications for the project
        List<Application> allApplications = applicationController.getApplicationsByProject(project);
        System.out.println("Total applications in project: " + allApplications.size());
        
        // Log all application details
        for (Application app : allApplications) {
            System.out.println("Application - ID: " + app.getApplicationId() + 
                            ", Applicant: " + app.getApplicant().getName() + 
                            ", Status: " + app.getStatus());
        }

        List<Application> successfulApplications = applicationController.getApplicationsByStatus(
            project, ApplicationStatus.SUCCESSFUL);

        System.out.println("Successful applications: " + successfulApplications.size());
        
        if (successfulApplications.isEmpty()) {
            showMessage("No successful applications found that are eligible for booking.");
            return;
        }
        
        System.out.println("The following applications are eligible for booking:");
        System.out.println("ID | Applicant Name | NRIC | Flat Type | Application Date");
        System.out.println("-----------------------------------------------------------");
        
        int index = 1;
        for (Application app : successfulApplications) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            System.out.printf("%2d | %-20s | %-12s | %-10s | %s\n",
                            index++, 
                            app.getApplicant().getName(),
                            app.getApplicant().getNric(),
                            app.getSelectedFlatType().getDisplayName(),
                            dateFormat.format(app.getApplicationDate()));
        }
        
        // Ask which application to process
        System.out.print("\nEnter the number of the application to process (1-" + 
                        successfulApplications.size() + "), or 0 to cancel: ");
        int choice = getIntInput();
        
        if (choice <= 0 || choice > successfulApplications.size()) {
            showMessage("Operation cancelled.");
            return;
        }
        
        Application selectedApplication = successfulApplications.get(choice - 1);
        
        // Display application details
        displayApplicationDetails(selectedApplication);
        
        // Confirm booking
        System.out.println("\nOptions:");
        System.out.println("1. Process Flat Booking");
        System.out.println("2. Cancel");
        System.out.print("\nEnter your choice (1-2): ");
        int bookingChoice = getIntInput();
        
        if (bookingChoice == 1) {
            boolean booked = bookingController.bookFlat(selectedApplication.getApplicationId(), officer);
            
            if (booked) {
                showMessage("Flat booking processed successfully.");
            } else {
                showError("Failed to process flat booking. Please try again later.");
            }
        } else {
            showMessage("Flat booking cancelled.");
        }
    }
    
    /**
     * Displays and manages withdrawal requests for projects managed by the current HDB Manager.
     * Only accessible to users with HDBManager role.
     * Prompts the manager to select a project, lists withdrawal requests, and allows management of a selected request.
     */
    public void displayGenerateReceipt() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can generate booking receipts.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            showError("You are not assigned to any project yet.");
            return;
        }
        
        Project project = officer.getAssignedProject();
        printHeader("GENERATE BOOKING RECEIPT: " + project.getProjectName());
        
        // Get all BOOKED applications for the project
        List<Application> bookedApplications = applicationController.getApplicationsByStatus(
            project, ApplicationStatus.BOOKED);
        
        if (bookedApplications.isEmpty()) {
            showMessage("No booked applications found for receipt generation.");
            return;
        }
        
        System.out.println("The following applications have booked status:");
        System.out.println("ID | Applicant Name | NRIC | Flat Type | Booking Date");
        System.out.println("--------------------------------------------------------");
        
        int index = 1;
        for (Application app : bookedApplications) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            System.out.printf("%2d | %-20s | %-12s | %-10s | %s\n",
                            index++, 
                            app.getApplicant().getName(),
                            app.getApplicant().getNric(),
                            app.getSelectedFlatType().getDisplayName(),
                            dateFormat.format(app.getApplicationDate()));
        }
        
        // Ask which application to generate receipt for
        System.out.print("\nEnter the number of the application to generate receipt for (1-" + 
                        bookedApplications.size() + "), or 0 to cancel: ");
        int choice = getIntInput();
        
        if (choice <= 0 || choice > bookedApplications.size()) {
            showMessage("Operation cancelled.");
            return;
        }
        
        Application selectedApplication = bookedApplications.get(choice - 1);
        
        // Generate receipt
        Receipt receipt = officer.generateBookingReceipt(selectedApplication);
        
        if (receipt != null) {
            displayReceipt(receipt);
        } else {
            showError("Failed to generate receipt. Please try again later.");
        }
    }
    
    /**
     * Displays the interface for managing withdrawal requests.
     */
    public void displayManageWithdrawals() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can manage withdrawal requests.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("MANAGE WITHDRAWAL REQUESTS");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You are not managing any projects.");
            return;
        }
        
        System.out.println("Select a project:");
        int index = 1;
        for (Project project : projects) {
            System.out.printf("%d. %s\n", index++, project.getProjectName());
        }
        
        System.out.print("\nEnter selection (1-" + projects.size() + "): ");
        int projectChoice = getIntInput();
        
        if (projectChoice < 1 || projectChoice > projects.size()) {
            showError("Invalid selection.");
            return;
        }
        
        Project selectedProject = projects.get(projectChoice - 1);
        
        printHeader("WITHDRAWAL REQUESTS FOR: " + selectedProject.getProjectName());
        
        // Get applications with withdrawal requests for the selected project
        List<Application> withdrawalRequests = new ArrayList<>();
        List<Application> applications = applicationController.getApplicationsByProject(selectedProject);
        
        for (Application app : applications) {
            if (app.isWithdrawalRequested()) {
                withdrawalRequests.add(app);
            }
        }
        
        if (withdrawalRequests.isEmpty()) {
            showMessage("No withdrawal requests found for this project.");
            return;
        }
        
        displayApplicationsList(withdrawalRequests);
        
        System.out.print("\nEnter application ID to manage, or 0 to return: ");
        String applicationId = scanner.nextLine();
        
        if (!applicationId.equals("0")) {
            Application selectedApplication = null;
            for (Application app : withdrawalRequests) {
                if (app.getApplicationId().equals(applicationId)) {
                    selectedApplication = app;
                    break;
                }
            }
            
            if (selectedApplication != null) {
                displayManageWithdrawal(selectedApplication, manager);
            } else {
                showError("Invalid application ID.");
            }
        }
    }
    
    /**
     * Displays a list of applications.
     * 
     * @param applications The list of applications to display
     */
    private void displayApplicationsList(List<Application> applications) {
        System.out.println("ID | Applicant | Project | Flat Type | Status");
        System.out.println("-------------------------------------------");
        
        for (Application application : applications) {
            String status = application.getStatus().getDisplayName();
            if (application.isWithdrawalRequested()) {
                status += " (Withdrawal Requested)";
            }
            
            System.out.printf("%-20s | %-20s | %-20s | %-10s | %s\n",
                             application.getApplicationId(),
                             application.getApplicant().getName(),
                             application.getProject().getProjectName(),
                             application.getSelectedFlatType().getDisplayName(),
                             status);
        }
    }
    
    /**
     * Displays detailed information about an application.
     * 
     * @param application The application to display
     */
    private void displayApplicationDetails(Application application) {
        printHeader("APPLICATION DETAILS");
        
        System.out.println("Application ID: " + application.getApplicationId());
        System.out.println("Applicant: " + application.getApplicant().getName() + " (" + 
                          application.getApplicant().getNric() + ")");
        System.out.println("Project: " + application.getProject().getProjectName());
        System.out.println("Neighborhood: " + application.getProject().getNeighborhood());
        System.out.println("Flat Type: " + application.getSelectedFlatType().getDisplayName());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        System.out.println("Status: " + application.getStatus().getDisplayName());
        
        if (application.isWithdrawalRequested()) {
            System.out.println("Withdrawal Requested: Yes");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays management options for an application.
     * 
     * @param application The application to manage
     * @param manager The HDB manager
     */
    private void displayManageApplication(Application application, HDBManager manager) {
        printHeader("MANAGE APPLICATION: " + application.getApplicationId());
        
        // Display options based on current status
        System.out.println("Select action:");
        
        if (application.getStatus() == ApplicationStatus.PENDING) {
            System.out.println("1. Approve Application");
            System.out.println("2. Reject Application");
        } else {
            System.out.println("Current status is " + application.getStatus().getDisplayName() + 
                              ". No actions available.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("0. Return");
        
        System.out.print("\nEnter selection: ");
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                if (application.getStatus() == ApplicationStatus.PENDING) {
                    boolean approved = applicationController.approveApplication(
                        application.getApplicationId(), manager);
                    
                    if (approved) {
                        showMessage("Application approved successfully.");
                    } else {
                        showError("Failed to approve application. Please check availability.");
                    }
                }
                break;
            case 2:
                if (application.getStatus() == ApplicationStatus.PENDING) {
                    boolean rejected = applicationController.rejectApplication(
                        application.getApplicationId(), manager);
                    
                    if (rejected) {
                        showMessage("Application rejected successfully.");
                    } else {
                        showError("Failed to reject application.");
                    }
                }
                break;
        }
    }
    
    /**
     * Displays management options for a withdrawal request.
     * 
     * @param application The application with a withdrawal request
     * @param manager The HDB manager
     */
    private void displayManageWithdrawal(Application application, HDBManager manager) {
        printHeader("MANAGE WITHDRAWAL REQUEST: " + application.getApplicationId());
        
        System.out.println("Select action:");
        System.out.println("1. Approve Withdrawal");
        System.out.println("2. Reject Withdrawal");
        System.out.println("0. Return");
        
        System.out.print("\nEnter selection: ");
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                boolean approved = applicationController.approveWithdrawal(
                    application.getApplicationId(), manager);
                
                if (approved) {
                    showMessage("Withdrawal approved successfully.");
                } else {
                    showError("Failed to approve withdrawal.");
                }
                break;
            case 2:
                boolean rejected = applicationController.rejectWithdrawal(
                    application.getApplicationId(), manager);
                
                if (rejected) {
                    showMessage("Withdrawal rejected successfully.");
                } else {
                    showError("Failed to reject withdrawal.");
                }
                break;
        }
    }
    
    /**
     * Displays a receipt.
     * 
     * @param receipt The receipt to display
     */
    private void displayReceipt(Receipt receipt) {
        printHeader("BOOKING RECEIPT");
        
        System.out.println(receipt.generateFormattedReceipt());
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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
    
    /**
     * Gets an integer input from the user.
     * 
     * @return The integer input
     */
    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}