package com.bto.view;

import java.util.List;
import java.util.Map;

import com.bto.controller.ReportController;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.Report;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * ReportView displays reports for HDB Managers.
 * 
 * This class extends the abstract base view class {@link ARenderView}
 */
public class ReportView extends ARenderView {
    private ReportController reportController;
    private User currentUser;
    
    /**
     * Constructor for ReportView.
     *
     * @param reportController Controller for generating reports
     * @param currentUser The currently logged in user
     */
    public ReportView(ReportController reportController, User currentUser) {
        this.reportController = reportController;
        this.currentUser = currentUser;
    }
    
    /**
     * Renders the application based on the user's selection.
     * 
     * @param selection The selected menu option
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        
        // Check if user is authorized to access reports
        if (!reportController.isAuthorizedForReports(currentUser)) {
            System.out.println("You are not authorized to access the reporting features.");
            pressEnterToContinue();
            return;
        }
        
        switch (selection) {
            case 0:
                renderChoice();
                break;
            case 1:
                generateCompleteReport();
                break;
            case 2:
                generateFilteredReport();
                break;
            case 3:
                return; // Return to previous menu
            default:
                System.out.println("Invalid option. Please try again.");
                delay(1);
                renderApp(0);
        }
    }
    
    /**
     * Renders the main choice menu for report generation.
     */
    @Override
    public void renderChoice() {
        printBorder("Report Generation");
        System.out.println("Select an option:");
        System.out.println("(1) Generate Complete Booking Report");
        System.out.println("(2) Generate Filtered Report");
        System.out.println("(3) Back to Main Menu");
        
        int choice = getInputInt("Enter your choice: ", 3);
        renderApp(choice);
    }
    
    /**
     * Generate a complete report of all booked flats.
     */
    private void generateCompleteReport() {
        printSingleBorder("Complete Booking Report");
        System.out.println("Generating complete booking report...");
        
        Report report = reportController.generateBookingReport((HDBManager) currentUser);
        
        if (report != null && !report.getApplications().isEmpty()) {
            displayReport(report);
        } else {
            System.out.println("No booked flats found or report generation failed.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Generate a filtered report based on user input criteria.
     */
    private void generateFilteredReport() {
        printSingleBorder("Filtered Report");
        
        // Get project filter
        String projectName = getInputString("Project Name (leave blank for all): ");
        projectName = projectName.trim().isEmpty() ? null : projectName.trim();
        
        // Get flat type filter
        System.out.println("\nSelect Flat Type:");
        System.out.println("1. All Types");
        System.out.println("2. 2-Room");
        System.out.println("3. 3-Room");
        int flatTypeChoice = getInputInt("Enter choice: ", 3);
        String flatType = (flatTypeChoice == 2) ? "2-Room" : (flatTypeChoice == 3) ? "3-Room" : null;
        
        // Get marital status filter
        System.out.println("\nSelect Marital Status:");
        System.out.println("1. All");
        System.out.println("2. Married");
        System.out.println("3. Single");
        int maritalChoice = getInputInt("Enter choice: ", 3);
        String maritalStatus = (maritalChoice == 2) ? "Married" : (maritalChoice == 3) ? "Single" : null;
        
        // Get age range filter
        int minAge = getInputInt("\nMinimum age (0 for no minimum): ");
        minAge = (minAge <= 0) ? 0 : minAge;
        
        int maxAge = getInputInt("Maximum age (0 for no maximum): ");
        maxAge = (maxAge <= 0) ? 0 : maxAge;
        
        // Create filters map
        Map<String, Object> filters = reportController.createFilterMap(
            projectName, flatType, maritalStatus, minAge, maxAge);
        
        // Generate the report
        System.out.println("\nGenerating filtered report...");
        Report report = reportController.generateFilteredReport((HDBManager) currentUser, filters);
        
        if (report != null && !report.getApplications().isEmpty()) {
            displayReport(report);
        } else {
            System.out.println("No matching bookings found or report generation failed.");
        }
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Display a formatted report.
     * 
     * @param report The report to display
     */
    private void displayReport(Report report) {
        List<Application> applications = report.getApplications();
        
        printDoubleUnderline(report.getTitle());
        System.out.println("Total entries: " + applications.size());
        
        System.out.println("\nFormat: Project | Flat Type | Applicant ID | Age | Marital Status");
        System.out.println("-----------------------------------------------------------------------");
        
        for (Application app : applications) {
            System.out.printf("%-15s | %-8s | %-12s | %-3d | %-10s\n",
                app.getProject().getProjectName(),
                app.getFlatTypeBooked(),
                app.getApplicant().getUserID(),
                app.getApplicant().getAge(),
                app.getApplicant().getMaritalStatus());
        }
    }
}