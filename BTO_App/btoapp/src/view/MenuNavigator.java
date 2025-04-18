package view;

import controller.*;
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
    private AuthenticationController authController;
    private BookingController bookingController;
    
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
                         ManagerController managerController,
                         AuthenticationController authController,
                         BookingController bookingController) {
        this.currentUser = currentUser;
        this.projectController = projectController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.managerController=managerController;
        this.authController=authController;
        this.bookingController=bookingController;
        
        // Initialize view instances
        ProjectView projectView = new ProjectView(currentUser, projectController, applicationController,managerController);
        //ApplicationView applicationView = new ApplicationView(currentUser, applicationController);
        ApplicationView applicationView = new ApplicationView(currentUser, applicationController, projectController, bookingController);
        EnquiryView enquiryView = new EnquiryView(currentUser, enquiryController, projectController);
        PasswordChangeView passwordView = new PasswordChangeView(currentUser,authController);
        
        // Initialize action maps
        if (currentUser instanceof HDBManager) {
            initializeManagerActions(projectView, applicationView, enquiryView, passwordView);
            officerActions = new HashMap<>(); // Prevent null pointer
            applicantActions = new HashMap<>(); // Prevent null pointer
        } else if (currentUser instanceof HDBOfficer) {
            initializeOfficerActions(projectView, applicationView, enquiryView, passwordView);
            managerActions = new HashMap<>(); // Prevent null pointer
            applicantActions = new HashMap<>(); // Prevent null pointer
        } else if (currentUser instanceof Applicant) {
            initializeApplicantActions(projectView, applicationView, enquiryView, passwordView);
            managerActions = new HashMap<>(); // Prevent null pointer
            officerActions = new HashMap<>(); // Prevent null pointer
        } else {
            throw new IllegalArgumentException("Unknown user type: " + currentUser.getClass().getSimpleName());
        }
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
        HDBOfficer officer = (HDBOfficer) currentUser;

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

        // Change password option is now consistently at option 8
        officerActions.put(8, () -> {
            passwordView.display();
            return true;
        });

        // Add application-specific options if the officer has any application
        // NOTE: Changed from hasActiveApplication() to check for any application
        if (officer.getCurrentApplication() != null) {
            officerActions.put(9, () -> {
            applicationView.displayMyApplication();
            return true;
        });

            officerActions.put(10, () -> {
            applicationView.displayWithdrawalRequest();
            return true;
        });
    }
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
        
        MenuAction action = null;
        
        if (currentUser instanceof HDBOfficer) {
            // For officers, always use the mapped action at the given option
            action = officerActions.get(option);
        } else if (currentUser instanceof HDBManager) {
            action = managerActions.get(option);
        } else if (currentUser instanceof Applicant) {
            action = applicantActions.get(option);
        }
        
        if (action != null) {
            return action.execute();
        }
        
        System.out.println("\n!!! ERROR: Invalid option. Please try again.");
        return true;
    }
}