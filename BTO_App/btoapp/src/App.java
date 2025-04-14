import controller.ApplicationController;
import controller.AuthenticationController;
import controller.EnquiryController;
import controller.ProjectController;
import datamanager.ApplicantDataManager;
import datamanager.ApplicationDataManager;
import datamanager.EnquiryDataManager;
import datamanager.ManagerDataManager;
import datamanager.OfficerDataManager;
import datamanager.ProjectDataManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.Applicant;
import model.Project;
import model.User;
import service.EligibilityCheckerService;
import view.LoginView;
import view.MainMenuView;

/**
 * The App class initiates the BTO Management System startup.
 * 
 * @author Your Name
 * @version 1.0
 */
public class App {
    // Scanner for user input
    private Scanner scanner;

    // Controllers and Managers
    private AuthenticationController authController;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;

    /**
     * Constructor initializes application components.
     */
    public App() {
        try {
            // Initialize scanner
            scanner = new Scanner(System.in);

            // Initialize data managers
            System.out.println("Initializing data managers...");
            ApplicantDataManager applicantDataManager = new ApplicantDataManager();
            OfficerDataManager officerDataManager = new OfficerDataManager();
            ManagerDataManager managerDataManager = new ManagerDataManager();
            
            // Debug: Check if files exist and load data
            System.out.println("Loading applicant data...");
            List<Applicant> applicants = applicantDataManager.readAllApplicants();
            System.out.println("Loaded " + applicants.size() + " applicants");
            
            System.out.println("Loading officer data...");
            boolean officersLoaded = officerDataManager.loadOfficerData();
            System.out.println("Officers loaded: " + officersLoaded);
            
            System.out.println("Loading manager data...");
            boolean managersLoaded = managerDataManager.loadManagerData();
            System.out.println("Managers loaded: " + managersLoaded);

            // Initialize authentication controller
            System.out.println("DEBUG: Loading users into authentication controller...");
            authController = new AuthenticationController(
                applicantDataManager, 
                officerDataManager, 
                managerDataManager
            );
            
            // Step 1: Create maps of managers and officers for ProjectDataManager
            System.out.println("DEBUG: Creating manager and officer maps...");
            Map<String, model.HDBManager> managerMap = managerDataManager.getAllManagers().stream()
                .collect(java.util.stream.Collectors.toMap(
                    m -> m.getNric().trim(), 
                    m -> m
                ));
            
            Map<String, model.HDBOfficer> officerMap = officerDataManager.getAllOfficers().stream()
                .collect(java.util.stream.Collectors.toMap(
                    o -> o.getNric().trim(), 
                    o -> o
                ));
            
            System.out.println("DEBUG: Created manager map with " + managerMap.size() + " entries");
            System.out.println("DEBUG: Created officer map with " + officerMap.size() + " entries");
            
            // Step 2: Initialize ProjectDataManager and load projects
            System.out.println("DEBUG: Initializing ProjectDataManager...");
            ProjectDataManager projectDataManager = new ProjectDataManager(managerMap, officerMap);
            
            // Step 3: Initialize ProjectController
            System.out.println("DEBUG: Initializing ProjectController...");
            projectController = new ProjectController(projectDataManager);
            
            // Step 4: Get projects and create a clean map
            List<Project> projects = projectController.getAllProjects();
            System.out.println("DEBUG: ProjectController returned " + projects.size() + " projects");
            
            Map<String, Project> projectMap = new HashMap<>();
            for (Project project : projects) {
                if (project != null && project.getProjectName() != null) {
                    projectMap.put(project.getProjectName().trim(), project);
                    System.out.println("DEBUG: Added project to map: " + project.getProjectName());
                }
            }
            System.out.println("DEBUG: Created project map with " + projectMap.size() + " entries");
            
            // Step 5: Initialize ApplicationDataManager and controller
            System.out.println("DEBUG: Initializing ApplicationDataManager...");
            ApplicationDataManager applicationDataManager = new ApplicationDataManager(
                applicantDataManager, projectDataManager);
            
            System.out.println("DEBUG: Loading application data...");
            boolean applicationsLoaded = applicationDataManager.loadApplicationData();
            System.out.println("DEBUG: Applications loaded: " + applicationsLoaded);
            
            // Create EligibilityCheckerService
            EligibilityCheckerService eligibilityService = new EligibilityCheckerService();
            
            System.out.println("DEBUG: Initializing ApplicationController...");
            applicationController = new ApplicationController(
                applicationDataManager, applicantDataManager, eligibilityService);
            
            // Step 6: Create applicant map for EnquiryDataManager
            System.out.println("DEBUG: Creating applicant map for EnquiryDataManager...");
            Map<String, Applicant> applicantMap = new HashMap<>();
            for (Applicant applicant : applicants) {
                if (applicant != null && applicant.getNric() != null) {
                    applicantMap.put(applicant.getNric().trim(), applicant);
                    System.out.println("DEBUG: Added applicant to map: " + applicant.getName() + 
                                      ", NRIC: " + applicant.getNric());
                }
            }
            System.out.println("DEBUG: Created applicant map with " + applicantMap.size() + " entries");
            
            // Step 7: Initialize EnquiryDataManager
            System.out.println("DEBUG: Initializing EnquiryDataManager...");
            EnquiryDataManager enquiryDataManager = new EnquiryDataManager(applicantMap, projectMap);
            
            // Step 8: Initialize EnquiryController
            System.out.println("DEBUG: Initializing EnquiryController...");
            enquiryController = new EnquiryController(projectController, enquiryDataManager);
            
            System.out.println("DEBUG: Initialization complete");
        } catch (Exception e) {
            System.out.println("ERROR during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Boot up the application.
     */
    public void start() {
        try {
            // Display application title
            printAppTitle();

            // Render login choice menu
            renderLoginChoice();
        } catch (Exception e) {
            System.out.println("ERROR during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the App Title in a decorative box.
     */
    private void printAppTitle() {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ██████╗ ████████╗ ██████╗                               ║");
        System.out.println("║                     ██╔══██╗╚══██╔══╝██╔═══██╗                              ║");
        System.out.println("║                     ██████╔╝   ██║   ██║   ██║                              ║");
        System.out.println("║                     ██╔══██╗   ██║   ██║   ██║                              ║");
        System.out.println("║                     ██████╔╝   ██║   ╚██████╔╝                              ║");
        System.out.println("║                     ╚═════╝    ╚═╝    ╚═════╝                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("                  Build-To-Order Management System");
    }

    /**
     * Displays the Main Menu and prompts for login.
     */
    private void renderLoginChoice() {
        String input = "Main Menu > Please login with your NRIC and password";
        String space = String.format("%" + (99 - input.length()) + "s", "");
        
        System.out.println(
            "╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ " + input + space + "║");
        System.out.println(
            "╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        
        // Login process
        LoginView loginView = new LoginView(authController);
        User currentUser = loginView.displayAndGetUser();
    
        // Navigate to user-specific view
        if (currentUser != null) {
            MainMenuView mainMenuView = new MainMenuView(
                currentUser, 
                projectController, 
                applicationController, 
                enquiryController
            );
            mainMenuView.display();
        } else {
            System.out.println("Login failed or canceled. Exiting system.");
        }
    }

    /**
     * Main method to launch the application.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            App app = new App();
            app.start();
        } catch (Exception e) {
            System.out.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}