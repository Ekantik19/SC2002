package view;

import controller.ApplicationController;
import controller.ProjectController;
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
import view.interfaces.IBTOView;

/**
 * View for application-related operations in the BTO Management System.
 */
public class ApplicationView extends ARenderView implements IBTOView {
    
    private User currentUser;
    private ApplicationController applicationController;
    private Scanner scanner;
    private ProjectController projectController;
    
    /**
     * Constructor for ApplicationView.
     * 
     * @param currentUser The currently logged-in user
     * @param applicationController Controller for application operations
     */
    // public ApplicationView(User currentUser, ApplicationController applicationController) {
    //     this.currentUser = currentUser;
    //     this.applicationController = applicationController;
    //     this.scanner = new Scanner(System.in);
    // }
    public ApplicationView(User currentUser, ApplicationController applicationController, 
                      ProjectController projectController) {
    this.currentUser = currentUser;
    this.applicationController = applicationController;
    this.projectController = projectController;
    this.scanner = new Scanner(System.in);
}
    
    @Override
    public void display() {
        if (currentUser instanceof Applicant) {
            displayMyApplication();
        } else if (currentUser instanceof HDBOfficer) {
            displayOfficerApplications();
        } else if (currentUser instanceof HDBManager) {
            displayManageApplications();
        }
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
     * Displays applications for an HDB officer.
     */
    private void displayOfficerApplications() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can access this view.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            showError("You are not assigned to any project yet.");
            return;
        }
        
        printHeader("PROJECT APPLICATIONS: " + officer.getAssignedProject().getProjectName());
        
        // Get applications for the officer's project
        List<Application> applications = applicationController.getApplicationsByProject(
            officer.getAssignedProject());
        
        if (applications.isEmpty()) {
            showMessage("No applications found for this project.");
            return;
        }
        
        displayApplicationsList(applications);
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
        
        printHeader("PROCESS FLAT BOOKING");
        
        System.out.print("Enter applicant's NRIC: ");
        String nric = scanner.nextLine();
        
        Application application = officer.retrieveApplicationByNric(nric);
        
        if (application == null) {
            showError("No application found for the specified NRIC in your project.");
            return;
        }
        
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            showError("Application status must be SUCCESSFUL to process booking. Current status: " +
                     application.getStatus().getDisplayName());
            return;
        }
        
        // Display application details
        displayApplicationDetails(application);
        
        System.out.println("\nAre you sure you want to process a flat booking for this application? (Y/N)");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean booked = officer.bookFlat(application);
            
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
     * Displays the interface for generating booking receipts.
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
        
        printHeader("GENERATE BOOKING RECEIPT");
        
        System.out.print("Enter applicant's NRIC: ");
        String nric = scanner.nextLine();
        
        Application application = officer.retrieveApplicationByNric(nric);
        
        if (application == null) {
            showError("No application found for the specified NRIC in your project.");
            return;
        }
        
        if (application.getStatus() != ApplicationStatus.BOOKED) {
            showError("Application status must be BOOKED to generate a receipt. Current status: " +
                     application.getStatus().getDisplayName());
            return;
        }
        
        // Generate receipt
        Receipt receipt = officer.generateBookingReceipt(application);
        
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
    
    @Override
    public void refreshData() {
        // Refresh data if needed
    }
    
    @Override
    public boolean handleNavigation(int option) {
        // Not needed for this view
        return true;
    }
    
    @Override
    public void showMessage(String message) {
        System.out.println("\n>>> " + message);
    }
    
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