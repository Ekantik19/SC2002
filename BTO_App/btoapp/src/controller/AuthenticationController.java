// AuthController.java
package controller;

import controller.abstracts.AAuthenticationController;
import datamanager.ApplicantDataManager;
import datamanager.ApplicationDataManager;
import datamanager.ManagerDataManager;
import datamanager.OfficerDataManager;
import java.util.List;
import model.Applicant;
import model.Application;
import model.HDBManager;
import model.HDBOfficer;
import model.User;
import model.enums.ApplicationStatus;

/**
 * Authentication Controller for the BTO Management System.
 * Handles user authentication, session management, and user lookup.
 * 
 * This controller extends the abstract authentication controller 
 * and provides concrete implementation for user management specific 
 * to the BTO system.
 * 
 * @author Your Name
 * @version 1.0
 */
public class AuthenticationController extends AAuthenticationController {
    
    private ApplicantDataManager applicantDataManager;
    private OfficerDataManager officerDataManager;
    private ManagerDataManager managerDataManager;
    private ApplicationDataManager applicationDataManager;
    
    /**
     * Constructor for AuthController.
     * 
     * @param applicantDataManager The ApplicantDataManager to use
     * @param officerDataManager The OfficerDataManager to use
     * @param managerDataManager The ManagerDataManager to use
     * @param applicationDataManager The ApplicationDataManager to use
     */
    public AuthenticationController(ApplicantDataManager applicantDataManager,
        OfficerDataManager officerDataManager,
        ManagerDataManager managerDataManager,
        ApplicationDataManager applicationDataManager) {
        super();
        this.applicantDataManager = applicantDataManager;
        this.officerDataManager = officerDataManager;
        this.managerDataManager = managerDataManager;
        this.applicationDataManager = applicationDataManager;

        // IMPORTANT: Load users from data managers
        loadUsers();
    }
    
    /**
     * Loads users from data managers into the user map.
     */
    private void loadUsers() {
        // Clear existing users first
        userMap.clear();
        
        // Load managers
        for (HDBManager manager : managerDataManager.getAllManagers()) {
            addUser(manager);
        }
        
        // Load officers
        
        for (HDBOfficer officer : officerDataManager.getAllOfficers()) {
            addUser(officer);
        }
        
        // Load applicants
        for (Applicant applicant : applicantDataManager.readAllApplicants()) {
            addUser(applicant);
        }
    }
    
    /**
     * Authenticates a user and links their applications if they exist.
     * 
     * @param nric The NRIC of the user
     * @param password The password of the user
     * @return The authenticated user, or null if authentication fails
     */
    public User authenticateAndLinkApplications(String nric, String password) {
        
        // Get user from map
        User user = userMap.get(nric);
        
        // If user exists and credentials are valid
        if (user != null && user.authenticate(nric, password)) {
            System.out.println("Authentication successful for " + user.getName());
            
            // Link applications for applicants OR officers (who can also be applicants)
            if ((user instanceof Applicant || user instanceof HDBOfficer) && applicationDataManager != null) {
                linkApplicationsToUser(user);
            }
            
            return user;
        }
        
        System.out.println("Authentication failed for NRIC: " + nric);
        return null;
    }
    
    /**
     * Links applications to a user (either applicant or officer).
     * 
     * @param user The user to link applications to
     */
    private void linkApplicationsToUser(User user) {
        
        // Get applications for this user
        List<Application> applications = applicationDataManager.getApplicationsByApplicant(user.getNric());
        
        // First try to find an active application (PENDING or SUCCESSFUL)
        Application activeApplication = null;
        Application bookedApplication = null;
        
        for (Application app : applications) {
            
            if (app.isActive()) {
                activeApplication = app;
                break;
            } else if (app.getStatus() == ApplicationStatus.BOOKED) {
                bookedApplication = app;
            }
        }
        
        // If we found an active application, use that; otherwise use a booked one if available
        Application selectedApplication = (activeApplication != null) ? activeApplication : bookedApplication;
        
        if (selectedApplication != null) {
            
            // Set current application based on user type
            if (user instanceof Applicant) {
                Applicant applicant = (Applicant) user;
                applicant.setCurrentApplication(selectedApplication);
                
                // Update applicant in data manager
                applicantDataManager.updateApplicant(applicant);
            } 
            else if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                officer.setCurrentApplication(selectedApplication);
                
                // Update officer in data manager
                officerDataManager.updateOfficer(officer);
            }
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePassword(String nric, String oldPassword, String newPassword) {
        System.out.println("Attempting password change for NRIC: " + nric);
        
        // Validate input
        if (nric == null || oldPassword == null || newPassword == null) {
            System.out.println("Invalid input parameters for password change");
            return false;
        }
        
        // First, verify the user exists in the userMap
        User user = userMap.get(nric);
        if (user == null) {
            System.out.println("User not found for NRIC: " + nric);
            return false;
        }
        
        // Attempt to change password
        boolean changed = super.changePassword(nric, oldPassword, newPassword);
        
        if (changed) {
            
            try {
                // Update the appropriate data manager
                if (user instanceof HDBManager) {
                    HDBManager manager = (HDBManager) user;
                    boolean updateResult = managerDataManager.updateManager(manager);
                    
                    // Save manager data
                    boolean saveResult = managerDataManager.saveManagerData();
                    
                    return updateResult && saveResult;
                } else if (user instanceof HDBOfficer) {
                    HDBOfficer officer = (HDBOfficer) user;
                    boolean updateResult = officerDataManager.updateOfficer(officer);
                    
                    // Save officer data
                    boolean saveResult = officerDataManager.saveOfficerData();
                    
                    return updateResult && saveResult;
                } else if (user instanceof Applicant) {
                    applicantDataManager.updateApplicant((Applicant) user);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Error during password change process: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("Password change failed for NRIC: " + nric);
        }
        
        return false;
    }
}