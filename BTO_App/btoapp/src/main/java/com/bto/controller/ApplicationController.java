package com.bto.controller;

import java.util.ArrayList;
import java.util.List;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;
import com.bto.model.User;

/**
 * Controller for handling application-related operations.
 */
public class ApplicationController {
    private DataManager dataManager;
    private AuthController authController;
    private ProjectController projectController;
    
    /**
     * Constructor for ApplicationController.
     * 
     * @param dataManager The data manager to be used
     * @param authController The auth controller to be used
     * @param projectController The project controller to be used
     */
    public ApplicationController(DataManager dataManager, AuthController authController, ProjectController projectController) {
        this.dataManager = dataManager;
        this.authController = authController;
        this.projectController = projectController;
    }
    
    /**
     * Apply for a project as an applicant.
     * 
     * @param applicant The applicant applying for the project
     * @param projectName The name of the project to apply for
     * @param flatType The type of flat to apply for
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(Applicant applicant, String projectName, String flatType) {
        // Check if applicant already has an existing application
        Application existingApplication = getApplication(applicant);
        if (existingApplication != null) {
            return false; // Already has an application
        }
        
        // Find the project by name
        Project project = projectController.getProjectByName(projectName);
        
        // Project must exist and be visible
        if (project == null || !project.isVisible()) {
            return false;
        }
        
        // Validate applicant eligibility based on age and marital status
        boolean isMarried = "Married".equalsIgnoreCase(applicant.getMaritalStatus());
        
        // Singles 35+ can only apply for 2-Room
        if (!isMarried) {
            if (applicant.getAge() < 35) {
                return false; // Singles below 35 cannot apply
            }
            
            // Check if project has 2-Room flats and applicant wants 2-Room
            if (!project.hasFlatType("2-Room") || !"2-Room".equalsIgnoreCase(flatType)) {
                return false;
            }
        } 
        // Married applicants 21+ can apply for 2-Room or 3-Room
        else {
            if (applicant.getAge() < 21) {
                return false; // Married below 21 cannot apply
            }
            
            // Check if project has the requested flat type
            if (!project.hasFlatType(flatType)) {
                return false;
            }
        }
        
        // Create the application
        Application application = new Application(applicant, project);
        application.setFlatType(flatType);
        
        // Add to data manager
        dataManager.addApplication(application);
        
        // Set current application for applicant
        applicant.setCurrentApplication(application);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Apply for a project as an HDB Officer (in applicant capacity).
     * 
     * @param officer The HDB Officer applying as an applicant
     * @param projectName The name of the project to apply for
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(HDBOfficer officer, String projectName) {
        // Find the project by name
        Project project = projectController.getProjectByName(projectName);
        
        if (project == null) {
            return false;
        }
        
        // Officers cannot apply for projects they are handling
        if (officer.getAssignedProject() != null && 
            officer.getAssignedProject().getProjectName().equals(projectName)) {
            return false;
        }
        
        // Create an Applicant from the officer's information
        Applicant applicantView = new Applicant(
            officer.getUserID(),
            "password", // Using default password as shown in DataManager
            officer.getAge(),
            officer.getMaritalStatus()
        );
        
        // Create and process application
        Application application = new Application(applicantView, project);
        
        // Determine appropriate flat type based on marital status and age
        String flatType;
        if ("Married".equalsIgnoreCase(officer.getMaritalStatus())) {
            // Married officers can apply for any flat type, defaulting to 3-Room if available
            flatType = project.hasFlatType("3-Room") ? "3-Room" : "2-Room";
        } else {
            // Single officers can only apply for 2-Room
            if (officer.getAge() < 35 || !project.hasFlatType("2-Room")) {
                return false;
            }
            flatType = "2-Room";
        }
        
        application.setFlatType(flatType);
        project.addApplication(application);
        dataManager.addApplication(application);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Get the application for a specific applicant.
     * 
     * @param applicant The applicant to get the application for
     * @return The application if found, null otherwise
     */
    public Application getApplication(Applicant applicant) {
        List<Application> allApplications = dataManager.getAllApplications();
        
        for (Application app : allApplications) {
            if (app.getApplicant().getUserID().equals(applicant.getUserID())) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Request withdrawal of the current application.
     * 
     * @return true if withdrawal request is submitted, false otherwise
     */
    public boolean requestWithdrawal() {
        User currentUser = authController.getCurrentUser();
        
        // Only applicants can request withdrawal
        if (!(currentUser instanceof Applicant)) {
            return false;
        }
        
        Applicant applicant = (Applicant) currentUser;
        return requestWithdrawal(applicant);
    }
    
    /**
     * Request withdrawal of an application for a specific applicant.
     * 
     * @param applicant The applicant requesting withdrawal
     * @return true if withdrawal request is successful, false otherwise
     */
    public boolean requestWithdrawal(Applicant applicant) {
        // Find the application for this applicant
        Application application = getApplication(applicant);
        
        if (application == null) {
            return false;
        }
        
        // Check application status - withdrawal only allowed for certain statuses
        String status = application.getStatus();
        if (!status.equals(Application.STATUS_PENDING) && 
            !status.equals(Application.STATUS_SUCCESSFUL) &&
            !status.equals(Application.STATUS_BOOKED)) {
            return false;
        }
        
        // Set withdrawal request flag
        application.setWithdrawalRequested(true);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Approve a withdrawal request for an application.
     * 
     * @param manager The HDB Manager approving the withdrawal
     * @param userID The ID of the user whose application to withdraw
     * @return true if withdrawal approval is successful, false otherwise
     */
    public boolean approveWithdrawal(HDBManager manager, String userID) {
        // Find the application by user ID
        Application application = findApplicationByUserID(userID);
        
        if (application == null) {
            return false;
        }
        
        // Check if withdrawal was actually requested
        if (!application.isWithdrawalRequested()) {
            return false;
        }
        
        // If application was booked, return units to the project
        if (Application.STATUS_BOOKED.equals(application.getStatus())) {
            Project project = application.getProject();
            String flatType = application.getFlatTypeBooked();
            
            if (flatType != null) {
                project.incrementUnits(flatType);
            }
        }
        
        // Remove the application from the project
        application.getProject().getApplications().remove(application);
        
        // Remove from data manager's applications list
        dataManager.getAllApplications().remove(application);
        
        // Clear the applicant's current application
        Applicant applicant = (Applicant) application.getApplicant();
        if (applicant != null) {
            applicant.setCurrentApplication(null);
        }
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Process an application (approve, reject, or process withdrawal request).
     * 
     * @param userID The ID of the user whose application to process
     * @param approved Whether to approve or reject the application/withdrawal
     * @return true if processing is successful, false otherwise
     */
    public boolean processApplication(String userID, boolean approved) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can process applications
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the application by user ID
        Application application = findApplicationByUserID(userID);
        if (application == null) {
            return false;
        }
        
        // Check if this is a withdrawal request
        if (application.isWithdrawalRequested()) {
            if (approved) {
                return approveWithdrawal(manager, userID);
            } else {
                // Reject withdrawal request - just remove the withdrawal flag
                application.setWithdrawalRequested(false);
                dataManager.saveData();
                return true;
            }
        }
        
        // Regular application processing
        if (approved) {
            return approveApplication(manager, userID);
        } else {
            // Reject application
            application.updateStatus(Application.STATUS_UNSUCCESSFUL);
            dataManager.saveData();
            return true;
        }
    }
    
    /**
     * Approve an application for a BTO project.
     * 
     * @param manager The HDB Manager approving the application
     * @param userID The ID of the user whose application to approve
     * @return true if approval is successful, false otherwise
     */
    public boolean approveApplication(HDBManager manager, String userID) {
        // Find the application by user ID
        Application application = findApplicationByUserID(userID);
        
        if (application == null) {
            return false;
        }
        
        // Check if the project has available units
        Project project = application.getProject();
        
        // Determine flat type eligibility
        User applicant = application.getApplicant();
        String flatType = application.getFlatType();
        
        if (applicant instanceof Applicant) {
            Applicant applicantDetails = (Applicant) applicant;
            
            // Check flat type availability
            if (!project.hasFlatType(flatType)) {
                return false;
            }
        }
        
        // Update application status to Successful
        application.updateStatus(Application.STATUS_SUCCESSFUL);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Book a flat for an applicant.
     * 
     * @param officer The HDB Officer processing the booking
     * @param applicantID The ID of the applicant
     * @param projectName The name of the project
     * @param flatType The type of flat to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(HDBOfficer officer, String applicantID, String projectName, String flatType) {
        // Verify the officer is approved for a project
        if (officer.getAssignedProject() == null || 
            !officer.getAssignedProject().getProjectName().equals(projectName) || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            return false;
        }
        
        // Find the application by applicant ID and project name
        Application application = null;
        
        for (Application app : dataManager.getAllApplications()) {
            if (app.getApplicant().getUserID().equals(applicantID) && 
                app.getProject().getProjectName().equals(projectName)) {
                application = app;
                break;
            }
        }
        
        if (application == null) {
            return false;
        }
        
        // Check if application status is successful
        if (!Application.STATUS_SUCCESSFUL.equals(application.getStatus())) {
            return false;
        }
        
        // Check if the flat type is valid for the project
        Project project = application.getProject();
        if (!project.hasFlatType(flatType) || project.getRemainingUnits(flatType) <= 0) {
            return false;
        }
        
        // Book the flat
        application.setFlatTypeBooked(flatType);
        
        // Decrement available units
        project.decrementUnits(flatType);
        
        // Update application status to booked
        application.updateStatus(Application.STATUS_BOOKED);
        
        // Update applicant's booked flat information
        Applicant applicant = (Applicant) application.getApplicant();
        applicant.setBookedFlatType(flatType);
        applicant.setBookedProject(project.getProjectName());
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Generate a receipt for a flat booking.
     * 
     * @param officer The HDB Officer generating the receipt
     * @param applicantID The ID of the applicant
     * @return The generated receipt as a string, or null if generation fails
     */
    public String generateReceipt(HDBOfficer officer, String applicantID) {
        // Verify the officer is approved for a project
        if (officer.getAssignedProject() == null || 
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            return null;
        }
        
        // Find the application for this applicant
        Application application = null;
        for (Application app : dataManager.getAllApplications()) {
            if (app.getApplicant().getUserID().equals(applicantID)) {
                application = app;
                break;
            }
        }
        
        if (application == null || !Application.STATUS_BOOKED.equals(application.getStatus())) {
            return null;
        }
        
        // Generate a Receipt object
        Receipt receipt = new Receipt(application, officer);
        
        // Call the printReceipt method to get a string representation
        return receipt.printReceipt();
    }
    
    /**
     * Get the detailed profile of an applicant, including booking information.
     * 
     * @param applicant The applicant whose profile to retrieve
     * @return The applicant with complete profile information
     */
    public Applicant getApplicantProfile(Applicant applicant) {
        // Find the application for this applicant to get booking details
        Application application = getApplication(applicant);
        
        if (application != null && Application.STATUS_BOOKED.equals(application.getStatus())) {
            // If application exists and is booked, update the applicant's booked flat type
            applicant.setBookedFlatType(application.getFlatTypeBooked());
            applicant.setBookedProject(application.getProject().getProjectName());
        }
        
        return applicant;
    }
    
    /**
     * Find an application by the user ID of the applicant.
     * 
     * @param userID The ID of the user whose application to find
     * @return The application if found, null otherwise
     */
    private Application findApplicationByUserID(String userID) {
        List<Application> allApplications = dataManager.getAllApplications();
        
        for (Application application : allApplications) {
            if (application.getApplicant().getUserID().equals(userID)) {
                return application;
            }
        }
        
        return null;
    }
    
    /**
     * Directly withdraw an application for testing purposes only.
     * Bypasses the request-approval process; not for main system use.
     * 
     * @param applicant The applicant whose application to withdraw
     * @return true if withdrawn successfully, false otherwise
     */
    public boolean withdrawApplication(Applicant applicant) {
        Application application = getApplication(applicant);
        if (application == null) {
            return false;
        }

        // If booked, restore flat unit
        if (Application.STATUS_BOOKED.equals(application.getStatus()) && application.getFlatTypeBooked() != null) {
            Project project = application.getProject();
            String flatType = application.getFlatTypeBooked();
            project.incrementUnits(flatType);
        }

        // Remove application
        application.getProject().getApplications().remove(application);
        dataManager.getAllApplications().remove(application);
        applicant.setCurrentApplication(null);

        // Save changes
        dataManager.saveData();
        return true;
    }
    
    /**
     * Withdraw an application for an HDB Officer.
     * 
     * @param officer The HDB Officer whose application to withdraw
     * @return true if withdrawal is successful, false otherwise
     */
    public boolean withdrawApplication(HDBOfficer officer) {
        // Find the application by officer ID
        Application application = null;
        for (Application app : dataManager.getAllApplications()) {
            if (app.getApplicant().getUserID().equals(officer.getUserID())) {
                application = app;
                break;
            }
        }
        
        if (application == null) {
            return false;
        }
        
        // If booked, restore flat unit
        if (Application.STATUS_BOOKED.equals(application.getStatus()) && application.getFlatTypeBooked() != null) {
            Project project = application.getProject();
            String flatType = application.getFlatTypeBooked();
            project.incrementUnits(flatType);
        }
        
        // Remove application
        application.getProject().getApplications().remove(application);
        dataManager.getAllApplications().remove(application);
        
        // Save changes
        dataManager.saveData();
        
        return true;
    }
    
    /**
     * Get a list of applications with pending status for a list of projects.
     * 
     * @param projects The list of projects to check
     * @return A list of pending applications
     */
    public List<Application> getPendingApplications(List<Project> projects) {
        List<Application> pendingApplications = new ArrayList<>();
        
        for (Project project : projects) {
            for (Application application : project.getApplications()) {
                if (Application.STATUS_PENDING.equals(application.getStatus())) {
                    pendingApplications.add(application);
                }
            }
        }
        
        return pendingApplications;
    }

    /**
     * Get a list of applications with withdrawal requests for a list of projects.
     * 
     * @param projects The list of projects to check
     * @return A list of applications with withdrawal requests
     */
    public List<Application> getWithdrawalRequests(List<Project> projects) {
        List<Application> withdrawalRequests = new ArrayList<>();
        
        for (Project project : projects) {
            for (Application application : project.getApplications()) {
                if (application.isWithdrawalRequested()) {
                    withdrawalRequests.add(application);
                }
            }
        }
        
        return withdrawalRequests;
    }
}