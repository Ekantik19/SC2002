package com.bto.view;

import java.util.ArrayList;
import java.util.List;

import com.bto.controller.ApplicationController;
import com.bto.controller.ProjectController;
import com.bto.model.Application;
import com.bto.model.Project;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * ApplicationView class is responsible for rendering application-related views
 * in the BTO Management System.
 * 
 * This class extends the abstract base view class {@link ARenderView}.
 */
public class ApplicationView extends ARenderView {
    private User currentUser;
    private ApplicationController applicationController;
    private ProjectController projectController;

    /**
     * Constructs a new ApplicationView with the necessary dependencies.
     * 
     * @param currentUser The user currently interacting with the system
     * @param applicationController Controller for application-related operations
     */
    public ApplicationView(User currentUser, ApplicationController applicationController) {
        this.currentUser = currentUser;
        this.applicationController = applicationController;
    }

    /**
     * Renders the application based on the user's selection.
     * 
     * @param selection The selected menu option
     *                  <ul>
     *                      <li>0: Main menu</li>
     *                      <li>1: View All Applications</li>
     *                      <li>2: View Pending Applications</li>
     *                      <li>3: View Withdrawal Requests</li>
     *                      <li>4: Process Application</li>
     *                  </ul>
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        switch (selection) {
            case 0:
                renderChoice();
                break;
            case 1:
                viewAllApplications();
                break;
            case 2:
                viewPendingApplications();
                break;
            case 3:
                viewWithdrawalRequests();
                break;
            case 4:
                processApplication();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                delay(2);
                renderApp(0);
        }
    }

    /**
     * Renders the main choice menu for application-related operations.
     */
    @Override
    public void renderChoice() {
        printBorder("Application Management");
        System.out.println("Welcome, " + currentUser.getUserID());
        System.out.println("Select an option:");
        System.out.println("(1) View All Applications");
        System.out.println("(2) View Pending Applications");
        System.out.println("(3) View Withdrawal Requests");
        System.out.println("(4) Process Application");
        System.out.println("(0) Exit");
    }

    /**
     * View all applications in the system.
     */
    private void viewAllApplications() {
        // First get the projects available to the current user
        List<Project> userProjects = projectController.getAvailableProjects(currentUser);
        
        // Get all applications for these projects
        List<Application> applications = new ArrayList<>();
        for (Project project : userProjects) {
            applications.addAll(project.getApplications());
        }
        
        displayApplications(applications, "All Applications");
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * View pending applications.
     */
    private void viewPendingApplications() {
        // Get current user's projects first
        List<Project> userProjects = projectController.getAvailableProjects(currentUser);
        
        // Use the projects to get pending applications
        List<Application> pendingApplications = applicationController.getPendingApplications(userProjects);
        
        displayApplications(pendingApplications, "Pending Applications");
        
        pressEnterToContinue();
        renderApp(0);
    }
    /**
     * View applications with withdrawal requests.
     */
    private void viewWithdrawalRequests() {
        List<Project> userProjects = projectController.getAvailableProjects(currentUser);
        
        List<Application> withdrawalRequests = applicationController.getWithdrawalRequests(userProjects);
        
        displayApplications(withdrawalRequests, "Withdrawal Requests");
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * Process an application.
     */
    private void processApplication() {
        String applicantID = getInputString("Enter Applicant ID: ");
        
        System.out.println("Process Options:");
        System.out.println("(1) Approve");
        System.out.println("(2) Reject");
        
        int choice = getInputInt("Enter your choice: ", 2);
        
        boolean result = applicationController.processApplication(
            applicantID, 
            choice == 1
        );
        
        System.out.println(result ? 
            "Application processed successfully" : 
            "Failed to process application"
        );
        
        pressEnterToContinue();
        renderApp(0);
    }

    /**
     * Display a list of applications.
     * 
     * @param applications List of applications to display
     * @param title Title for the applications list
     */
    private void displayApplications(List<Application> applications, String title) {
        if (applications.isEmpty()) {
            System.out.println("No " + title.toLowerCase() + " found.");
            return;
        }
        
        System.out.println(title + ":");
        for (Application app : applications) {
            System.out.printf("Applicant: %s, Project: %s, Status: %s\n", 
                app.getApplicant().getUserID(), 
                app.getProject().getProjectName(), 
                app.getStatus()
            );
        }
    }
}