package view;

import controller.ProjectController;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import model.HDBManager;
import model.Project;
import model.Report;
import model.enums.FlatType;
import view.abstracts.ARenderView;
import view.interfaces.ViewInterface;

/**
 * View for report generation and viewing in the BTO Management System.
 */
public class ReportView extends ARenderView implements ViewInterface{
    
    private HDBManager manager;
    private ProjectController projectController;
    private Scanner scanner;
    
    /**
     * Constructor for ReportView.
     * 
     * @param manager The HDB manager
     * @param projectController Controller for project operations
     */
    public ReportView(HDBManager manager, ProjectController projectController) {
        this.manager = manager;
        this.projectController = projectController;
        this.scanner = new Scanner(System.in);
    }
 
    public void display() {
        printHeader("REPORT GENERATION");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You are not managing any projects.");
            return;
        }
        
        System.out.println("Select a project to generate a report for:");
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
        
        printHeader("GENERATE REPORT FOR: " + selectedProject.getProjectName());
        
        System.out.println("Select report type:");
        System.out.println("1. Project Booking Report (All Bookings)");
        System.out.println("2. Filter by Marital Status");
        System.out.println("3. Filter by Flat Type");
        System.out.println("4. Filter by Age Range");
        
        System.out.print("\nEnter selection (1-4): ");
        int reportTypeChoice = getIntInput();
        
        Report report = null;
        
        switch (reportTypeChoice) {
            case 1:
                report = generateProjectBookingReport(selectedProject);
                break;
            case 2:
                report = generateMaritalStatusReport(selectedProject);
                break;
            case 3:
                report = generateFlatTypeReport(selectedProject);
                break;
            case 4:
                report = generateAgeRangeReport(selectedProject);
                break;
            default:
                showError("Invalid selection.");
                return;
        }
        
        if (report != null) {
            displayReport(report);
        } else {
            showError("Failed to generate report. Please try again later.");
        }
    }
    
    /**
     * Generates a project booking report.
     * 
     * @param project The project to generate the report for
     * @return The generated report
     */
    private Report generateProjectBookingReport(Project project) {
        return manager.generateReport(project, "Project Booking Report");
    }
    
    /**
     * Generates a report filtered by marital status.
     * 
     * @param project The project to generate the report for
     * @return The generated report
     */
    private Report generateMaritalStatusReport(Project project) {
        System.out.println("\nSelect marital status to filter by:");
        System.out.println("1. Single");
        System.out.println("2. Married");
        
        System.out.print("\nEnter selection (1-2): ");
        int statusChoice = getIntInput();
        
        String maritalStatus;
        if (statusChoice == 1) {
            maritalStatus = "Single";
        } else if (statusChoice == 2) {
            maritalStatus = "Married";
        } else {
            showError("Invalid selection.");
            return null;
        }
        
        Report baseReport = generateProjectBookingReport(project);
        if (baseReport != null) {
            return baseReport.filterByMaritalStatus(maritalStatus);
        }
        
        return null;
    }
    
    /**
     * Generates a report filtered by flat type.
     * 
     * @param project The project to generate the report for
     * @return The generated report
     */
    private Report generateFlatTypeReport(Project project) {
        System.out.println("\nSelect flat type to filter by:");
        System.out.println("1. 2-Room");
        System.out.println("2. 3-Room");
        
        System.out.print("\nEnter selection (1-2): ");
        int typeChoice = getIntInput();
        
        FlatType flatType;
        if (typeChoice == 1) {
            flatType = FlatType.TWO_ROOM;
        } else if (typeChoice == 2) {
            flatType = FlatType.THREE_ROOM;
        } else {
            showError("Invalid selection.");
            return null;
        }
        
        Report baseReport = generateProjectBookingReport(project);
        if (baseReport != null) {
            return baseReport.filterByFlatType(flatType);
        }
        
        return null;
    }
    
    /**
     * Generates a report filtered by age range.
     * 
     * @param project The project to generate the report for
     * @return The generated report
     */
    private Report generateAgeRangeReport(Project project) {
        System.out.print("\nEnter minimum age: ");
        int minAge = getIntInput();
        
        System.out.print("Enter maximum age: ");
        int maxAge = getIntInput();
        
        if (minAge < 21 || maxAge < minAge) {
            showError("Invalid age range. Minimum age must be at least 21 and maximum age must be greater than or equal to minimum age.");
            return null;
        }
        
        Report baseReport = generateProjectBookingReport(project);
        if (baseReport != null) {
            return baseReport.filterByAgeRange(minAge, maxAge);
        }
        
        return null;
    }
    
    /**
     * Displays a report.
     * 
     * @param report The report to display
     */
    private void displayReport(Report report) {
        printHeader("REPORT: " + report.getReportTitle());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("Project: " + report.getProject().getProjectName());
        System.out.println("Generation Date: " + dateFormat.format(report.getGenerationDate()));
        System.out.println("Total Entries: " + report.getEntries().size());
        
        System.out.println("\nEntries:");
        System.out.println("Name | NRIC | Age | Marital Status | Flat Type");
        System.out.println("-------------------------------------------");
        
        for (Report.ReportEntry entry : report.getEntries()) {
            System.out.printf("%-20s | %-12s | %3d | %-14s | %s\n",
                             entry.getApplicantName(),
                             entry.getApplicantNric(),
                             entry.getApplicantAge(),
                             entry.getMaritalStatus(),
                             entry.getFlatType().getDisplayName());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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