package com.bto.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.bto.controller.ApplicationController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.controller.ReportController;
import com.bto.model.Application;
import com.bto.model.Enquiry;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Report;

/**
 * View for the HDB Manager interface.
 */
public class ManagerView {
    private Scanner scanner;
    private HDBManager manager;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private ReportController reportController;
    
    /**
     * Constructor for ManagerView with dependency injection.
     * 
     * @param manager The HDB Manager user
     * @param applicationController The application controller
     * @param projectController The project controller
     * @param enquiryController The enquiry controller
     * @param reportController The report controller
     */
    public ManagerView(HDBManager manager, 
                    ApplicationController applicationController,
                    ProjectController projectController,
                    EnquiryController enquiryController,
                    ReportController reportController) {
        this.scanner = new Scanner(System.in);
        this.manager = manager;
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.enquiryController = enquiryController;
        this.reportController = reportController;
    }
        
    /**
     * Display the HDB Manager interface.
     */
    public void display() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n==================================");
            System.out.println("    HDB Manager Menu    ");
            System.out.println("==================================");
            System.out.println("1. Create New BTO Project");
            System.out.println("2. View All Projects");
            System.out.println("3. View My Projects");
            System.out.println("4. Edit Project");
            System.out.println("5. Toggle Project Visibility");
            System.out.println("6. Delete Project");
            System.out.println("7. Process Officer Registrations");
            System.out.println("8. Process Applications");
            System.out.println("9. Process Withdrawal Requests");
            System.out.println("10. Generate Reports");
            System.out.println("11. View All Enquiries");
            System.out.println("12. Reply to Enquiries");
            System.out.println("13. Change Password");
            System.out.println("14. Back to Main Menu");
            
            System.out.print("\nEnter choice: ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    createNewProject();
                    break;
                case 2:
                    viewAllProjects();
                    break;
                case 3:
                    viewMyProjects();
                    break;
                case 4:
                    editProject();
                    break;
                case 5:
                    toggleProjectVisibility();
                    break;
                case 6:
                    deleteProject();
                    break;
                case 7:
                    processOfficerRegistrations();
                    break;
                case 8:
                    processApplications();
                    break;
                case 9:
                    processWithdrawalRequests();
                    break;
                case 10:
                    generateReports();
                    break;
                case 11:
                    viewAllEnquiries();
                    break;
                case 12:
                    replyToEnquiries();
                    break;
                case 13:
                    changePassword();
                    break;
                case 14:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    /**
     * Create a new BTO project.
     */
    private void createNewProject() {
        System.out.println("\n==================================");
        System.out.println("    Create New BTO Project    ");
        System.out.println("==================================");
        
        // Get project details
        System.out.println("Enter project name:");
        String projectName = scanner.nextLine();
        
        System.out.println("Enter neighborhood (e.g., Yishun, Boon Lay):");
        String neighborhood = scanner.nextLine();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date openingDate = null;
        Date closingDate = null;
        
        // Get opening date
        while (openingDate == null) {
            System.out.println("Enter application opening date (dd/MM/yyyy):");
            String openingDateStr = scanner.nextLine();
            
            try {
                openingDate = dateFormat.parse(openingDateStr);
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please use dd/MM/yyyy.");
            }
        }
        
        // Get closing date
        while (closingDate == null) {
            System.out.println("Enter application closing date (dd/MM/yyyy):");
            String closingDateStr = scanner.nextLine();
            
            try {
                closingDate = dateFormat.parse(closingDateStr);
                
                // Ensure closing date is after opening date
                if (closingDate.before(openingDate)) {
                    System.out.println("Closing date must be after opening date.");
                    closingDate = null;
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please use dd/MM/yyyy.");
            }
        }
        
        // Get flat types and units
        Map<String, Integer> flatTypes = new HashMap<>();
        
        System.out.println("Enter number of 2-Room flats (0 if none):");
        int twoRoomCount = getIntInput();
        if (twoRoomCount > 0) {
            flatTypes.put("2-Room", twoRoomCount);
        }
        
        System.out.println("Enter number of 3-Room flats (0 if none):");
        int threeRoomCount = getIntInput();
        if (threeRoomCount > 0) {
            flatTypes.put("3-Room", threeRoomCount);
        }
        
        if (flatTypes.isEmpty()) {
            System.out.println("At least one flat type must have units available.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Enter maximum number of HDB Officers for this project (max 10):");
        int maxOfficers = getIntInput();
        if (maxOfficers < 1 || maxOfficers > 10) {
            System.out.println("Number of officers must be between 1 and 10. Setting to default of 5.");
            maxOfficers = 5;
        }
        
        // Create the project
        Project newProject = manager.createProject(projectName, neighborhood, openingDate, closingDate, flatTypes);
        
        if (newProject != null) {
            newProject.setMaxOfficerSlots(maxOfficers);
            newProject.setVisible(false); // Default to hidden until manager toggles visibility
            System.out.println("Project created successfully!");
            System.out.println("Project ID: " + newProject.getProjectID());
        } else {
            System.out.println("Failed to create project. You may already be handling a project during the same period.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View all projects in the system.
     */
    private void viewAllProjects() {
        System.out.println("\n==================================");
        System.out.println("    All Projects    ");
        System.out.println("==================================");
        
        List<Project> allProjects = manager.viewProjects(null);
        
        if (allProjects.isEmpty()) {
            System.out.println("No projects in the system.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nID\tProject Name\tNeighborhood\tManager\tStatus\tVisibility");
        System.out.println("------------------------------------------------------------------");
        
        for (Project project : allProjects) {
            String status = getProjectStatus(project);
            String visibility = project.isVisible() ? "Visible" : "Hidden";
            String managerName = project.getHdbManagerInCharge().getName();
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood(),
                             managerName,
                             status,
                             visibility);
        }
        
        // Allow viewing project details
        System.out.println("\nEnter project ID to view details (0 to return):");
        int projectID = getIntInput();
        
        if (projectID > 0) {
            Project selectedProject = null;
            for (Project project : allProjects) {
                if (project.getProjectID() == projectID) {
                    selectedProject = project;
                    break;
                }
            }
            
            if (selectedProject != null) {
                displayProjectDetails(selectedProject);
            } else {
                System.out.println("Invalid project ID.");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View projects created by this manager.
     */
    private void viewMyProjects() {
        System.out.println("\n==================================");
        System.out.println("    My Projects    ");
        System.out.println("==================================");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects yet.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nID\tProject Name\tNeighborhood\tStatus\tVisibility");
        System.out.println("------------------------------------------------------------------");
        
        for (Project project : myProjects) {
            String status = getProjectStatus(project);
            String visibility = project.isVisible() ? "Visible" : "Hidden";
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood(),
                             status,
                             visibility);
        }
        
        // Allow viewing project details
        System.out.println("\nEnter project ID to view details (0 to return):");
        int projectID = getIntInput();
        
        if (projectID > 0) {
            Project selectedProject = null;
            for (Project project : myProjects) {
                if (project.getProjectID() == projectID) {
                    selectedProject = project;
                    break;
                }
            }
            
            if (selectedProject != null) {
                displayProjectDetails(selectedProject);
            } else {
                System.out.println("Invalid project ID.");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Display detailed information about a project.
     * 
     * @param project The project to display details for
     */
    private void displayProjectDetails(Project project) {
        System.out.println("\n==================================");
        System.out.println("    Project Details    ");
        System.out.println("==================================");
        
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Application Period: " + dateFormat.format(project.getOpeningDate()) + 
                           " to " + dateFormat.format(project.getClosingDate()));
        
        System.out.println("Status: " + getProjectStatus(project));
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Manager in Charge: " + project.getHdbManagerInCharge().getName());
        
        System.out.println("\nFlat Types Available:");
        Map<String, Integer> flatTypes = project.getFlatTypes();
        for (String type : flatTypes.keySet()) {
            System.out.println(type + ": " + flatTypes.get(type) + " units");
        }
        
        System.out.println("\nHDB Officers:");
        List<HDBOfficer> officers = project.getOfficers();
        if (officers.isEmpty()) {
            System.out.println("No officers assigned yet.");
        } else {
            for (HDBOfficer officer : officers) {
                System.out.println(officer.getName() + " (" + officer.getUserID() + ")");
            }
        }
        
        System.out.println("Officer Slots: " + project.getRemainingOfficerSlots() + " available out of " + 
                          project.getMaxOfficerSlots());
        
        List<Application> applications = project.getApplications();
        System.out.println("\nApplications:");
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
    
    /**
     * Edit an existing project.
     */
    private void editProject() {
        System.out.println("\n==================================");
        System.out.println("    Edit Project    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects to edit.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nYour Projects:");
        System.out.println("ID\tProject Name\tNeighborhood");
        System.out.println("----------------------------------");
        
        for (Project project : myProjects) {
            System.out.printf("%d\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.getNeighborhood());
        }
        
        System.out.println("\nEnter project ID to edit (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            return;
        }
        
        Project selectedProject = null;
        for (Project project : myProjects) {
            if (project.getProjectID() == projectID) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Check if project has applications
        if (!selectedProject.getApplications().isEmpty()) {
            System.out.println("Warning: This project already has applications. Some edits may be restricted.");
        }
        
        // Get fields to edit
        String projectName = null;
        String neighborhood = null;
        Date openingDate = null;
        Date closingDate = null;
        Map<String, Integer> flatTypes = null;
        
        System.out.println("\nCurrent Project Name: " + selectedProject.getProjectName());
        System.out.println("Enter new Project Name (or leave blank to keep current):");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) {
            projectName = newName;
        }
        
        System.out.println("\nCurrent Neighborhood: " + selectedProject.getNeighborhood());
        System.out.println("Enter new Neighborhood (or leave blank to keep current):");
        String newNeighborhood = scanner.nextLine();
        if (!newNeighborhood.isEmpty()) {
            neighborhood = newNeighborhood;
        }
        
        // Check if project has applications before allowing date changes
        if (selectedProject.getApplications().isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            System.out.println("\nCurrent Opening Date: " + dateFormat.format(selectedProject.getOpeningDate()));
            System.out.println("Enter new Opening Date (dd/MM/yyyy, or leave blank to keep current):");
            String newOpeningDate = scanner.nextLine();
            if (!newOpeningDate.isEmpty()) {
                try {
                    openingDate = dateFormat.parse(newOpeningDate);
                } catch (ParseException e) {
                    System.out.println("Invalid date format. Opening date will not be changed.");
                }
            }
            
            System.out.println("\nCurrent Closing Date: " + dateFormat.format(selectedProject.getClosingDate()));
            System.out.println("Enter new Closing Date (dd/MM/yyyy, or leave blank to keep current):");
            String newClosingDate = scanner.nextLine();
            if (!newClosingDate.isEmpty()) {
                try {
                    closingDate = dateFormat.parse(newClosingDate);
                    
                    // Ensure closing date is after opening date
                    Date effectiveOpeningDate = (openingDate != null) ? openingDate : selectedProject.getOpeningDate();
                    if (closingDate.before(effectiveOpeningDate)) {
                        System.out.println("Closing date must be after opening date. Closing date will not be changed.");
                        closingDate = null;
                    }
                } catch (ParseException e) {
                    System.out.println("Invalid date format. Closing date will not be changed.");
                }
            }
        } else {
            System.out.println("\nCannot change dates for a project with existing applications.");
        }
        
        // Allow editing flat types if there are no applications
        if (selectedProject.getApplications().isEmpty()) {
            System.out.println("\nDo you want to modify flat types? (Y/N)");
            String modifyFlatTypes = scanner.nextLine();
            
            if (modifyFlatTypes.equalsIgnoreCase("Y")) {
                flatTypes = new HashMap<>();
                
                System.out.println("Enter number of 2-Room flats (0 if none):");
                int twoRoomCount = getIntInput();
                if (twoRoomCount > 0) {
                    flatTypes.put("2-Room", twoRoomCount);
                }
                
                System.out.println("Enter number of 3-Room flats (0 if none):");
                int threeRoomCount = getIntInput();
                if (threeRoomCount > 0) {
                    flatTypes.put("3-Room", threeRoomCount);
                }
                
                if (flatTypes.isEmpty()) {
                    System.out.println("At least one flat type must have units available. Flat types will not be changed.");
                    flatTypes = null;
                }
            }
        } else {
            System.out.println("\nCannot change flat types for a project with existing applications.");
        }
        
        // Attempt to edit the project
        boolean success = manager.editProject(selectedProject, projectName, neighborhood, 
                                           openingDate, closingDate, flatTypes);
        
        if (success) {
            System.out.println("Project updated successfully!");
        } else {
            System.out.println("Failed to update project.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Toggle the visibility of a project.
     */
    private void toggleProjectVisibility() {
        System.out.println("\n==================================");
        System.out.println("    Toggle Project Visibility    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nYour Projects:");
        System.out.println("ID\tProject Name\tCurrent Visibility");
        System.out.println("----------------------------------");
        
        for (Project project : myProjects) {
            System.out.printf("%d\t%s\t%s\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             project.isVisible() ? "Visible" : "Hidden");
        }
        
        System.out.println("\nEnter project ID to toggle visibility (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            return;
        }
        
        Project selectedProject = null;
        for (Project project : myProjects) {
            if (project.getProjectID() == projectID) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = manager.toggleProjectVisibility(selectedProject);
        
        if (success) {
            System.out.println("Project visibility toggled to: " + 
                              (selectedProject.isVisible() ? "Visible" : "Hidden"));
        } else {
            System.out.println("Failed to toggle visibility.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Delete an existing project.
     */
    private void deleteProject() {
        System.out.println("\n==================================");
        System.out.println("    Delete Project    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects to delete.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nYour Projects:");
        System.out.println("ID\tProject Name\tApplications");
        System.out.println("----------------------------------");
        
        for (Project project : myProjects) {
            int applicationCount = project.getApplications().size();
            System.out.printf("%d\t%s\t%d\n", 
                             project.getProjectID(), 
                             project.getProjectName(), 
                             applicationCount);
        }
        
        System.out.println("\nNote: Projects with applications cannot be deleted.");
        System.out.println("Enter project ID to delete (0 to cancel):");
        int projectID = getIntInput();
        
        if (projectID == 0) {
            return;
        }
        
        Project selectedProject = null;
        for (Project project : myProjects) {
            if (project.getProjectID() == projectID) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Confirm deletion
        System.out.println("\nAre you sure you want to delete project '" + 
                           selectedProject.getProjectName() + "'? (Y/N)");
        String confirmation = scanner.nextLine();
        
        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = manager.deleteProject(selectedProject);
        
        if (success) {
            System.out.println("Project deleted successfully!");
        } else {
            System.out.println("Failed to delete project. Check if it has applications.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process officer registrations.
     */
    private void processOfficerRegistrations() {
        System.out.println("\n==================================");
        System.out.println("    Process Officer Registrations    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Get pending registrations for all projects
        
        
List<HDBOfficer> pendingRegistrations = projectController.getPendingOfficerRegistrations(myProjects);        if (pendingRegistrations.isEmpty()) {
            System.out.println("No pending officer registrations to process.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nPending Officer Registrations:");
        System.out.println("Officer ID\tOfficer Name\tProject");
        System.out.println("----------------------------------");
        
        for (HDBOfficer officer : pendingRegistrations) {
            System.out.printf("%s\t%s\t%s\n", 
                             officer.getUserID(), 
                             officer.getName(), 
                             officer.getAssignedProject().getProjectName());
        }
        
        System.out.println("\nEnter Officer ID to process (0 to cancel):");
        String officerID = scanner.nextLine();
        
        if (officerID.equals("0")) {
            return;
        }
        
        HDBOfficer selectedOfficer = null;
        for (HDBOfficer officer : pendingRegistrations) {
            if (officer.getUserID().equals(officerID)) {
                selectedOfficer = officer;
                break;
            }
        }
        
        if (selectedOfficer == null) {
            System.out.println("Invalid Officer ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        Project selectedProject = selectedOfficer.getAssignedProject();
        
        // Display officer and project details
        System.out.println("\nOfficer Details:");
        System.out.println("ID: " + selectedOfficer.getUserID());
        System.out.println("Name: " + selectedOfficer.getName());
        System.out.println("Age: " + selectedOfficer.getAge());
        System.out.println("Marital Status: " + selectedOfficer.getMaritalStatus());
        
        System.out.println("\nProject: " + selectedProject.getProjectName());
        System.out.println("Available Officer Slots: " + selectedProject.getRemainingOfficerSlots() + 
                           " out of " + selectedProject.getMaxOfficerSlots());
        
        // Get approval/rejection
        System.out.println("\nApprove this registration? (Y/N)");
        String approve = scanner.nextLine();
        boolean approved = approve.equalsIgnoreCase("Y");
        
        boolean success = manager.processOfficerRegistration(selectedOfficer, selectedProject, approved);
        
        if (success) {
            if (approved) {
                System.out.println("Registration approved successfully!");
            } else {
                System.out.println("Registration rejected successfully!");
            }
        } else {
            System.out.println("Failed to process registration. Check if there are available officer slots.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process BTO applications.
     */
    private void processApplications() {
        System.out.println("\n==================================");
        System.out.println("    Process Applications    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Get pending applications for all projects
        List<Application> pendingApplications = applicationController.getPendingApplications(myProjects);
        
        if (pendingApplications.isEmpty()) {
            System.out.println("No pending applications to process.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nPending Applications:");
        System.out.println("ID\tApplicant\tProject\tAge\tMarital Status");
        System.out.println("----------------------------------------------------------");
        
        for (Application app : pendingApplications) {
            System.out.printf("%d\t%s\t%s\t%d\t%s\n", 
                             app.getApplicationID(), 
                             app.getApplicant().getName(), 
                             app.getProject().getProjectName(),
                             app.getApplicant().getAge(),
                             app.getApplicant().getMaritalStatus());
        }
        
        System.out.println("\nEnter Application ID to process (0 to cancel):");
        int applicationID = getIntInput();
        
        if (applicationID == 0) {
            return;
        }
        
        Application selectedApp = null;
        for (Application app : pendingApplications) {
            if (app.getApplicationID() == applicationID) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid Application ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display application details
        System.out.println("\nApplication Details:");
        System.out.println("ID: " + selectedApp.getApplicationID());
        // Display application details
        System.out.println("\nApplication Details:");
        System.out.println("ID: " + selectedApp.getApplicationID());
        System.out.println("Applicant: " + selectedApp.getApplicant().getName() + 
                           " (" + selectedApp.getApplicant().getUserID() + ")");
        System.out.println("Age: " + selectedApp.getApplicant().getAge());
        System.out.println("Marital Status: " + selectedApp.getApplicant().getMaritalStatus());
        System.out.println("Project: " + selectedApp.getProject().getProjectName());
        
        // Check eligible flat types
        Project project = selectedApp.getProject();
        Map<String, Integer> flatTypes = project.getFlatTypes();
        String eligibleTypes = "";
        
        if ("Single".equals(selectedApp.getApplicant().getMaritalStatus())) {
            // Singles can only apply for 2-Room
            if (flatTypes.containsKey("2-Room") && flatTypes.get("2-Room") > 0) {
                eligibleTypes = "2-Room";
            }
        } else {
            // Married can apply for any flat type
            if (flatTypes.containsKey("2-Room") && flatTypes.get("2-Room") > 0) {
                eligibleTypes += "2-Room, ";
            }
            if (flatTypes.containsKey("3-Room") && flatTypes.get("3-Room") > 0) {
                eligibleTypes += "3-Room";
            }
            if (eligibleTypes.endsWith(", ")) {
                eligibleTypes = eligibleTypes.substring(0, eligibleTypes.length() - 2);
            }
        }
        
        System.out.println("Eligible for: " + (eligibleTypes.isEmpty() ? "No eligible flat types" : eligibleTypes));
        
        // Get approval/rejection
        System.out.println("\nApprove this application? (Y/N)");
        String approve = scanner.nextLine();
        boolean approved = approve.equalsIgnoreCase("Y");
        
        boolean success = manager.processApplication(selectedApp, approved);
        
        if (success) {
            if (approved) {
                System.out.println("Application approved successfully!");
                System.out.println("The applicant can now book a flat with an HDB Officer.");
            } else {
                System.out.println("Application rejected successfully!");
            }
        } else {
            System.out.println("Failed to process application. Check if there are eligible flat types available.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process withdrawal requests.
     */
    private void processWithdrawalRequests() {
        System.out.println("\n==================================");
        System.out.println("    Process Withdrawal Requests    ");
        System.out.println("==================================");
        
        // Show user's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Get withdrawal requests for all projects
        List<Application> withdrawalRequests = applicationController.getWithdrawalRequests(myProjects);
        
        if (withdrawalRequests.isEmpty()) {
            System.out.println("No withdrawal requests to process.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nWithdrawal Requests:");
        System.out.println("ID\tApplicant\tProject\tStatus");
        System.out.println("----------------------------------");
        
        for (Application app : withdrawalRequests) {
            System.out.printf("%d\t%s\t%s\t%s\n", 
                             app.getApplicationID(), 
                             app.getApplicant().getName(), 
                             app.getProject().getProjectName(),
                             app.getStatus());
        }
        
        System.out.println("\nEnter Application ID to process (0 to cancel):");
        int applicationID = getIntInput();
        
        if (applicationID == 0) {
            return;
        }
        
        Application selectedApp = null;
        for (Application app : withdrawalRequests) {
            if (app.getApplicationID() == applicationID) {
                selectedApp = app;
                break;
            }
        }
        
        if (selectedApp == null) {
            System.out.println("Invalid Application ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display application details
        System.out.println("\nApplication Details:");
        System.out.println("ID: " + selectedApp.getApplicationID());
        System.out.println("Applicant: " + selectedApp.getApplicant().getName() + 
                           " (" + selectedApp.getApplicant().getUserID() + ")");
        System.out.println("Project: " + selectedApp.getProject().getProjectName());
        System.out.println("Current Status: " + selectedApp.getStatus());
        
        if (selectedApp.getFlatTypeBooked() != null) {
            System.out.println("Flat Type Booked: " + selectedApp.getFlatTypeBooked());
        }
        
        // Get approval/rejection
        System.out.println("\nApprove this withdrawal request? (Y/N)");
        String approve = scanner.nextLine();
        boolean approved = approve.equalsIgnoreCase("Y");
        
        boolean success = manager.processWithdrawalRequest(selectedApp, approved);
        
        if (success) {
            if (approved) {
                System.out.println("Withdrawal request approved successfully!");
                if (Application.STATUS_BOOKED.equals(selectedApp.getStatus())) {
                    System.out.println("The flat has been returned to the available pool.");
                }
            } else {
                System.out.println("Withdrawal request rejected successfully!");
            }
        } else {
            System.out.println("Failed to process withdrawal request.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generate reports with various filters.
     */
    private void generateReports() {
        System.out.println("\n==================================");
        System.out.println("    Generate Reports    ");
        System.out.println("==================================");
        
        System.out.println("Select Report Type:");
        System.out.println("1. All Bookings Report");
        System.out.println("2. Bookings by Project");
        System.out.println("3. Bookings by Flat Type");
        System.out.println("4. Bookings by Marital Status");
        System.out.println("5. Bookings by Age Range");
        System.out.println("6. Custom Filtered Report");
        
        System.out.print("\nEnter choice: ");
        int choice = getIntInput();
        
        Report report = null;
        
        switch (choice) {
            case 1:
                report = reportController.generateBookingReport(manager);
                break;
            case 2:
                report = generateProjectFilteredReport();
                break;
            case 3:
                report = generateFlatTypeFilteredReport();
                break;
            case 4:
                report = generateMaritalStatusFilteredReport();
                break;
            case 5:
                report = generateAgeRangeFilteredReport();
                break;
            case 6:
                report = generateCustomFilteredReport();
                break;
            default:
                System.out.println("Invalid choice!");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
        }
        
        if (report != null) {
            displayReport(report);
        } else {
            System.out.println("Failed to generate report.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Generate a report filtered by project.
     * 
     * @return The generated report
     */
    private Report generateProjectFilteredReport() {
        System.out.println("\n==================================");
        System.out.println("    Report by Project    ");
        System.out.println("==================================");
        
        // Show projects
        List<Project> allProjects = manager.viewProjects(null);
        
        if (allProjects.isEmpty()) {
            System.out.println("No projects available.");
            return null;
        }
        
        System.out.println("\nSelect a project:");
        for (int i = 0; i < allProjects.size(); i++) {
            System.out.println((i + 1) + ". " + allProjects.get(i).getProjectName());
        }
        
        System.out.print("\nEnter choice (0 to cancel): ");
        int choice = getIntInput();
        
        if (choice <= 0 || choice > allProjects.size()) {
            System.out.println("Invalid choice!");
            return null;
        }
        
        Project selectedProject = allProjects.get(choice - 1);
        
        return reportController.generateFilteredReport(manager, selectedProject.getProjectName(), 
                                                   null, null, 0, 0);
    }
    
    /**
     * Generate a report filtered by flat type.
     * 
     * @return The generated report
     */
    private Report generateFlatTypeFilteredReport() {
        System.out.println("\n==================================");
        System.out.println("    Report by Flat Type    ");
        System.out.println("==================================");
        
        System.out.println("\nSelect a flat type:");
        System.out.println("1. 2-Room");
        System.out.println("2. 3-Room");
        
        System.out.print("\nEnter choice (0 to cancel): ");
        int choice = getIntInput();
        
        String flatType = null;
        
        switch (choice) {
            case 1:
                flatType = "2-Room";
                break;
            case 2:
                flatType = "3-Room";
                break;
            default:
                System.out.println("Invalid choice!");
                return null;
        }
        
        return reportController.generateFilteredReport(manager, null, flatType, null, 0, 0);
    }
    
    /**
     * Generate a report filtered by marital status.
     * 
     * @return The generated report
     */
    private Report generateMaritalStatusFilteredReport() {
        System.out.println("\n==================================");
        System.out.println("    Report by Marital Status    ");
        System.out.println("==================================");
        
        System.out.println("\nSelect marital status:");
        System.out.println("1. Married");
        System.out.println("2. Single");
        
        System.out.print("\nEnter choice (0 to cancel): ");
        int choice = getIntInput();
        
        Boolean maritalStatus = null;
        
        switch (choice) {
            case 1:
                maritalStatus = true; // Married
                break;
            case 2:
                maritalStatus = false; // Single
                break;
            default:
                System.out.println("Invalid choice!");
                return null;
        }
        
        return reportController.generateFilteredReport(manager, null, null, maritalStatus, 0, 0);
    }
    
    /**
     * Generate a report filtered by age range.
     * 
     * @return The generated report
     */
    private Report generateAgeRangeFilteredReport() {
        System.out.println("\n==================================");
        System.out.println("    Report by Age Range    ");
        System.out.println("==================================");
        
        System.out.println("Enter minimum age (0 for no minimum):");
        int minAge = getIntInput();
        
        System.out.println("Enter maximum age (0 for no maximum):");
        int maxAge = getIntInput();
        
        if (minAge < 0 || maxAge < 0 || (maxAge > 0 && minAge > maxAge)) {
            System.out.println("Invalid age range!");
            return null;
        }
        
        return reportController.generateFilteredReport(manager, null, null, null, minAge, maxAge);
    }
    
    /**
     * Generate a custom report with multiple filters.
     * 
     * @return The generated report
     */
    private Report generateCustomFilteredReport() {
        System.out.println("\n==================================");
        System.out.println("    Custom Filtered Report    ");
        System.out.println("==================================");
        
        String projectName = null;
        String flatType = null;
        Boolean maritalStatus = null;
        int minAge = 0;
        int maxAge = 0;
        
        // Get project filter
        System.out.println("Do you want to filter by project? (Y/N)");
        String filterByProject = scanner.nextLine();
        
        if (filterByProject.equalsIgnoreCase("Y")) {
            List<Project> allProjects = manager.viewProjects(null);
            
            if (!allProjects.isEmpty()) {
                System.out.println("\nSelect a project:");
                for (int i = 0; i < allProjects.size(); i++) {
                    System.out.println((i + 1) + ". " + allProjects.get(i).getProjectName());
                }
                
                System.out.print("\nEnter choice (0 to skip): ");
                int choice = getIntInput();
                
                if (choice > 0 && choice <= allProjects.size()) {
                    projectName = allProjects.get(choice - 1).getProjectName();
                }
            }
        }
        
        // Get flat type filter
        System.out.println("\nDo you want to filter by flat type? (Y/N)");
        String filterByFlatType = scanner.nextLine();
        
        if (filterByFlatType.equalsIgnoreCase("Y")) {
            System.out.println("\nSelect a flat type:");
            System.out.println("1. 2-Room");
            System.out.println("2. 3-Room");
            
            System.out.print("\nEnter choice (0 to skip): ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    flatType = "2-Room";
                    break;
                case 2:
                    flatType = "3-Room";
                    break;
            }
        }
        
        // Get marital status filter
        System.out.println("\nDo you want to filter by marital status? (Y/N)");
        String filterByMaritalStatus = scanner.nextLine();
        
        if (filterByMaritalStatus.equalsIgnoreCase("Y")) {
            System.out.println("\nSelect marital status:");
            System.out.println("1. Married");
            System.out.println("2. Single");
            
            System.out.print("\nEnter choice (0 to skip): ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    maritalStatus = true; // Married
                    break;
                case 2:
                    maritalStatus = false; // Single
                    break;
            }
        }
        
        // Get age range filter
        System.out.println("\nDo you want to filter by age range? (Y/N)");
        String filterByAge = scanner.nextLine();
        
        if (filterByAge.equalsIgnoreCase("Y")) {
            System.out.println("Enter minimum age (0 for no minimum):");
            minAge = getIntInput();
            
            System.out.println("Enter maximum age (0 for no maximum):");
            maxAge = getIntInput();
            
            if (minAge < 0 || maxAge < 0 || (maxAge > 0 && minAge > maxAge)) {
                System.out.println("Invalid age range! Age filter will be skipped.");
                minAge = 0;
                maxAge = 0;
            }
        }
        
        return reportController.generateFilteredReport(manager, projectName, flatType, maritalStatus, minAge, maxAge);
    }
    
    /**
     * Display a generated report.
     * 
     * @param report The report to display
     */
    private void displayReport(Report report) {
        System.out.println("\n==================================");
        System.out.println("    Report: " + report.getTitle() + "    ");
        System.out.println("==================================");
        System.out.println("Generated by: " + report.getManager().getName());
        System.out.println("Date: " + report.getGenerationDate());
        
        List<Application> applications = report.getApplications();
        System.out.println("\nTotal Bookings: " + applications.size());
        
        // Display statistics if available
        Map<String, Map<String, Integer>> statistics = report.getStatistics();
        if (statistics != null && !statistics.isEmpty()) {
            System.out.println("\nStatistics:");
            
            for (String category : statistics.keySet()) {
                System.out.println("\n" + category + ":");
                Map<String, Integer> statData = statistics.get(category);
                
                for (String key : statData.keySet()) {
                    System.out.println("  " + key + ": " + statData.get(key));
                }
            }
        }
        
        // Display applications
        if (!applications.isEmpty()) {
            System.out.println("\nBooking Details:");
            System.out.println("ID\tApplicant\tProject\tFlat Type\tAge\tMarital Status");
            System.out.println("--------------------------------------------------------------------");
            
            for (Application app : applications) {
                System.out.printf("%d\t%s\t%s\t%s\t%d\t%s\n", 
                                 app.getApplicationID(), 
                                 app.getApplicant().getName(), 
                                 app.getProject().getProjectName(),
                                 app.getFlatTypeBooked(),
                                 app.getApplicant().getAge(),
                                 app.getApplicant().getMaritalStatus());
            }
        }
        
        // Save report option
        System.out.println("\nWould you like to save this report? (Y/N)");
        String saveReport = scanner.nextLine();
        
        if (saveReport.equalsIgnoreCase("Y")) {
            boolean saved = reportController.saveReport(report);
            if (saved) {
                System.out.println("Report saved successfully.");
            } else {
                System.out.println("Failed to save report.");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * View all enquiries across all projects.
     */
    private void viewAllEnquiries() {
        System.out.println("\n==================================");
        System.out.println("    View All Enquiries    ");
        System.out.println("==================================");
        
        List<Enquiry> allEnquiries = enquiryController.getAllEnquiries();
        
        if (allEnquiries.isEmpty()) {
            System.out.println("No enquiries in the system.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nID\tProject\tFrom\tDate\tStatus\tEnquiry");
        System.out.println("--------------------------------------------------------------------");
        
        for (Enquiry enquiry : allEnquiries) {
            String status = enquiry.getResponse() == null ? "Pending" : "Responded";
            String enquiryText = enquiry.getEnquiryText();
            if (enquiryText.length() > 30) {
                enquiryText = enquiryText.substring(0, 27) + "...";
            }
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\t%s\n", 
                             enquiry.getEnquiryID(), 
                             enquiry.getProject().getProjectName(),
                             enquiry.getUser().getName(),
                             enquiry.getCreationDate(),
                             status,
                             enquiryText);
        }
        
        // View enquiry details
        System.out.println("\nEnter Enquiry ID to view details (0 to cancel):");
        int enquiryID = getIntInput();
        
        if (enquiryID == 0) {
            return;
        }
        
        Enquiry selectedEnquiry = null;
        for (Enquiry enquiry : allEnquiries) {
            if (enquiry.getEnquiryID() == enquiryID) {
                selectedEnquiry = enquiry;
                break;
            }
        }
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid Enquiry ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        displayEnquiryDetails(selectedEnquiry);
    }
    
    /**
     * Reply to enquiries.
     */
    private void replyToEnquiries() {
        System.out.println("\n==================================");
        System.out.println("    Reply to Enquiries    ");
        System.out.println("==================================");
        
        // Get enquiries for manager's projects
        Map<String, Object> filters = new HashMap<>();
        filters.put("createdByMe", true);
        List<Project> myProjects = manager.viewProjects(filters);
        
        if (myProjects.isEmpty()) {
            System.out.println("You haven't created any projects.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        List<Enquiry> projectEnquiries = new ArrayList<>();
        for (Project project : myProjects) {
            projectEnquiries.addAll(enquiryController.getProjectEnquiries(project));
        }
        
        // Filter to only pending enquiries
        List<Enquiry> pendingEnquiries = new ArrayList<>();
        for (Enquiry enquiry : projectEnquiries) {
            if (enquiry.getResponse() == null) {
                pendingEnquiries.add(enquiry);
            }
        }
        
        if (pendingEnquiries.isEmpty()) {
            System.out.println("No pending enquiries to reply to.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nPending Enquiries:");
        System.out.println("ID\tProject\tFrom\tDate\tEnquiry");
        System.out.println("------------------------------------------------------------");
        
        for (Enquiry enquiry : pendingEnquiries) {
            String enquiryText = enquiry.getEnquiryText();
            if (enquiryText.length() > 30) {
                enquiryText = enquiryText.substring(0, 27) + "...";
            }
            
            System.out.printf("%d\t%s\t%s\t%s\t%s\n", 
                             enquiry.getEnquiryID(), 
                             enquiry.getProject().getProjectName(),
                             enquiry.getUser().getName(),
                             enquiry.getCreationDate(),
                             enquiryText);
        }
        
        // Select enquiry to reply to
        System.out.println("\nEnter Enquiry ID to reply to (0 to cancel):");
        int enquiryID = getIntInput();
        
        if (enquiryID == 0) {
            return;
        }
        
        Enquiry selectedEnquiry = null;
        for (Enquiry enquiry : pendingEnquiries) {
            if (enquiry.getEnquiryID() == enquiryID) {
                selectedEnquiry = enquiry;
                break;
            }
        }
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid Enquiry ID!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display enquiry details
        System.out.println("\nEnquiry Details:");
        System.out.println("ID: " + selectedEnquiry.getEnquiryID());
        System.out.println("From: " + selectedEnquiry.getUser().getName() + 
                           " (" + selectedEnquiry.getUser().getUserID() + ")");
        System.out.println("Project: " + selectedEnquiry.getProject().getProjectName());
        System.out.println("Date: " + selectedEnquiry.getCreationDate());
        System.out.println("Enquiry: " + selectedEnquiry.getEnquiryText());
        
        // Get response
        System.out.println("\nEnter your response:");
        String response = scanner.nextLine();
        
        if (response.trim().isEmpty()) {
            System.out.println("Response cannot be empty.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        boolean success = enquiryController.respondToEnquiry(selectedEnquiry, response);
        
        if (success) {
            System.out.println("Response submitted successfully!");
        } else {
            System.out.println("Failed to submit response.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Display detailed information about an enquiry.
     * 
     * @param enquiry The enquiry to display details for
     */
    private void displayEnquiryDetails(Enquiry enquiry) {
        System.out.println("\n==================================");
        System.out.println("    Enquiry Details    ");
        System.out.println("==================================");
        
        System.out.println("ID: " + enquiry.getEnquiryID());
        System.out.println("Project: " + enquiry.getProject().getProjectName());
        System.out.println("From: " + enquiry.getUser().getName() + 
                           " (" + enquiry.getUser().getUserID() + ")");
        System.out.println("Date: " + enquiry.getCreationDate());
        System.out.println("\nEnquiry:");
        System.out.println(enquiry.getEnquiryText());
        
        if (enquiry.getResponse() != null) {
            System.out.println("\nResponse:");
            System.out.println(enquiry.getResponse());
        } else {
            System.out.println("\nStatus: Pending response");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Change the manager's password.
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
        
        boolean success = manager.changePassword(currentPassword, newPassword);
        
        if (success) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Please check your current password and try again.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Get the status of a project based on its dates.
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