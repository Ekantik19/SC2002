import controller.*;
import datamanager.*;
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
    private ManagerController managerController;
    private BookingController bookingController;

    /**
     * Constructor initializes application components.
     */
    public App() {
        try {
            scanner = new Scanner(System.in);

            // Initialize data managers
            ApplicantDataManager applicantDataManager = new ApplicantDataManager();
            OfficerDataManager officerDataManager = new OfficerDataManager();
            ManagerDataManager managerDataManager = new ManagerDataManager();

            // Debug: Check if files exist and load data
            List<Applicant> applicants = applicantDataManager.readAllApplicants();
            boolean officersLoaded = officerDataManager.loadOfficerData();
            boolean managersLoaded = managerDataManager.loadManagerData();

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

            ProjectDataManager projectDataManager = new ProjectDataManager(managerMap, officerMap);

            projectController = new ProjectController(projectDataManager);

            List<Project> projects = projectController.getAllProjects();

            Map<String, Project> projectMap = new HashMap<>();
            for (Project project : projects) {
                if (project != null && project.getProjectName() != null) {
                    projectMap.put(project.getProjectName().trim(), project);
                }
            }

            ApplicationDataManager applicationDataManager = new ApplicationDataManager(
                applicantDataManager, projectDataManager, officerDataManager);

            boolean applicationsLoaded = applicationDataManager.loadApplicationData();

            authController = new AuthenticationController(
                applicantDataManager, 
                officerDataManager, 
                managerDataManager,
                applicationDataManager  
            );

            // Create EligibilityCheckerService
            EligibilityCheckerService eligibilityService = new EligibilityCheckerService();

            applicationController = new ApplicationController(
                applicationDataManager, applicantDataManager, eligibilityService);

            bookingController = new BookingController(applicationDataManager, projectDataManager);

            Map<String, Applicant> applicantMap = new HashMap<>();
            for (Applicant applicant : applicants) {
                if (applicant != null && applicant.getNric() != null) {
                    applicantMap.put(applicant.getNric().trim(), applicant);
                }
            }

            EnquiryDataManager enquiryDataManager = new EnquiryDataManager(applicantMap, projectMap);

            enquiryController = new EnquiryController(projectController, enquiryDataManager);

            managerController = new ManagerController(
                managerDataManager, 
                projectDataManager,
                officerDataManager
            );
           
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
    
            boolean exitSystem = false;
            while (!exitSystem) {
                // Render login choice menu
                User currentUser = renderLoginChoice();
                
                if (currentUser == null) {
                    // User canceled login or login failed
                    System.out.println("Would you like to exit the system? (Y/N)");
                    Scanner scanner = new Scanner(System.in);
                    String response = scanner.nextLine();
                    if (response.equalsIgnoreCase("Y")) {
                        exitSystem = true;
                    }
                }
            }
            
            System.out.println("Thank you for using the BTO Management System. Goodbye!");
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
    private User renderLoginChoice() {
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
                enquiryController,
                managerController,
                authController,
                bookingController
            );
            mainMenuView.display();
        }
        
        return currentUser;
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