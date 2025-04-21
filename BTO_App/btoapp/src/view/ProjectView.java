package view;

import controller.ApplicationController;
import controller.ManagerController;
import controller.ProjectController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import model.Applicant;
import model.Application;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.User;
import model.enums.FlatType;
import view.abstracts.ARenderView;
import view.interfaces.ViewInterface;

/**
 * View for project-related operations in the BTO Management System.
 */
public class ProjectView extends ARenderView implements ViewInterface{
    
    private User currentUser;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private ManagerController managerController;
    private Scanner scanner;
    
    /**
     * Constructor for ProjectView.
     * 
     * @param currentUser The currently logged-in user
     * @param projectController Controller for project operations
     * @param applicationController Controller for application operations
     * @param managerController Controller for manager operations
     */
    public ProjectView(User currentUser, ProjectController projectController, 
        ApplicationController applicationController,
        ManagerController managerController) {
            this.currentUser = currentUser;
            this.projectController = projectController;
            this.applicationController = applicationController;
            this.managerController = managerController;
            this.scanner = new Scanner(System.in);
    }

    public void display() {
        if (currentUser instanceof Applicant) {
            displayProjectsForApplicant((Applicant) currentUser);
        } else if (currentUser instanceof HDBOfficer) {
            displayProjectsForOfficer((HDBOfficer) currentUser);
        } else if (currentUser instanceof HDBManager) {
            displayAllProjects();
        }
    }
    
    /**
     * Displays available projects for an applicant.
     * 
     * @param applicant The applicant
     */
    private void displayProjectsForApplicant(Applicant applicant) {
        printHeader("AVAILABLE PROJECTS");
        
        List<Project> projects = projectController.getVisibleProjectsForApplicant(applicant);
        
        if (projects.isEmpty()) {
            showMessage("No available projects found for your eligibility criteria.");
            return;
        }
        
        System.out.println("ID | Project Name | Neighborhood | Application Period | Available Flat Types");
        System.out.println("---------------------------------------------------------------------");
        
        int index = 1;
        for (Project project : projects) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String openDate = dateFormat.format(project.getApplicationOpeningDate());
            String closeDate = dateFormat.format(project.getApplicationClosingDate());
            
            StringBuilder flatTypes = new StringBuilder();
            for (Project.FlatTypeInfo info : project.getFlatTypeInfoList()) {
                if (flatTypes.length() > 0) {
                    flatTypes.append(", ");
                }
                flatTypes.append(info.getFlatType().getDisplayName());
            }
            
            System.out.printf("%2d | %-20s | %-15s | %s to %s | %s\n",
                            index++, project.getProjectName(), project.getNeighborhood(),
                            openDate, closeDate, flatTypes.toString());
        }
        
        System.out.print("\nEnter the ID of a project to view details, or 0 to return: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= projects.size()) {
            displayProjectDetails(projects.get(choice - 1));
            
            // Option to apply
            System.out.println("\nWould you like to apply for this project? (Y/N)");
            String applyOption = scanner.nextLine();
            
            if (applyOption.equalsIgnoreCase("Y")) {
                applyForProject(applicant, projects.get(choice - 1));
            }
        }
    }
    
    /**
     * Displays projects for an HDB officer.
     * 
     * @param officer The HDB officer
     */
    private void displayProjectsForOfficer(HDBOfficer officer) {
        printHeader("PROJECTS");
        
        List<Project> projects = projectController.getAllProjects();
        
        if (projects.isEmpty()) {
            showMessage("No projects found in the system.");
            return;
        }
        
        System.out.println("ID | Project Name | Neighborhood | Application Period | Status");
        System.out.println("----------------------------------------------------------");
        
        int index = 1;
        for (Project project : projects) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String openDate = dateFormat.format(project.getApplicationOpeningDate());
            String closeDate = dateFormat.format(project.getApplicationClosingDate());
            
            String status = "Available";
            if (officer.isAssignedToProject(project)) {
                status = "Assigned";
            } else if (officer.getAssignedProject() != null && 
                      !officer.getAssignedProject().equals(project)) {
                status = "Not Eligible";
            }
            
            System.out.printf("%2d | %-20s | %-15s | %s to %s | %s\n",
                            index++, project.getProjectName(), project.getNeighborhood(),
                            openDate, closeDate, status);
        }
        
        System.out.println("\nEnter the ID of a project to view details, or 0 to return: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= projects.size()) {
            displayProjectDetails(projects.get(choice - 1));
        }
    }
    
    /**
     * Displays all projects for a manager.
     */
    public void displayAllProjects() {
        printHeader("ALL PROJECTS");
        
        List<Project> projects = projectController.getAllProjects();
        
        if (projects.isEmpty()) {
            showMessage("No projects found in the system.");
            return;
        }
        
        System.out.println("ID | Project Name | Neighborhood | Manager | App Period | Visibility | # Units");
        System.out.println("------------------------------------------------------------------------");
        
        int index = 1;
        for (Project project : projects) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String openDate = dateFormat.format(project.getApplicationOpeningDate());
            String closeDate = dateFormat.format(project.getApplicationClosingDate());
            
            int totalUnits = 0;
            for (Project.FlatTypeInfo info : project.getFlatTypeInfoList()) {
                totalUnits += info.getNumberOfUnits();
            }
            
            System.out.printf("%2d | %-20s | %-15s | %-15s | %s to %s | %-10s | %d\n",
                            index++, project.getProjectName(), project.getNeighborhood(),
                            project.getManagerInCharge().getName(),
                            openDate, closeDate, 
                            project.isVisible() ? "Visible" : "Hidden",
                            totalUnits);
        }
        
        System.out.print("\nEnter the ID of a project to view/edit details, or 0 to return: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= projects.size()) {
            Project selectedProject = projects.get(choice - 1);
            displayProjectDetails(selectedProject);
            
            // If current user is the manager in charge, offer edit options
            if (currentUser instanceof HDBManager && 
                selectedProject.getManagerInCharge().getNric().equals(currentUser.getNric())) {
                displayManagerProjectOptions(selectedProject);
            }
        }
    }
    
    /**
     * Displays the interface for creating a new project.
     */
    public void displayCreateProject() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can create projects.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("CREATE NEW PROJECT");
        
        System.out.print("Project Name: ");
        String projectName = scanner.nextLine();
        
        System.out.print("Neighborhood: ");
        String neighborhood = scanner.nextLine();
        
        // Get flat types
        List<FlatType> flatTypes = new ArrayList<>();
        List<Integer> numberOfUnits = new ArrayList<>();
        List<Double> sellingPrices = new ArrayList<>();
        
        // 2-Room flat
        System.out.print("Number of 2-Room units (0 if none): ");
        int twoRoomUnits = getIntInput();
        if (twoRoomUnits > 0) {
            System.out.print("Selling price for 2-Room units: $");
            double twoRoomPrice = getDoubleInput();
            
            flatTypes.add(FlatType.TWO_ROOM);
            numberOfUnits.add(twoRoomUnits);
            sellingPrices.add(twoRoomPrice);
        }
        
        // 3-Room flat
        System.out.print("Number of 3-Room units (0 if none): ");
        int threeRoomUnits = getIntInput();
        if (threeRoomUnits > 0) {
            System.out.print("Selling price for 3-Room units: $");
            double threeRoomPrice = getDoubleInput();
            
            flatTypes.add(FlatType.THREE_ROOM);
            numberOfUnits.add(threeRoomUnits);
            sellingPrices.add(threeRoomPrice);
        }
        
        // Application period
        System.out.print("Application Opening Date (DD/MM/YYYY): ");
        Date openingDate = getDateInput();
        
        System.out.print("Application Closing Date (DD/MM/YYYY): ");
        Date closingDate = getDateInput();
        
        System.out.print("Number of officer slots (max 10): ");
        int officerSlots = getIntInput();
        officerSlots = Math.min(10, Math.max(0, officerSlots));
        
        // Create project
        Project project = projectController.createProject(projectName, neighborhood, 
                                                        flatTypes, numberOfUnits, 
                                                        sellingPrices, openingDate, 
                                                        closingDate, manager, officerSlots);
        
        if (project != null) {
            showMessage("Project created successfully!");
            displayProjectDetails(project);
        } else {
            showError("Failed to create project. Please check your inputs.");
        }
    }
    
    /**
     * Displays the interface for registering as an officer for a project.
     */
    public void displayRegisterForProject() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can register for projects.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if already assigned to a project
        if (officer.isProjectAssigned()) {
            showError("You are already assigned to a project: " + 
                     officer.getAssignedProject().getProjectName());
            return;
        }
        
        printHeader("REGISTER FOR PROJECT");
        
        // Get available projects
        List<Project> availableProjects = new ArrayList<>();
        List<Project> allProjects = projectController.getAllProjects();
        
        for (Project project : allProjects) {
            // Check if project has available slots and officer is not applying for it
            if (project.getRemainingOfficerSlots() > 0 && 
                (officer.getCurrentApplication() == null || 
                 !officer.getCurrentApplication().getProject().equals(project))) {
                availableProjects.add(project);
            }
        }
        
        if (availableProjects.isEmpty()) {
            showError("No projects available for registration.");
            return;
        }
        
        System.out.println("ID | Project Name | Neighborhood | Officer Slots Available");
        System.out.println("--------------------------------------------------------");
        
        int index = 1;
        for (Project project : availableProjects) {
            System.out.printf("%2d | %-20s | %-15s | %d\n",
                            index++, project.getProjectName(), project.getNeighborhood(),
                            project.getRemainingOfficerSlots());
        }
        
        System.out.print("\nSelect a project to register for (1-" + availableProjects.size() + "): ");
        int choice = getIntInput();
        
        if (choice < 1 || choice > availableProjects.size()) {
            showError("Invalid selection.");
            return;
        }
        
        Project selectedProject = availableProjects.get(choice - 1);
        
        // Confirm registration
        System.out.println("\nYou are about to register as an HDB Officer for " + 
                          selectedProject.getProjectName() + ".");
        System.out.print("Confirm registration? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean registered = projectController.registerOfficerForProject(
                selectedProject.getProjectName(), officer);
            
            if (registered) {
                showMessage("Registration submitted successfully! Waiting for manager approval.");
            } else {
                showError("Failed to register for project. Please try again later.");
            }
        } else {
            showMessage("Registration cancelled.");
        }
    }
    
    /**
     * Displays the registration status for an officer.
     */
    public void displayRegistrationStatus() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can check registration status.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        printHeader("REGISTRATION STATUS");
        
        if (officer.getAssignedProject() == null) {
            showMessage("You are not registered for any project.");
            return;
        }
        
        Project project = officer.getAssignedProject();
        
        System.out.println("Project: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Manager: " + project.getManagerInCharge().getName());
        System.out.println("Status: " + (officer.isRegistrationApproved() ? "Approved" : "Pending Approval"));
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays the details of the assigned project for an officer.
     */
    public void displayAssignedProject() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can view assigned projects.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            showError("You are not assigned to any project yet.");
            return;
        }
        
        Project project = officer.getAssignedProject();
        
        displayProjectDetails(project);
    }
    
    /**
     * Displays the interface for viewing projects created by the manager.
     */
    public void displayMyProjects() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can view their projects.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("MY PROJECTS");
        
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You have not created any projects yet.");
            return;
        }
        
        System.out.println("ID | Project Name | Neighborhood | App Period | Visibility | # Units");
        System.out.println("----------------------------------------------------------------");
        
        int index = 1;
        for (Project project : projects) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String openDate = dateFormat.format(project.getApplicationOpeningDate());
            String closeDate = dateFormat.format(project.getApplicationClosingDate());
            
            int totalUnits = 0;
            for (Project.FlatTypeInfo info : project.getFlatTypeInfoList()) {
                totalUnits += info.getNumberOfUnits();
            }
            
            System.out.printf("%2d | %-20s | %-15s | %s to %s | %-10s | %d\n",
                            index++, project.getProjectName(), project.getNeighborhood(),
                            openDate, closeDate, 
                            project.isVisible() ? "Visible" : "Hidden",
                            totalUnits);
        }
        
        System.out.print("\nEnter the ID of a project to view/edit details, or 0 to return: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= projects.size()) {
            Project selectedProject = projects.get(choice - 1);
            displayProjectDetails(selectedProject);
            displayManagerProjectOptions(selectedProject);
        }
    }
    
    /**
     * Displays the interface for updating a project.
     */
    public void displayUpdateProject() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can update projects.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("UPDATE PROJECT");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You have not created any projects yet.");
            return;
        }
        
        System.out.println("Select a project to update:");
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
        
        displayUpdateProject(selectedProject);
    }
    
    /**
     * Displays the interface for updating a specific project.
     * 
     * @param project The project to update
     */
    public void displayUpdateProject(Project project) {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can update projects.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("UPDATE PROJECT: " + project.getProjectName());
        
        System.out.println("Current Project Details:");
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Application Period: " + 
                          dateFormat.format(project.getApplicationOpeningDate()) + " to " + 
                          dateFormat.format(project.getApplicationClosingDate()));
        
        System.out.println("Officer Slots: " + project.getOfficerSlots());
        
        // Get new values
        System.out.println("\nEnter new values (press Enter to keep current value):");
        
        System.out.print("Project Name [" + project.getProjectName() + "]: ");
        String projectName = scanner.nextLine();
        if (projectName.trim().isEmpty()) {
            projectName = project.getProjectName();
        }
        
        System.out.print("Neighborhood [" + project.getNeighborhood() + "]: ");
        String neighborhood = scanner.nextLine();
        if (neighborhood.trim().isEmpty()) {
            neighborhood = project.getNeighborhood();
        }
        
        System.out.print("Application Opening Date [" + 
                        dateFormat.format(project.getApplicationOpeningDate()) + "]: ");
        String openingDateStr = scanner.nextLine();
        Date openingDate = project.getApplicationOpeningDate();
        if (!openingDateStr.trim().isEmpty()) {
            try {
                openingDate = dateFormat.parse(openingDateStr);
            } catch (ParseException e) {
                showError("Invalid date format. Using existing date.");
            }
        }
        
        System.out.print("Application Closing Date [" + 
                        dateFormat.format(project.getApplicationClosingDate()) + "]: ");
        String closingDateStr = scanner.nextLine();
        Date closingDate = project.getApplicationClosingDate();
        if (!closingDateStr.trim().isEmpty()) {
            try {
                closingDate = dateFormat.parse(closingDateStr);
            } catch (ParseException e) {
                showError("Invalid date format. Using existing date.");
            }
        }
        
        System.out.print("Officer Slots [" + project.getOfficerSlots() + "]: ");
        String officerSlotsStr = scanner.nextLine();
        int officerSlots = project.getOfficerSlots();
        if (!officerSlotsStr.trim().isEmpty()) {
            try {
                officerSlots = Integer.parseInt(officerSlotsStr);
                officerSlots = Math.min(10, Math.max(project.getAssignedOfficers().size(), officerSlots));
            } catch (NumberFormatException e) {
                showError("Invalid number. Using existing value.");
            }
        }
        
        // Update project
        boolean updated = projectController.updateProject(project.getProjectName(), projectName, 
                                                      neighborhood, openingDate, closingDate,
                                                      officerSlots, manager);
        
        if (updated) {
            showMessage("Project updated successfully!");
        } else {
            showError("Failed to update project. Please check your inputs.");
        }
    }
    
    /**
     * Displays the interface for toggling project visibility.
     */
    public void displayToggleVisibility() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can toggle project visibility.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("TOGGLE PROJECT VISIBILITY");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You have not created any projects yet.");
            return;
        }
        
        System.out.println("ID | Project Name | Current Visibility");
        System.out.println("-----------------------------------");
        
        int index = 1;
        for (Project project : projects) {
            System.out.printf("%2d | %-20s | %s\n",
                            index++, project.getProjectName(),
                            project.isVisible() ? "Visible" : "Hidden");
        }
        
        System.out.print("\nEnter the ID of a project to toggle visibility, or 0 to return: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= projects.size()) {
            Project selectedProject = projects.get(choice - 1);
            toggleProjectVisibility(selectedProject);
        }
    }
    
    /**
     * Toggles the visibility of a project.
     * 
     * @param project The project to toggle visibility for
     */
    private void toggleProjectVisibility(Project project) {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can toggle project visibility.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        System.out.println("\nCurrent visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Toggle to: " + (project.isVisible() ? "Hidden" : "Visible"));
        System.out.print("Confirm toggle? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean toggled = projectController.toggleProjectVisibility(
                project.getProjectName(), !project.isVisible(), manager);
            
            if (toggled) {
                showMessage("Project visibility toggled successfully.");
            } else {
                showError("Failed to toggle project visibility.");
            }
        } else {
            showMessage("Toggle cancelled.");
        }
    }
    
    /**
     * Displays the interface for managing officer registrations.
     */
    public void displayManageOfficers() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can manage officer registrations.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("MANAGE OFFICER REGISTRATIONS");
        
        // Get projects managed by this manager
        List<Project> projects = projectController.getProjectsByManager(manager);
        
        if (projects.isEmpty()) {
            showMessage("You have not created any projects yet.");
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
        
        displayManageOfficers(selectedProject);
    }
    
    /**
     * Displays the interface for managing officer registrations for a specific project.
     * 
     * @param project The project to manage officers for
     */
    public void displayManageOfficers(Project project) {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can manage officer registrations.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("OFFICER REGISTRATIONS FOR: " + project.getProjectName());
        
        // Get pending officer registrations
        List<HDBOfficer> pendingOfficers = managerController.getPendingOfficersForProject(project.getProjectName());
        
        if (pendingOfficers.isEmpty()) {
            showMessage("No pending officer registrations for this project.");
        } else {
            System.out.println("Pending Officer Registrations:");
            System.out.println("ID | Officer Name | NRIC");
            System.out.println("-------------------------");
            
            int index = 1;
            for (HDBOfficer officer : pendingOfficers) {
                System.out.printf("%2d | %-20s | %s\n",
                                index++, officer.getName(), officer.getNric());
            }
            
            System.out.print("\nEnter the ID of an officer to approve/reject, or 0 to continue: ");
            int choice = getIntInput();
            
            if (choice > 0 && choice <= pendingOfficers.size()) {
                HDBOfficer selectedOfficer = pendingOfficers.get(choice - 1);
                
                System.out.println("\n1. Approve Registration");
                System.out.println("2. Reject Registration");
                System.out.println("0. Cancel");
                
                System.out.print("\nEnter selection (0-2): ");
                int action = getIntInput();
                
                if (action == 1) {
                    boolean approved = managerController.approveOfficerRegistration(selectedOfficer, manager);
                    
                    if (approved) {
                        showMessage("Officer registration approved successfully.");
                    } else {
                        showError("Failed to approve officer registration.");
                    }
                } else if (action == 2) {
                    boolean rejected = manager.rejectOfficerRegistration(selectedOfficer);
                    
                    if (rejected) {
                        showMessage("Officer registration rejected successfully.");
                    } else {
                        showError("Failed to reject officer registration.");
                    }
                }
            }
        }
        
        // Show approved officers
        List<HDBOfficer> approvedOfficers = projectController.getApprovedOfficersForProject(project.getProjectName());
        
        if (approvedOfficers.isEmpty()) {
            System.out.println("\nNo approved officers for this project.");
        } else {
            System.out.println("\nApproved Officers:");
            System.out.println("ID | Officer Name | NRIC");
            System.out.println("-------------------------");
            
            int index = 1;
            for (HDBOfficer officer : approvedOfficers) {
                System.out.printf("%2d | %-20s | %s\n",
                                index++, officer.getName(), officer.getNric());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays detailed information about a project.
     * 
     * @param project The project to display
     */
    private void displayProjectDetails(Project project) {
        printHeader("PROJECT DETAILS: " + project.getProjectName());
        
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Application Period: " + 
                          dateFormat.format(project.getApplicationOpeningDate()) + " to " + 
                          dateFormat.format(project.getApplicationClosingDate()));
        
        System.out.println("Manager In Charge: " + project.getManagerInCharge().getName());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        
        System.out.println("\nFlat Types:");
        for (Project.FlatTypeInfo info : project.getFlatTypeInfoList()) {
            System.out.printf("- %s: %d units at $%.2f\n", 
                             info.getFlatType().getDisplayName(),
                             info.getNumberOfUnits(),
                             info.getSellingPrice());
        }
        
        System.out.println("\nOfficer Slots: " + project.getOfficerSlots() + 
                          " (Remaining: " + project.getRemainingOfficerSlots() + ")");
        
        List<HDBOfficer> officers = project.getAssignedOfficers();
        if (!officers.isEmpty()) {
            System.out.println("\nAssigned Officers:");
            for (HDBOfficer officer : officers) {
                System.out.println("- " + officer.getName());
            }
        }
        
        // Wait for user to continue
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays manager-specific project options.
     * 
     * @param project The selected project
     */
    private void displayManagerProjectOptions(Project project) {
        printHeader("PROJECT MANAGEMENT OPTIONS");
        System.out.println("1. Edit Project Details");
        System.out.println("2. Toggle Project Visibility");
        System.out.println("3. Manage Officer Registrations");
        System.out.println("4. Delete Project");
        System.out.println("0. Return to Project List");
        
        System.out.print("\nEnter selection: ");
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                displayUpdateProject(project);
                break;
            case 2:
                toggleProjectVisibility(project);
                break;
            case 3:
                displayManageOfficers(project);
                break;
            case 4:
                deleteProject(project);
                break;
        }
    }
    
    /**
     * Deletes a project.
     * 
     * @param project The project to delete
     */
    private void deleteProject(Project project) {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can delete projects.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("DELETE PROJECT");
        
        System.out.println("Are you sure you want to delete the project: " + project.getProjectName() + "?");
        System.out.println("This action cannot be undone.");
        System.out.print("Type 'DELETE' to confirm: ");
        String confirmation = scanner.nextLine();
        
        if (confirmation.equals("DELETE")) {
            boolean deleted = projectController.deleteProject(project.getProjectName(), manager);
            
            if (deleted) {
                showMessage("Project deleted successfully.");
            } else {
                showError("Failed to delete project. It may have active applications.");
            }
        } else {
            showMessage("Project deletion cancelled.");
        }
    }
    
    /**
     * Applies for a project.
     * 
     * @param applicant The applicant
     * @param project The project to apply for
     */
    private void applyForProject(Applicant applicant, Project project) {
        printHeader("APPLY FOR PROJECT: " + project.getProjectName());
        
        // Check if applicant already has an active application
        if (applicant.hasActiveApplication()) {
            showError("You already have an active application. You cannot apply for multiple projects.");
            return;
        }
        
        // Add explicit check for HDB Officers
        if (currentUser instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) currentUser;
            System.out.println("Checking if officer is assigned to project: " + project.getProjectName());
            System.out.println("Officer assigned project: " + 
                            (officer.getAssignedProject() != null ? officer.getAssignedProject().getProjectName() : "none"));
            System.out.println("Registration approved: " + officer.isRegistrationApproved());
            
            // Direct check for assigned officers
            List<HDBOfficer> assignedOfficers = project.getAssignedOfficers();
            for (HDBOfficer assignedOfficer : assignedOfficers) {
                if (assignedOfficer.getNric().equals(officer.getNric())) {
                    showError("As an HDB Officer assigned to this project, you cannot apply for it.");
                    return;
                }
            }
        }
        
        // Display available flat types
        System.out.println("Available Flat Types:");
        List<FlatType> availableFlatTypes = new ArrayList<>();
        
        for (Project.FlatTypeInfo info : project.getFlatTypeInfoList()) {
            if (info.getNumberOfUnits() > 0) {
                FlatType type = info.getFlatType();
                
                // Check eligibility - basic check, controller will do detailed check
                boolean eligible = true;
                if (!applicant.isMarried() && type != FlatType.TWO_ROOM) {
                    eligible = false;
                }
                
                if (eligible) {
                    System.out.printf("%d. %s - $%.2f\n", 
                                    availableFlatTypes.size() + 1, 
                                    type.getDisplayName(),
                                    info.getSellingPrice());
                    availableFlatTypes.add(type);
                }
            }
        }
        
        if (availableFlatTypes.isEmpty()) {
            showError("There are no flat types available for you in this project.");
            return;
        }
        
        System.out.print("\nSelect flat type (1-" + availableFlatTypes.size() + "): ");
        int flatTypeChoice = getIntInput();
        
        if (flatTypeChoice < 1 || flatTypeChoice > availableFlatTypes.size()) {
            showError("Invalid selection.");
            return;
        }
        
        FlatType selectedFlatType = availableFlatTypes.get(flatTypeChoice - 1);
        
        // Confirm application
        System.out.println("\nYou are about to apply for a " + selectedFlatType.getDisplayName() + 
                        " flat in " + project.getProjectName() + ".");
        System.out.print("Confirm application? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            // Submit application
            Application application = applicationController.submitApplication(applicant, project, selectedFlatType);
            
            if (application != null) {
                showMessage("Application submitted successfully! Your application ID is: " + 
                        application.getApplicationId());
            } else {
                showError("Failed to submit application. Please try again later.");
            }
        } else {
            showMessage("Application cancelled.");
        }
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
    
    /**
     * Gets a double input from the user.
     * 
     * @return The double input
     */
    private double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    /**
     * Gets a date input from the user.
     * 
     * @return The date input
     */
    private Date getDateInput() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        while (true) {
            try {
                return dateFormat.parse(scanner.nextLine());
            } catch (ParseException e) {
                System.out.print("Please enter a valid date (DD/MM/YYYY): ");
            }
        }
    }
}