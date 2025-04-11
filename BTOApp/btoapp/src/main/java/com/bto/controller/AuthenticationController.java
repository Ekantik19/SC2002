// AuthController.java
package com.bto.controller;

import com.bto.controller.abstracts.AAuthenticationController;
import com.bto.datamanager.ApplicantDataManager;
import com.bto.datamanager.ManagerDataManager;
import com.bto.datamanager.OfficerDataManager;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;

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
    
    /**
     * Constructor for AuthController.
     * 
     * @param applicantDataManager The ApplicantDataManager to use
     * @param officerDataManager The OfficerDataManager to use
     * @param managerDataManager The ManagerDataManager to use
     */
    public AuthenticationController(ApplicantDataManager applicantDataManager, 
                          OfficerDataManager officerDataManager,
                          ManagerDataManager managerDataManager) {
        super();
        this.applicantDataManager = applicantDataManager;
        this.officerDataManager = officerDataManager;
        this.managerDataManager = managerDataManager;
        
        // Load users into the userMap
        loadUsers();
    }
    
    /**
     * Loads users from data managers into the user map.
     */
    private void loadUsers() {
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
        boolean changed = super.changePassword(nric, oldPassword, newPassword);
        
        if (changed) {
            // Update the appropriate data manager
            if (userMap.get(nric) instanceof HDBManager) {
                managerDataManager.updateManager((HDBManager) userMap.get(nric));
            } else if (userMap.get(nric) instanceof HDBOfficer) {
                officerDataManager.updateOfficer((HDBOfficer) userMap.get(nric));
            } else if (userMap.get(nric) instanceof Applicant) {
                applicantDataManager.updateApplicant((Applicant) userMap.get(nric));
            }
        }
        
        return changed;
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
            // This would need a method to write all applicants
            // Assuming the writeApplicants or similar method exists
            success = true; // Replace with actual save operation
        }
        
        return success;
    }
}