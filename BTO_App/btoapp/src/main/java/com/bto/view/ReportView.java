package com.bto.view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.bto.controller.ReportController;
import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.Project;
import com.bto.model.Report;
import com.bto.model.User;

/**
 * View class for generating and viewing reports in the BTO Management System.
 * Allows HDB Managers to generate various reports and view statistics.
 */
public class ReportView {
    private Scanner scanner;
    private ReportController reportController;
    private User currentUser;
    
    /**
     * Constructor for ReportView.
     *
     * @param scanner Scanner for user input
     * @param reportController Controller for generating reports
     * @param currentUser The currently logged in user
     */
    public ReportView(Scanner scanner, ReportController reportController, User currentUser) {
        this.scanner = scanner;
        this.reportController = reportController;
        this.currentUser = currentUser;
    }
    
    /**
     * Display the main report menu.
     */
    public void displayReportMenu() {
        // Check if user is authorized to access reports
        if (!reportController.isAuthorizedForReports(currentUser)) {
            System.out.println("You are not authorized to access the reporting features.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== Report Generation ===");
            System.out.println("1. Generate Complete Booking Report");
            System.out.println("2. Generate Filtered Report");
            System.out.println("3. Generate Statistical Report");
            System.out.println("4. View Previous Reports");
            System.out.println("5. Back to Main Menu");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    generateCompleteReport(manager);
                    break;
                case "2":
                    generateFilteredReport(manager);
                    break;
                case "3":
                    generateStatisticalReport(manager);
                    break;
                case "4":
                    viewPreviousReports(manager);
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
     * Generate a complete report of all booked flats.
     * 
     * @param manager The HDB Manager generating the report
     */
    private void generateCompleteReport(HDBManager manager) {
        System.out.println("\nGenerating complete booking report...");
        
        Report report = reportController.generateBookingReport(manager);
        
        if (report != null && !report.getApplications().isEmpty()) {
            System.out.println("Report generated successfully with " + 
                              report.getApplications().size() + " bookings.");
            displayReportDetails(report);
        } else {
            System.out.println("No booked flats found or report generation failed.");
        }
    }
    
    /**
     * Generate a filtered report based on user input criteria.
     * 
     * @param manager The HDB Manager generating the report
     */
    private void generateFilteredReport(HDBManager manager) {
        System.out.println("\n=== Generate Filtered Report ===");
        
        // Get project filter
        List<Project> projects = reportController.getManagerProjects(manager);
        System.out.println("Available Projects:");
        System.out.println("0. All Projects");
        for (int i = 0; i < projects.size(); i++) {
            System.out.println((i + 1) + ". " + projects.get(i).getProjectName());
        }
        
        System.out.print("Select project (enter number, 0 for all): ");
        int projectChoice;
        try {
            projectChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            projectChoice = 0;
        }
        
        String projectName = null;
        if (projectChoice > 0 && projectChoice <= projects.size()) {
            projectName = projects.get(projectChoice - 1).getProjectName();
        }
        
        // Get flat type filter
        System.out.println("\nSelect Flat Type:");
        System.out.println("1. All Types");
        System.out.println("2. 2-Room");
        System.out.println("3. 3-Room");
        
        System.out.print("Enter choice: ");
        int flatTypeChoice;
        try {
            flatTypeChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            flatTypeChoice = 1;
        }
        
        String flatType = null;
        if (flatTypeChoice == 2) {
            flatType = "2-Room";
        } else if (flatTypeChoice == 3) {
            flatType = "3-Room";
        }
        
        // Get marital status filter
        System.out.println("\nSelect Marital Status:");
        System.out.println("1. All");
        System.out.println("2. Married");
        System.out.println("3. Single");
        
        System.out.print("Enter choice: ");
        int maritalChoice;
        try {
            maritalChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            maritalChoice = 1;
        }
        
        Boolean maritalStatus = null;
        if (maritalChoice == 2) {
            maritalStatus = true;
        } else if (maritalChoice == 3) {
            maritalStatus = false;
        }
        
        // Get age range filter
        System.out.print("\nMinimum age (0 for no minimum): ");
        int minAge;
        try {
            minAge = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            minAge = 0;
        }
        
        System.out.print("Maximum age (0 for no maximum): ");
        int maxAge;
        try {
            maxAge = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            maxAge = 0;
        }
        
        // Generate the report
        System.out.println("\nGenerating filtered report...");
        Report report = reportController.generateFilteredReport(manager, projectName, 
                                                               flatType, maritalStatus, 
                                                               minAge, maxAge);
        
        if (report != null && !report.getApplications().isEmpty()) {
            System.out.println("Report generated successfully with " + 
                              report.getApplications().size() + " bookings.");
            displayReportDetails(report);
        } else {
            System.out.println("No matching bookings found or report generation failed.");
        }
    }
    
    /**
     * Generate a statistical report with distribution analysis.
     * 
     * @param manager The HDB Manager generating the report
     */
    private void generateStatisticalReport(HDBManager manager) {
        System.out.println("\nGenerating statistical report...");
        
        Report report = reportController.generateStatisticalReport(manager);
        
        if (report != null) {
            System.out.println("Statistical report generated successfully.");
            System.out.println("\n=== " + report.getTitle() + " ===");
            System.out.println("Generated by: " + report.getManager().getName());
            System.out.println("Date: " + report.getGenerationDate());
            System.out.println("\nTotal Bookings: " + report.getApplications().size());
            
            // Display statistics
            Map<String, Map<String, Integer>> statistics = report.getStatistics();
            for (String category : statistics.keySet()) {
                System.out.println("\n" + category + ":");
                Map<String, Integer> statData = statistics.get(category);
                
                if (statData.isEmpty()) {
                    System.out.println("  No data available");
                } else {
                    int total = 0;
                    for (String key : statData.keySet()) {
                        int count = statData.get(key);
                        total += count;
                        System.out.println("  " + key + ": " + count);
                    }
                    System.out.println("  Total: " + total);
                }
            }
            
            // Optionally display booking details
            System.out.print("\nWould you like to see all booking details? (y/n): ");
            String viewDetails = scanner.nextLine().trim().toLowerCase();
            if (viewDetails.equals("y")) {
                displayReportDetails(report);
            }
        } else {
            System.out.println("No booking data available or report generation failed.");
        }
    }
    
    /**
     * View previously generated reports.
     * 
     * @param manager The HDB Manager viewing the reports
     */
    private void viewPreviousReports(HDBManager manager) {
        List<Report> reports = reportController.getManagerReports(manager);
        
        if (reports.isEmpty()) {
            System.out.println("No previous reports found.");
            return;
        }
        
        System.out.println("\n=== Previous Reports ===");
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            System.out.println((i + 1) + ". " + report.getTitle() + 
                              " - " + report.getGenerationDate() + 
                              " (" + report.getApplications().size() + " entries)");
        }
        
        System.out.print("\nSelect a report to view (enter number, 0 to cancel): ");
        int selection;
        try {
            selection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            selection = 0;
        }
        
        if (selection > 0 && selection <= reports.size()) {
            Report selectedReport = reports.get(selection - 1);
            System.out.println("\n=== " + selectedReport.getTitle() + " ===");
            System.out.println("Generated by: " + selectedReport.getManager().getName());
            System.out.println("Date: " + selectedReport.getGenerationDate());
            
            // Display statistics if available
            Map<String, Map<String, Integer>> statistics = selectedReport.getStatistics();
            if (!statistics.isEmpty()) {
                System.out.println("\nStatistics:");
                for (String category : statistics.keySet()) {
                    System.out.println("\n" + category + ":");
                    Map<String, Integer> statData = statistics.get(category);
                    
                    if (statData.isEmpty()) {
                        System.out.println("  No data available");
                    } else {
                        int total = 0;
                        for (String key : statData.keySet()) {
                            int count = statData.get(key);
                            total += count;
                            System.out.println("  " + key + ": " + count);
                        }
                        System.out.println("  Total: " + total);
                    }
                }
            }
            
            // Display booking details
            displayReportDetails(selectedReport);
        } else if (selection != 0) {
            System.out.println("Invalid selection.");
        }
    }
    
    /**
     * Display detailed information for each application in a report.
     * 
     * @param report The report to display details for
     */
    private void displayReportDetails(Report report) {
        List<Application> applications = report.getApplications();
        
        System.out.println("\n=== Booking Details ===");
        System.out.println("Total entries: " + applications.size());
        
        if (applications.isEmpty()) {
            System.out.println("No booking data available.");
            return;
        }
        
        System.out.println("\nFormat: Project - Flat Type - Applicant Name (NRIC) - Age - Marital Status");
        System.out.println("-----------------------------------------------------------------------");
        
        for (Application app : applications) {
            Applicant applicant = (Applicant) app.getApplicant();
            System.out.println(app.getProject().getProjectName() + " - " + 
                              app.getFlatType() + " - " + 
                              applicant.getName() + " (" + applicant.getUserID() + ") - " + 
                              applicant.getAge() + " - " + 
                              (applicant.isMarried() ? "Married" : "Single"));
        }
        
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}