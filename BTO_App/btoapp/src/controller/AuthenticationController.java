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
        System.out.println("DEBUG: Loading users into authentication controller...");
        loadUsers();
        System.out.println("DEBUG: Loaded " + userMap.size() + " total users");
    }
    
    /**
     * Loads users from data managers into the user map.
     */
    private void loadUsers() {
        // Clear existing users first
        userMap.clear();
        
        // Load managers
        System.out.println("DEBUG: Loading managers into userMap...");
        for (HDBManager manager : managerDataManager.getAllManagers()) {
            System.out.println("DEBUG: Adding manager: " + manager.getName() + ", NRIC: " + manager.getNric());
            addUser(manager);
        }
        
        // Load officers
        System.out.println("DEBUG: Loading officers into userMap...");
        for (HDBOfficer officer : officerDataManager.getAllOfficers()) {
            System.out.println("DEBUG: Adding officer: " + officer.getName() + ", NRIC: " + officer.getNric());
            addUser(officer);
        }
        
        // Load applicants
        System.out.println("DEBUG: Loading applicants into userMap...");
        for (Applicant applicant : applicantDataManager.readAllApplicants()) {
            System.out.println("DEBUG: Adding applicant: " + applicant.getName() + ", NRIC: " + applicant.getNric());
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
        System.out.println("DEBUG: AuthController.authenticateAndLinkApplications - NRIC: " + nric);
        
        // Get user from map
        User user = userMap.get(nric);
        
        // If user exists and credentials are valid
        if (user != null && user.authenticate(nric, password)) {
            System.out.println("DEBUG: Authentication successful for " + user.getName());
            
            // Link applications for applicants OR officers (who can also be applicants)
            if ((user instanceof Applicant || user instanceof HDBOfficer) && applicationDataManager != null) {
                linkApplicationsToUser(user);
            }
            
            return user;
        }
        
        System.out.println("DEBUG: Authentication failed for NRIC: " + nric);
        return null;
    }
    
    /**
     * Links applications to a user (either applicant or officer).
     * 
     * @param user The user to link applications to
     */
    private void linkApplicationsToUser(User user) {
        System.out.println("DEBUG: Linking applications for user: " + user.getName());
        
        // Get applications for this user
        List<Application> applications = applicationDataManager.getApplicationsByApplicant(user.getNric());
        
        System.out.println("DEBUG: Found " + applications.size() + " applications for this user");
        
        // First try to find an active application (PENDING or SUCCESSFUL)
        Application activeApplication = null;
        Application bookedApplication = null;
        
        for (Application app : applications) {
            System.out.println("DEBUG: Checking application: " + app.getApplicationId() + 
                            ", Status: " + app.getStatus());
            
            if (app.isActive()) {
                activeApplication = app;
                System.out.println("DEBUG: Found active application: " + app.getApplicationId());
                break;
            } else if (app.getStatus() == ApplicationStatus.BOOKED) {
                bookedApplication = app;
                System.out.println("DEBUG: Found booked application: " + app.getApplicationId());
            }
        }
        
        // If we found an active application, use that; otherwise use a booked one if available
        Application selectedApplication = (activeApplication != null) ? activeApplication : bookedApplication;
        
        if (selectedApplication != null) {
            System.out.println("DEBUG: Setting application: " + selectedApplication.getApplicationId() + 
                            " for user: " + user.getName());
            
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
        
        // Log the result for debugging
        if (user instanceof Applicant) {
            Applicant applicant = (Applicant) user;
            if (applicant.getCurrentApplication() == null) {
                System.out.println("DEBUG: No application found for applicant " + applicant.getName());
            } else {
                System.out.println("DEBUG: Successfully linked application " + 
                                applicant.getCurrentApplication().getApplicationId() + 
                                " to applicant " + applicant.getName());
            }
        } 
        else if (user instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) user;
            if (officer.getCurrentApplication() == null) {
                System.out.println("DEBUG: No application found for officer " + officer.getName());
            } else {
                System.out.println("DEBUG: Successfully linked application " + 
                                officer.getCurrentApplication().getApplicationId() + 
                                " to officer " + officer.getName());
            }
        }
    }
    
    /**
     * Refreshes user data from data managers.
     * Call this method when user data has been updated externally.
     */
    public void refreshUsers() {
        userMap.clear();
        loadUsers();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePassword(String nric, String oldPassword, String newPassword) {
        System.out.println("DEBUG: Attempting password change for NRIC: " + nric);
        
        // Validate input
        if (nric == null || oldPassword == null || newPassword == null) {
            System.out.println("DEBUG: Invalid input parameters for password change");
            return false;
        }
        
        // First, verify the user exists in the userMap
        User user = userMap.get(nric);
        if (user == null) {
            System.out.println("DEBUG: User not found in userMap for NRIC: " + nric);
            return false;
        }
        
        // Attempt to change password
        boolean changed = super.changePassword(nric, oldPassword, newPassword);
        
        if (changed) {
            System.out.println("DEBUG: Password change successful for NRIC: " + nric);
            
            try {
                // Update the appropriate data manager
                if (user instanceof HDBManager) {
                    HDBManager manager = (HDBManager) user;
                    System.out.println("DEBUG: Updating manager in data manager");
                    boolean updateResult = managerDataManager.updateManager(manager);
                    System.out.println("DEBUG: Manager update result: " + updateResult);
                    
                    // Save manager data
                    boolean saveResult = managerDataManager.saveManagerData();
                    System.out.println("DEBUG: Manager save result: " + saveResult);
                    
                    return updateResult && saveResult;
                } else if (user instanceof HDBOfficer) {
                    HDBOfficer officer = (HDBOfficer) user;
                    System.out.println("DEBUG: Updating officer in data manager");
                    boolean updateResult = officerDataManager.updateOfficer(officer);
                    System.out.println("DEBUG: Officer update result: " + updateResult);
                    
                    // Save officer data
                    boolean saveResult = officerDataManager.saveOfficerData();
                    System.out.println("DEBUG: Officer save result: " + saveResult);
                    
                    return updateResult && saveResult;
                } else if (user instanceof Applicant) {
                    System.out.println("DEBUG: Updating applicant in data manager");
                    applicantDataManager.updateApplicant((Applicant) user);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error during password change process: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("DEBUG: Password change failed for NRIC: " + nric);
        }
        
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadUserData(String filePath) {
        boolean success = false;
        
        // For managers
        if (filePath.contains("Manager")) {
            managerDataManager.setFilePath(filePath);
            success = managerDataManager.loadManagerData();
        }
        // For officers
        else if (filePath.contains("Officer")) {
            success = officerDataManager.loadOfficerData();
        }
        // For applicants
        else if (filePath.contains("Applicant")) {
            applicantDataManager.setFilePath(filePath);
            success = (applicantDataManager.readAllApplicants() != null);
        }
        
        if (success) {
            refreshUsers();
        }
        
        return success;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveUserData(String filePath) {
        boolean success = false;
        
        // For managers
        if (filePath.contains("Manager")) {
            managerDataManager.setFilePath(filePath);
            success = managerDataManager.saveManagerData();
        }
        // For officers
        else if (filePath.contains("Officer")) {
            success = officerDataManager.saveOfficerData();
        }
        // For applicants
        else if (filePath.contains("Applicant")) {
            applicantDataManager.setFilePath(filePath);
            success = applicantDataManager.writeApplicants(applicantDataManager.readAllApplicants());
        }
        
        return success;
    }
}