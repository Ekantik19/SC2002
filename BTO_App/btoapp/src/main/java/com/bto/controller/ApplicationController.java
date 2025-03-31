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
    
    // /**
    //  * Constructor for ApplicationController.
    //  */
    // public ApplicationController() {
    //     this.dataManager = DataManager.getInstance();
    //     this.authController = new AuthController();
    // }

     /**
     * Constructor for ApplicationController.
     * 
     * @param dataManager The data manager to be used
     * @param authController The auth controller to be used
     */
    public ApplicationController(DataManager dataManager, AuthController authController) {
        this.dataManager = dataManager;
        this.authController = authController;
    }
    
    /**
     * Apply for a BTO project.
     * 
     * @param projectID The ID of the project to apply for
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(int projectID) {
        User currentUser = authController.getCurrentUser();
        
        // Only applicants can apply for projects
        if (!(currentUser instanceof Applicant)) {
            return false;
        }
        
        Applicant applicant = (Applicant) currentUser;
        
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        return applicant.applyForProject(project);
    }
    
    /**
     * View the current application status.
     * 
     * @return The status of the current application, or "No Application" if none exists
     */
    public String viewApplicationStatus() {
        User currentUser = authController.getCurrentUser();
        
        // Only applicants can view application status
        if (!(currentUser instanceof Applicant)) {
            return "Not an applicant";
        }
        
        Applicant applicant = (Applicant) currentUser;
        return applicant.viewApplicationStatus();
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
        return applicant.requestWithdrawal();
    }
    
    /**
     * Approve or reject an application.
     * 
     * @param applicationID The ID of the application
     * @param approved Whether to approve or reject the application
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processApplication(int applicationID, boolean approved) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can process applications
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the application by ID
        Application application = findApplicationByID(applicationID);
        if (application == null) {
            return false;
        }
        
        return manager.processApplication(application, approved);
    }
    
    /**
     * Approve or reject a withdrawal request.
     * 
     * @param applicationID The ID of the application
     * @param approved Whether to approve or reject the withdrawal
     * @return true if approval/rejection is successful, false otherwise
     */
    public boolean processWithdrawalRequest(int applicationID, boolean approved) {
        User currentUser = authController.getCurrentUser();
        
        // Only managers can process withdrawal requests
        if (!(currentUser instanceof HDBManager)) {
            return false;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        // Find the application by ID
        Application application = findApplicationByID(applicationID);
        if (application == null) {
            return false;
        }
        
        return manager.processWithdrawalRequest(application, approved);
    }
    
    /**
     * Book a flat for an application.
     * 
     * @param userID The ID of the applicant
     * @param flatType The type of flat to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(String userID, String flatType) {
        User currentUser = authController.getCurrentUser();
        
        // Only officers can book flats
        if (!(currentUser instanceof HDBOfficer)) {
            return false;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Retrieve the application
        Application application = officer.retrieveApplication(userID);
        if (application == null) {
            return false;
        }
        
        // Book the flat
        boolean booked = application.bookFlat(flatType);
        if (booked) {
            officer.updateApplicationStatus(application, Application.STATUS_BOOKED);
            dataManager.saveData();
        }
        
        return booked;
    }
    
    /**
     * Generate a receipt for a flat booking.
     * 
     * @param userID The ID of the applicant
     * @return The generated receipt, or null if generation fails
     */
    public Receipt generateReceipt(String userID) {
        User currentUser = authController.getCurrentUser();
        
        // Only officers can generate receipts
        if (!(currentUser instanceof HDBOfficer)) {
            return null;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Retrieve the application
        Application application = officer.retrieveApplication(userID);
        if (application == null) {
            return null;
        }
        
        return officer.generateReceipt(application);
    }
    
    /**
     * Find a project by its ID.
     * 
     * @param projectID The ID of the project to find
     * @return The project if found, null otherwise
     */
    private Project findProjectByID(int projectID) {
        List<Project> allProjects = dataManager.getAllProjects();
        
        for (Project project : allProjects) {
            if (project.getProjectID() == projectID) {
                return project;
            }
        }
        
        return null;
    }
    
    /**
     * Find an application by its ID.
     * 
     * @param applicationID The ID of the application to find
     * @return The application if found, null otherwise
     */
    private Application findApplicationByID(int applicationID) {
        List<Application> allApplications = dataManager.getAllApplications();
        
        for (Application application : allApplications) {
            if (application.getApplicationID() == applicationID) {
                return application;
            }
        }
        
        return null;
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

    /**
 * Get applications with successful status for a project.
 * 
 * @param project The project to get successful applications for
 * @return List of successful applications
 */
public List<Application> getSuccessfulApplications(Project project) {
    List<Application> successfulApplications = new ArrayList<>();
    
    for (Application app : project.getApplications()) {
        if (Application.STATUS_SUCCESSFUL.equals(app.getStatus())) {
            successfulApplications.add(app);
        }
    }
    
    return successfulApplications;
}

/**
 * Get flat types that an applicant is eligible for.
 * 
 * @param applicant The applicant to check eligibility for
 * @return List of flat types the applicant is eligible for
 */
public List<String> getEligibleFlatTypes(Applicant applicant) {
    List<String> eligibleTypes = new ArrayList<>();
    
    // Singles aged 35 and above can only apply for 2-Room
    if (!"Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 35) {
        eligibleTypes.add("2-Room");
    }
    // Married aged 21 and above can apply for any flat type
    else if ("Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 21) {
        eligibleTypes.add("2-Room");
        eligibleTypes.add("3-Room");
    }
    
    return eligibleTypes;
}

/**
 * Book a flat for an application.
 * 
 * @param app The application to book a flat for
 * @param flatType The type of flat to book
 * @param officer The HDB Officer processing the booking
 * @return true if booking is successful, false otherwise
 */
public boolean bookFlat(Application app, String flatType, HDBOfficer officer) {
    // Check if application status is successful
    if (!Application.STATUS_SUCCESSFUL.equals(app.getStatus())) {
        return false;
    }
    
    // Check if officer is assigned to the project
    if (officer.getAssignedProject() == null || 
        officer.getAssignedProject().getProjectID() != app.getProject().getProjectID()) {
        return false;
    }
    
    // Book the flat
    boolean booked = app.bookFlat(flatType);
    if (booked) {
        officer.updateApplicationStatus(app, Application.STATUS_BOOKED);
        dataManager.saveData();
    }
    
    return booked;
}

/**
 * Get applications with booked status for a project.
 * 
 * @param project The project to get booked applications for
 * @return List of booked applications
 */
public List<Application> getBookedApplications(Project project) {
    List<Application> bookedApplications = new ArrayList<>();
    
    for (Application app : project.getApplications()) {
        if (Application.STATUS_BOOKED.equals(app.getStatus())) {
            bookedApplications.add(app);
        }
    }
    
    return bookedApplications;
}

    // /**
    //  * Apply for a project as an HDB Officer (in applicant capacity).
    //  * 
    //  * @param projectID The ID of the project to apply for
    //  * @param officer The HDB Officer applying as an applicant
    //  * @return true if application is successful, false otherwise
    //  */
    // public boolean applyForProject(int projectID, HDBOfficer officer) {
    //     // Find the project by ID
    //     Project project = findProjectByID(projectID);
    //     if (project == null) {
    //         return false;
    //     }
        
    //     // Officers cannot apply for projects they are handling
    //     if (officer.getAssignedProject() != null && 
    //         officer.getAssignedProject().getProjectID() == projectID) {
    //         return false;
    //     }
        
    //     // Create and process application
    //     Application application = new Application(officer, project);
    //     project.addApplication(application);
    //     dataManager.saveApplication(application);
        
    //     return true;
    // }

    /**
     * Apply for a project as an HDB Officer (in applicant capacity).
     * 
     * @param projectID The ID of the project to apply for
     * @param officer The HDB Officer applying as an applicant
     * @return true if application is successful, false otherwise
     */
    public boolean applyForProject(int projectID, HDBOfficer officer) {
        // Find the project by ID
        Project project = findProjectByID(projectID);
        if (project == null) {
            return false;
        }
        
        // Officers cannot apply for projects they are handling
        if (officer.getAssignedProject() != null && 
            officer.getAssignedProject().getProjectID() == projectID) {
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
        project.addApplication(application);
        dataManager.addApplication(application); // Changed from saveApplication to addApplication
        
        return true;
    }

}