import java.util.List;
import java.util.Scanner;

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
import model.Applicant;
import model.User;
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
        // Initialize scanner
        scanner = new Scanner(System.in);

        // Initialize data managers
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
        authController = new AuthenticationController(
            applicantDataManager, 
            officerDataManager, 
            managerDataManager
        );
        
        // Initialize other controllers - these will be needed for MainMenuView
        ProjectDataManager projectDataManager = new ProjectDataManager(
            managerDataManager.getAllManagers().stream().collect(
                java.util.stream.Collectors.toMap(m -> m.getNric(), m -> m)),
            officerDataManager.getAllOfficers().stream().collect(
                java.util.stream.Collectors.toMap(o -> o.getNric(), o -> o))
        );
        
        ApplicationDataManager applicationDataManager = new ApplicationDataManager(
            applicantDataManager, projectDataManager);
        
        EnquiryDataManager enquiryDataManager = new EnquiryDataManager(
            applicants.stream().collect(
                java.util.stream.Collectors.toMap(a -> a.getNric(), a -> a)),
            projectDataManager.getAllProjects().stream().collect(
                java.util.stream.Collectors.toMap(p -> p.getProjectName(), p -> p))
        );
        
        projectController = new ProjectController(projectDataManager);
        applicationController = new ApplicationController(
            applicationDataManager, applicantDataManager, null); // Add your eligibility service
        enquiryController = new EnquiryController(projectController, enquiryDataManager);
    }

    /**
     * Boot up the application.
     */
    public void start() {
        // Display application title
        printAppTitle();

        // Render login choice menu
        renderLoginChoice();
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
     * Displays the Main Menu and prompts for login choice.
     */
    // private void renderLoginChoice() {
    //     String input = "Main Menu > Log in as?";
    //     String space = String.format("%" + (99 - input.length()) + "s", "");
        
    //     System.out.println(
    //         "╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
    //     System.out.println("║ " + input + space + "║");
    //     System.out.println(
    //         "╚════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        
    //     // Login process
    //     LoginView loginView = new LoginView(authController);
    //     User currentUser = loginView.displayAndGetUser();

    //     // Navigate to user-specific view
    //     if (currentUser != null) {
    //         MainMenuView mainMenuView = new MainMenuView(
    //             currentUser, 
    //             projectController, 
    //             applicationController, 
    //             enquiryController
    //         );
    //         mainMenuView.display();
    //     } else {
    //         System.out.println("Login failed. Exiting system.");
    //     }
    // }

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
        App app = new App();
        app.start();
    }
}