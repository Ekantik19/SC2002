package com.bto.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bto.controller.ProjectController;
import com.bto.model.Application;
import com.bto.model.Enquiry;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * View class for displaying project information in the BTO Management System.
 * This class extends the abstract base view class {@link ARenderView}
 */
public class ProjectView extends ARenderView {
    private ProjectController projectController;
    private User currentUser;
    
    /**
     * Constructs a new ProjectView with the specified controller and user.
     * 
     * @param projectController The controller for project operations
     * @param currentUser The currently logged in user
     */
    public ProjectView(ProjectController projectController, User currentUser) {
        super();
        this.projectController = projectController;
        this.currentUser = currentUser;
    }
    
    /**
     * Renders the project view based on the selection.
     * 
     * @param selection The view to render: 
     *                  0 - Project listing
     *                  1 - Project details
     *                  2 - Project applications
     *                  3 - Project enquiries
     *                  4 - Filtered projects
     */
    @Override
    public void renderApp(int selection) {
        switch (selection) {
            case 0:
                // Project listing
                renderChoice();
                displayProjectListing();
                break;
                
            case 1:
                // Project details view
                renderChoice();
                viewProjectDetails();
                break;
                
            case 2:
                // View project applications
                renderChoice();
                viewProjectApplications();
                break;
                
            case 3:
                // View project enquiries
                renderChoice();
                viewProjectEnquiries();
                break;
                
            case 4:
                // Filtered projects
                renderChoice();
                displayFilteredProjects();
                break;
                
            default:
                System.out.println("Invalid selection");
                delay(1);
                renderApp(0);
                break;
        }
    }
    
    /**
     * Renders the header for the project view.
     */
    @Override
    public void renderChoice() {
        printBTOHeader("Project Management");
    }
    
    /**
     * Displays a listing of all available projects based on user permissions.
     */
    private void displayProjectListing() {
        List<Project> projects = projectController.getAvailableProjects(currentUser);
        
        if (projects.isEmpty()) {
            System.out.println("No projects available to view.");
            pressEnterToContinue();
            return;
        }
        
        printSingleBorder("Available Projects");
        System.out.println("┌─────┬──────────────────┬──────────────────┬─────────────┬────────────┬─────────┐");
        System.out.println("│ ID  │ Project Name     │ Neighborhood     │ Status      │ Flat Types │ Visible │");
        System.out.println("├─────┼──────────────────┼──────────────────┼─────────────┼────────────┼─────────┤");
        
        for (Project project : projects) {
            String status = getProjectStatus(project);
            String flatTypes = formatFlatTypes(project.getFlatTypes());
            String visibility = project.isVisible() ? "Yes" : "No";
            
            System.out.printf("│ %-3d │ %-16s │ %-16s │ %-11s │ %-10s │ %-7s │\n", 
                             project.getProjectID(),
                             truncateString(project.getProjectName(), 16),
                             truncateString(project.getNeighborhood(), 16),
                             status,
                             flatTypes,
                             visibility);
        }
        
        System.out.println("└─────┴──────────────────┴──────────────────┴─────────────┴────────────┴─────────┘");
        
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Filter Projects");
        System.out.println("3. Return to Main Menu");
        
        int choice = getInputInt("Enter your choice: ", 3);
        
        switch (choice) {
            case 1:
                int projectId = getInputInt("Enter project ID to view: ");
                viewSpecificProject(projectId, projects);
                break;
                
            case 2:
                renderApp(4);
                break;
                
            case 3:
                // Return to calling menu
                return;
                
            default:
                System.out.println("Invalid choice. Returning to project listing.");
                delay(1);
                renderApp(0);
                break;
        }
    }
    
    /**
     * Displays projects filtered by various criteria.
     */
    private void displayFilteredProjects() {
        printSingleBorder("Filter Projects");
        
        System.out.println("Filter by:");
        System.out.println("1. Neighborhood");
        System.out.println("2. Flat Type");
        System.out.println("3. Status (Open/Closed)");
        System.out.println("4. Return to Project Listing");
        
        int choice = getInputInt("Enter your choice: ", 4);
        
        if (choice == 4) {
            renderApp(0);
            return;
        }
        
        List<Project> filteredProjects;
        
        switch (choice) {
            case 1:
                String neighborhood = getInputString("Enter neighborhood: ");
                filteredProjects = projectController.getProjectsByNeighborhood(neighborhood, currentUser);
                break;
                
            case 2:
                System.out.println("Select flat type:");
                System.out.println("1. 2-Room");
                System.out.println("2. 3-Room");
                int flatTypeChoice = getInputInt("Enter your choice: ", 2);
                String flatType = (flatTypeChoice == 1) ? "2-Room" : "3-Room";
                filteredProjects = projectController.getProjectsByFlatType(flatType, currentUser);
                break;
                
            case 3:
                System.out.println("Select status:");
                System.out.println("1. Open");
                System.out.println("2. Upcoming");
                System.out.println("3. Closed");
                int statusChoice = getInputInt("Enter your choice: ", 3);
                boolean isOpen = (statusChoice == 1);
                boolean isUpcoming = (statusChoice == 2);
                filteredProjects = projectController.getProjectsByStatus(isOpen, isUpcoming, currentUser);
                break;
                
            default:
                System.out.println("Invalid choice. Returning to filter options.");
                delay(1);
                renderApp(4);
                return;
        }
        
        if (filteredProjects.isEmpty()) {
            System.out.println("No projects match your filter criteria.");
            pressEnterToContinue();
            renderApp(4);
            return;
        }
        
        // Display filtered projects
        printSingleBorder("Filtered Projects");
        System.out.println("┌─────┬──────────────────┬──────────────────┬─────────────┬────────────┬─────────┐");
        System.out.println("│ ID  │ Project Name     │ Neighborhood     │ Status      │ Flat Types │ Visible │");
        System.out.println("├─────┼──────────────────┼──────────────────┼─────────────┼────────────┼─────────┤");
        
        for (Project project : filteredProjects) {
            String status = getProjectStatus(project);
            String flatTypes = formatFlatTypes(project.getFlatTypes());
            String visibility = project.isVisible() ? "Yes" : "No";
            
            System.out.printf("│ %-3d │ %-16s │ %-16s │ %-11s │ %-10s │ %-7s │\n", 
                             project.getProjectID(),
                             truncateString(project.getProjectName(), 16),
                             truncateString(project.getNeighborhood(), 16),
                             status,
                             flatTypes,
                             visibility);
        }
        
        System.out.println("└─────┴──────────────────┴──────────────────┴─────────────┴────────────┴─────────┘");
        
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Apply Different Filter");
        System.out.println("3. Return to Project Listing");
        
        choice = getInputInt("Enter your choice: ", 3);
        
        switch (choice) {
            case 1:
                int projectId = getInputInt("Enter project ID to view: ");
                viewSpecificProject(projectId, filteredProjects);
                break;
                
            case 2:
                renderApp(4);
                break;
                
            case 3:
                renderApp(0);
                break;
                
            default:
                System.out.println("Invalid choice. Returning to filtered projects.");
                delay(1);
                displayFilteredProjects();
                break;
        }
    }
    
    /**
     * Views details for a specific project.
     * 
     * @param projectId The ID of the project to view
     * @param availableProjects The list of projects available to the user
     */
    private void viewSpecificProject(int projectId, List<Project> availableProjects) {
        Project selectedProject = null;
        
        for (Project project : availableProjects) {
            if (project.getProjectID() == projectId) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            delay(2);
            renderApp(0);
            return;
        }
        
        viewProjectDetails(selectedProject);
    }
    
    /**
     * Prompts for a project ID and then displays its details.
     */
    private void viewProjectDetails() {
        List<Project> projects = projectController.getAvailableProjects(currentUser);
        
        if (projects.isEmpty()) {
            System.out.println("No projects available to view.");
            pressEnterToContinue();
            return;
        }
        
        printSingleBorder("Enter Project ID to View Details");
        
        for (Project project : projects) {
            System.out.printf("%d. %s (%s)\n", 
                              project.getProjectID(),
                              project.getProjectName(),
                              project.getNeighborhood());
        }
        
        int projectId = getInputInt("Enter Project ID (0 to cancel): ");
        
        if (projectId == 0) {
            renderApp(0);
            return;
        }
        
        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectID() == projectId) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            delay(2);
            viewProjectDetails();
            return;
        }
        
        viewProjectDetails(selectedProject);
    }
    
    /**
     * Displays detailed information for a specific project.
     * 
     * @param project The project to display details for
     */
    private void viewProjectDetails(Project project) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        printSingleBorder("Project Details: " + project.getProjectName());
        
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Status: " + getProjectStatus(project));
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Application Period: " + dateFormat.format(project.getOpeningDate()) + 
                           " to " + dateFormat.format(project.getClosingDate()));
        
        System.out.println("\nFlat Types Available:");
        Map<String, Integer> flatTypes = project.getFlatTypes();
        for (String type : flatTypes.keySet()) {
            System.out.println("  " + type + ": " + flatTypes.get(type) + " units");
        }
        
        System.out.println("\nProject Manager: " + project.getManagerInCharge().getName());
        
        List<HDBOfficer> officers = project.getAssignedOfficers();
        System.out.println("\nAssigned HDB Officers (" + officers.size() + "/" + 
                          (officers.size() + project.getRemainingOfficerSlots()) + "):");
        if (officers.isEmpty()) {
            System.out.println("  No officers assigned yet");
        } else {
            for (HDBOfficer officer : officers) {
                System.out.println("  " + officer.getName() + " (" + officer.getUserID() + ")");
            }
        }
        
        System.out.println("\nApplications: " + project.getApplications().size());
        System.out.println("Enquiries: " + project.getEnquiries().size());
        
        System.out.println("\nOptions:");
        System.out.println("1. View Applications");
        System.out.println("2. View Enquiries");
        System.out.println("3. Return to Project Listing");
        
        int choice = getInputInt("Enter your choice: ", 3);
        
        switch (choice) {
            case 1:
                viewProjectApplications(project);
                break;
                
            case 2:
                viewProjectEnquiries(project);
                break;
                
            case 3:
                renderApp(0);
                break;
                
            default:
                System.out.println("Invalid choice. Returning to project details.");
                delay(1);
                viewProjectDetails(project);
                break;
        }
    }
    
    /**
     * Prompts for a project ID and then displays its applications.
     */
    private void viewProjectApplications() {
        List<Project> projects = projectController.getAvailableProjects(currentUser);
        
        if (projects.isEmpty()) {
            System.out.println("No projects available to view applications for.");
            pressEnterToContinue();
            return;
        }
        
        printSingleBorder("Enter Project ID to View Applications");
        
        for (Project project : projects) {
            System.out.printf("%d. %s (%s) - %d applications\n", 
                              project.getProjectID(),
                              project.getProjectName(),
                              project.getNeighborhood(),
                              project.getApplications().size());
        }
        
        int projectId = getInputInt("Enter Project ID (0 to cancel): ");
        
        if (projectId == 0) {
            renderApp(0);
            return;
        }
        
        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectID() == projectId) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            delay(2);
            viewProjectApplications();
            return;
        }
        
        viewProjectApplications(selectedProject);
    }
    
    /**
     * Displays applications for a specific project.
     * 
     * @param project The project to display applications for
     */
    private void viewProjectApplications(Project project) {
        List<Application> applications = project.getApplications();
        
        if (applications.isEmpty()) {
            System.out.println("No applications for this project yet.");
            pressEnterToContinue();
            viewProjectDetails(project);
            return;
        }
        
        printSingleBorder("Applications for " + project.getProjectName());
        
        // Count applications by status
        int pending = 0, successful = 0, unsuccessful = 0, booked = 0;
        for (Application app : applications) {
            switch (app.getStatus()) {
                case "PENDING":
                    pending++;
                    break;
                case "SUCCESSFUL":
                    successful++;
                    break;
                case "UNSUCCESSFUL":
                    unsuccessful++;
                    break;
                case "BOOKED":
                    booked++;
                    break;
            }
        }
        
        System.out.println("Total Applications: " + applications.size());
        System.out.println("Status Breakdown:");
        System.out.println("  Pending: " + pending);
        System.out.println("  Successful: " + successful);
        System.out.println("  Unsuccessful: " + unsuccessful);
        System.out.println("  Booked: " + booked);
        
        System.out.println("\nApplication List:");
        System.out.println("┌─────┬──────────────────┬─────────────┬────────────┬──────────────┐");
        System.out.println("│ ID  │ Applicant        │ NRIC        │ Status     │ Flat Type    │");
        System.out.println("├─────┼──────────────────┼─────────────┼────────────┼──────────────┤");
        
        for (Application app : applications) {
            System.out.printf("│ %-3d │ %-16s │ %-11s │ %-10s │ %-12s │\n", 
                             app.getApplicationID(),
                             truncateString(app.getApplicant().getName(), 16),
                             app.getApplicant().getUserID(),
                             app.getStatus(),
                             app.getFlatTypeBooked() != null ? app.getFlatTypeBooked() : "Not booked");
        }
        
        System.out.println("└─────┴──────────────────┴─────────────┴────────────┴──────────────┘");
        
        pressEnterToContinue();
        viewProjectDetails(project);
    }
    
    /**
     * Prompts for a project ID and then displays its enquiries.
     */
    private void viewProjectEnquiries() {
        List<Project> projects = projectController.getAvailableProjects(currentUser);
        
        if (projects.isEmpty()) {
            System.out.println("No projects available to view enquiries for.");
            pressEnterToContinue();
            return;
        }
        
        printSingleBorder("Enter Project ID to View Enquiries");
        
        for (Project project : projects) {
            System.out.printf("%d. %s (%s) - %d enquiries\n", 
                              project.getProjectID(),
                              project.getProjectName(),
                              project.getNeighborhood(),
                              project.getEnquiries().size());
        }
        
        int projectId = getInputInt("Enter Project ID (0 to cancel): ");
        
        if (projectId == 0) {
            renderApp(0);
            return;
        }
        
        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectID() == projectId) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            delay(2);
            viewProjectEnquiries();
            return;
        }
        
        viewProjectEnquiries(selectedProject);
    }
    
    /**
     * Displays enquiries for a specific project.
     * 
     * @param project The project to display enquiries for
     */
    private void viewProjectEnquiries(Project project) {
        List<Enquiry> enquiries = project.getEnquiries();
        
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for this project yet.");
            pressEnterToContinue();
            viewProjectDetails(project);
            return;
        }
        
        printSingleBorder("Enquiries for " + project.getProjectName());
        
        // Count enquiries by response status
        int pending = 0, responded = 0;
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getResponse() == null) {
                pending++;
            } else {
                responded++;
            }
        }
        
        System.out.println("Total Enquiries: " + enquiries.size());
        System.out.println("Status Breakdown:");
        System.out.println("  Pending: " + pending);
        System.out.println("  Responded: " + responded);
        
        System.out.println("\nEnquiry List:");
        System.out.println("┌─────┬──────────────────┬─────────────┬───────────┬────────────────────────────┐");
        System.out.println("│ ID  │ From             │ Date        │ Status    │ Enquiry                    │");
        System.out.println("├─────┼──────────────────┼─────────────┼───────────┼────────────────────────────┤");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        for (Enquiry enquiry : enquiries) {
            String status = enquiry.getResponse() == null ? "Pending" : "Responded";
            String enquiryText = truncateString(enquiry.getEnquiryText(), 24);
            
            System.out.printf("│ %-3d │ %-16s │ %-11s │ %-9s │ %-26s │\n", 
                             enquiry.getEnquiryID(),
                             truncateString(enquiry.getUser().getName(), 16),
                             dateFormat.format(enquiry.getCreationDate()),
                             status,
                             enquiryText);
        }
        
        System.out.println("└─────┴──────────────────┴─────────────┴───────────┴────────────────────────────┘");
        
        pressEnterToContinue();
        viewProjectDetails(project);
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
     * Formats flat types into a compact string representation.
     * 
     * @param flatTypes The map of flat types and their counts
     * @return A formatted string representation
     */
    private String formatFlatTypes(Map<String, Integer> flatTypes) {
        StringBuilder sb = new StringBuilder();
        
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