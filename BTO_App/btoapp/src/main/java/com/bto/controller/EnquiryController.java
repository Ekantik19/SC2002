package com.bto.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bto.model.Applicant;
import com.bto.model.DataManager;
import com.bto.model.Enquiry;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.User;

/**
 * Controller for managing enquiries in the BTO Management System.
 * Handles the business logic for creating, viewing, updating, and deleting enquiries.
 */
public class EnquiryController {
    private DataManager dataManager;
    
    /**
     * Constructor for EnquiryController.
     * 
     * @param dataManager The data manager for persistent storage
     */
    public EnquiryController(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    /**
     * Create a new enquiry.
     * 
     * @param user The user submitting the enquiry
     * @param project The project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return True if the enquiry was created successfully, false otherwise
     */
    public boolean createEnquiry(User user, Project project, String enquiryText) {
        try {
            Enquiry enquiry = new Enquiry(user, project, enquiryText);
            dataManager.saveEnquiry(enquiry);
            
            // If user is an applicant, add to their enquiries list
            if (user instanceof Applicant) {
                ((Applicant) user).addEnquiry(enquiry);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error creating enquiry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Submit an enquiry - alias for createEnquiry to maintain compatibility with tests.
     * 
     * @param user The user submitting the enquiry
     * @param project The project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return 1 if successful (to represent success), -1 if failed
     */
    public int submitEnquiry(User user, Project project, String enquiryText) {
        boolean success = createEnquiry(user, project, enquiryText);
        return success ? 1 : -1;  // Return 1 for success, -1 for failure
    }
    
    /**
     * Get all enquiries created by a specific user.
     * 
     * @param user The user whose enquiries to retrieve
     * @return A list of enquiries created by the user
     */
    public List<Enquiry> getUserEnquiries(User user) {
        List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
        
        return allEnquiries.stream()
                .filter(e -> e.getUser().getUserID().equals(user.getUserID()))
                .collect(Collectors.toList());
    }
    
    /**
     * Edit an enquiry for a specific applicant.
     * Only the applicant who created the enquiry can edit it, and only if it hasn't been responded to.
     * 
     * @param applicant The applicant editing the enquiry
     * @param projectName The name of the project the enquiry is about
     * @param oldText The current text of the enquiry (to identify it)
     * @param newText The new text for the enquiry
     * @return True if the enquiry was edited successfully, false otherwise
     */
    public boolean editEnquiry(Applicant applicant, String projectName, String oldText, String newText) {
        // Get all enquiries for this applicant
        List<Enquiry> userEnquiries = getUserEnquiries(applicant);
        
        // Find the specific enquiry by project and text
        Enquiry enquiryToEdit = userEnquiries.stream()
            .filter(e -> e.getProject().getProjectName().equals(projectName) && e.getEnquiryText().equals(oldText))
            .findFirst()
            .orElse(null);
        
        if (enquiryToEdit == null) {
            return false;
        }
        
        // Only allow editing if the enquiry has no response yet
        if (enquiryToEdit.getResponse() != null && !enquiryToEdit.getResponse().isEmpty()) {
            return false;
        }
        
        // Update the enquiry
        enquiryToEdit.updateEnquiry(newText);
        dataManager.updateEnquiry(enquiryToEdit);
        
        return true;
    }
    
    /**
     * Delete an enquiry for a specific applicant.
     * Only the applicant who created the enquiry can delete it.
     * 
     * @param applicant The applicant deleting the enquiry
     * @param projectName The name of the project the enquiry is about
     * @param enquiryText The text of the enquiry (to identify it)
     * @return True if the enquiry was deleted successfully, false otherwise
     */
    public boolean deleteEnquiry(Applicant applicant, String projectName, String enquiryText) {
        // Get all enquiries for this applicant
        List<Enquiry> userEnquiries = getUserEnquiries(applicant);
        
        // Find the specific enquiry by project and text
        Enquiry enquiryToDelete = userEnquiries.stream()
            .filter(e -> e.getProject().getProjectName().equals(projectName) && e.getEnquiryText().equals(enquiryText))
            .findFirst()
            .orElse(null);
        
        if (enquiryToDelete == null) {
            return false;
        }
        
        // Delete the enquiry
        dataManager.deleteEnquiry(enquiryToDelete);
        
        // Remove from applicant's list of enquiries if present
        applicant.getEnquiries().remove(enquiryToDelete);
        
        return true;
    }
    
    /**
     * Get all enquiries for a specific project.
     * 
     * @param project The project to get enquiries for
     * @return A list of enquiries for the project
     */
    public List<Enquiry> getProjectEnquiries(Project project) {
        List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
        
        return allEnquiries.stream()
                .filter(e -> e.getProject().getProjectName().equals(project.getProjectName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all enquiries for a specific project by name, for an HDB Officer.
     * 
     * @param officer The HDB Officer requesting enquiries
     * @param projectName The name of the project to get enquiries for
     * @return A list of enquiries for the project, or empty list if officer isn't assigned
     */
    public List<Enquiry> getProjectEnquiries(HDBOfficer officer, String projectName) {
        // Verify the officer is assigned to this project
        if (officer.getAssignedProject() == null || 
            !officer.getAssignedProject().getProjectName().equals(projectName) ||
            !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
            return new ArrayList<>();
        }
        
        // Return enquiries for this project
        List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
        
        return allEnquiries.stream()
                .filter(e -> e.getProject().getProjectName().equals(projectName))
                .collect(Collectors.toList());
    }
    
    /**
     * Get pending enquiries (without responses) for a list of projects.
     * 
     * @param projects The projects to check for pending enquiries
     * @return A list of pending enquiries
     */
    public List<Enquiry> getPendingEnquiries(List<Project> projects) {
        List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
        
        // Get all project names
        List<String> projectNames = projects.stream()
                .map(Project::getProjectName)
                .collect(Collectors.toList());
        
        // Filter enquiries for these projects that have no response
        return allEnquiries.stream()
                .filter(e -> projectNames.contains(e.getProject().getProjectName()))
                .filter(e -> e.getResponse() == null || e.getResponse().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Add a response to an enquiry. 
     * Only HDB Officers and Managers can respond.
     * 
     * @param user The user responding to the enquiry (must be HDBOfficer or HDBManager)
     * @param applicantID The ID of the applicant who submitted the enquiry
     * @param projectName The name of the project the enquiry is about
     * @param response The response text
     * @return true if the response was added successfully, false otherwise
     */
    public boolean respondToEnquiry(User user, String applicantID, String projectName, String response) {
        // First validate the user type
        if (!(user instanceof HDBOfficer) && !(user instanceof HDBManager)) {
            return false; // Only HDB Officers and Managers can respond
        }
        
        // Find the applicant
        Applicant applicant = null;
        for (User u : dataManager.getAllUsers()) {
            if (u instanceof Applicant && u.getUserID().equals(applicantID)) {
                applicant = (Applicant) u;
                break;
            }
        }
        
        if (applicant == null) {
            return false;
        }
        
        // For HDB Officers, check project assignment
        if (user instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) user;
            // Officer must be assigned to the project and approved
            if (officer.getAssignedProject() == null || 
                !officer.getAssignedProject().getProjectName().equals(projectName) || 
                !HDBOfficer.STATUS_APPROVED.equals(officer.getRegistrationStatus())) {
                return false;
            }
        }
        
        // Retrieve the enquiry
        Enquiry enquiryToRespond = getEnquiry(applicant, projectName);
        
        if (enquiryToRespond == null) {
            return false;
        }
        
        // Add the response
        enquiryToRespond.addResponse(response);
        dataManager.updateEnquiry(enquiryToRespond);
        
        return true;
    }
    
    /**
     * Get an enquiry for a specific applicant and project.
     * 
     * @param applicant The applicant who submitted the enquiry
     * @param projectName The name of the project
     * @return The enquiry if found, null otherwise
     */
    private Enquiry getEnquiry(Applicant applicant, String projectName) {
        List<Enquiry> userEnquiries = getUserEnquiries(applicant);
        
        return userEnquiries.stream()
            .filter(e -> e.getProject().getProjectName().equals(projectName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all enquiries in the system (for HDB Managers only).
     * 
     * @param user The user requesting all enquiries
     * @return A list of all enquiries, or an empty list if the user is not a manager
     */
    public List<Enquiry> getAllEnquiries(User user) {
        // Only HDB Managers can view all enquiries
        if (!(user instanceof HDBManager)) {
            return new ArrayList<>();
        }
        
        return dataManager.getAllEnquiries();
    }
}