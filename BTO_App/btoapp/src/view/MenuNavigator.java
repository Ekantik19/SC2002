package view;

import controller.ApplicationController;
import controller.EnquiryController;
import controller.ManagerController;
import controller.ProjectController;
import java.util.HashMap;
import java.util.Map;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.User;
import view.menu.*;

/**
 * Handles navigation between different views based on menu selections.
 */
public class MenuNavigator {
    private User currentUser;
    private ProjectController projectController;
    private ApplicationController applicationController;
    private EnquiryController enquiryController;
    private ManagerController managerController;
    
    private Map<Integer, MenuAction> applicantActions;
    private Map<Integer, MenuAction> officerActions;
    private Map<Integer, MenuAction> managerActions;
    
    /**
     * Constructor for MenuNavigator.
     * 
     * @param currentUser The currently logged-in user
     * @param projectController Controller for project operations
     * @param applicationController Controller for application operations
     * @param enquiryController Controller for enquiry operations
     */
    public MenuNavigator(User currentUser, 
                         ProjectController projectController,
                         ApplicationController applicationController,
                         EnquiryController enquiryController,
                         ManagerController managerController) {
        this.currentUser = currentUser;
        this.projectController = projectController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.managerController=managerController;
        
        // Initialize view instances
        ProjectView projectView = new ProjectView(currentUser, projectController, applicationController,managerController);
        //ApplicationView applicationView = new ApplicationView(currentUser, applicationController);
        ApplicationView applicationView = new ApplicationView(currentUser, applicationController, projectController);
        EnquiryView enquiryView = new EnquiryView(currentUser, enquiryController, projectController);
        PasswordChangeView passwordView = new PasswordChangeView(currentUser);
        
        // Initialize action maps
        initializeApplicantActions(projectView, applicationView, enquiryView, passwordView);
        initializeOfficerActions(projectView, applicationView, enquiryView, passwordView);
        initializeManagerActions(projectView, applicationView, enquiryView, passwordView);
    }
    
    /**
     * Initializes actions for Applicant users.
     */
    private void initializeApplicantActions(ProjectView projectView, 
                                          ApplicationView applicationView,
                                          EnquiryView enquiryView,
                                          PasswordChangeView passwordView) {
        applicantActions = new HashMap<>();
        
        applicantActions.put(1, () -> {
            projectView.display();
            return true;
        });
        
        applicantActions.put(2, () -> {
            applicationView.displayMyApplication();
            return true;
        });
        
        applicantActions.put(3, () -> {
            applicationView.displayWithdrawalRequest();
            return true;
        });
        
        applicantActions.put(4, () -> {
            enquiryView.displayCreateEnquiry();
            return true;
        });
        
        applicantActions.put(5, () -> {
            enquiryView.displayMyEnquiries();
            return true;
        });
        
        applicantActions.put(6, () -> {
            passwordView.display();
            return true;
        });
    }
    
    /**
     * Initializes actions for HDB Officer users.
     */
    private void initializeOfficerActions(ProjectView projectView, 
                                ApplicationView applicationView,
                                EnquiryView enquiryView,
                                PasswordChangeView passwordView) {
        officerActions = new HashMap<>();

        officerActions.put(1, () -> {
            projectView.display();
            return true;
        });

        officerActions.put(2, () -> {
            projectView.displayRegisterForProject();
            return true;
        });

        officerActions.put(3, () -> {
            projectView.displayRegistrationStatus();
            return true;
        });

        officerActions.put(4, () -> {
            projectView.displayAssignedProject();
            return true;
        });

        officerActions.put(5, () -> {
            applicationView.displayProcessBooking();
            return true;
        });

        officerActions.put(6, () -> {
            applicationView.displayGenerateReceipt();
            return true;
        });

        officerActions.put(7, () -> {
            enquiryView.displayProjectEnquiries();
            return true;
        });

        officerActions.put(8, () -> {
            HDBOfficer officer = (HDBOfficer) currentUser;
            if (officer.hasActiveApplication()) {
            applicationView.displayMyApplication();
            } else {
            System.out.println("\n!!! ERROR: You don't have an active application.");
            }
            return true;
        });

        officerActions.put(9, () -> {
            HDBOfficer officer = (HDBOfficer) currentUser;
            if (officer.hasActiveApplication()) {
            applicationView.displayWithdrawalRequest();
            } else {
            System.out.println("\n!!! ERROR: You don't have an active application.");
            }
            return true;
        });

        officerActions.put(10, () -> {
            passwordView.display();
            return true;
        });
    }
    
    /**
     * Initializes actions for HDB Manager users.
     */
    private void initializeManagerActions(ProjectView projectView, 
                                        ApplicationView applicationView,
                                        EnquiryView enquiryView,
                                        PasswordChangeView passwordView) {
        managerActions = new HashMap<>();
        
        managerActions.put(1, () -> {
            projectView.displayCreateProject();
            return true;
        });
        
        managerActions.put(2, () -> {
            projectView.displayAllProjects();
            return true;
        });
        
        managerActions.put(3, () -> {
            projectView.displayMyProjects();
            return true;
        });
        
        managerActions.put(4, () -> {
            projectView.displayUpdateProject();
            return true;
        });
        
        managerActions.put(5, () -> {
            projectView.displayToggleVisibility();
            return true;
        });
        
        managerActions.put(6, () -> {
            projectView.displayManageOfficers();
            return true;
        });
        
        managerActions.put(7, () -> {
            applicationView.displayManageApplications();
            return true;
        });
        
        managerActions.put(8, () -> {
            applicationView.displayManageWithdrawals();
            return true;
        });
        
        managerActions.put(9, () -> {
            HDBManager manager = (HDBManager) currentUser;
            ReportView reportView = new ReportView(manager, projectController);
            reportView.display();
            return true;
        });
        
        managerActions.put(10, () -> {
            enquiryView.displayAllEnquiries();
            return true;
        });
        
        managerActions.put(11, () -> {
            passwordView.display();
            return true;
        });
    }
    
    /**
     * Navigates to the selected menu option.
     * 
     * @param option The selected menu option
     * @return true to continue execution, false to exit
     */
    public boolean navigate(int option) {
        System.out.println("DEBUG: User role: " + currentUser.getRole());
        System.out.println("DEBUG: User class: " + currentUser.getClass().getSimpleName());
        
        if (currentUser instanceof HDBManager) {
            MenuAction action = managerActions.get(option);
            if (action != null) {
                return action.execute();
            }
        } else if (currentUser instanceof HDBOfficer) {
            MenuAction action = officerActions.get(option);
            if (action != null) {
                return action.execute();
            }
        } else if (currentUser instanceof Applicant) {
            MenuAction action = applicantActions.get(option);
            if (action != null) {
                return action.execute();
            }
        }
        
        System.out.println("\n!!! ERROR: Invalid option. Please try again.");
        return true;
    }
}