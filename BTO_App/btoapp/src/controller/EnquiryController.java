package controller;

import controller.abstracts.ABaseController;
import controller.interfaces.IEnquiryController;
import datamanager.EnquiryDataManager;
import enquiry.Enquiry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;

/**
 * Controller for managing enquiries in the BTO Management System.
 * 
 * Responsible for handling all enquiry-related operations including:
 * - Creating new enquiries
 * - Updating existing enquiries
 * - Deleting enquiries
 * - Responding to enquiries
 * - Retrieving enquiries by various criteria
 * 
 * Integrates with data managers to persist and retrieve enquiry information.
 * 
 * @author Your Name
 * @version 1.0
 */
public class EnquiryController extends ABaseController implements IEnquiryController {
    
    private ProjectController projectController;
    private EnquiryDataManager enquiryDataManager;
    
    /**
     * Constructor for EnquiryController.
     * Loads existing enquiries upon initialisation
     * 
     * @param projectController Controller for managing project-related operations
     * @param enquiryDataManager Manager for handling enquiry data operations
     */
    public EnquiryController(ProjectController projectController, EnquiryDataManager enquiryDataManager) {
        this.projectController = projectController;
        this.enquiryDataManager = enquiryDataManager;
        
        // Load enquiries from data manager
        loadEnquiries();
    }
    
    /**
     * Loads enquiries from the data manager.
     */
    private void loadEnquiries() {
        List<Enquiry> enquiries = enquiryDataManager.loadEnquiries();
    }

    /**
     * Creates a new enquiry for an applicant.
     * 
     * Validates input, generates a unique enquiry ID, and associates 
     * the enquiry with the applicant and optionally a project.
     * 
     * @param applicant The applicant creating the enquiry
     * @param projectName Optional project name related to the enquiry
     * @param enquiryText The text content of the enquiry
     * @return The created Enquiry object, or null if creation fails
     */
    @Override
    public Enquiry createEnquiry(Applicant applicant, String projectName, String enquiryText) {
        // Validate input
        if (applicant == null) {
            System.out.println(" Cannot create enquiry - applicant is null");
            return null;
        }
        
        if (enquiryText == null || enquiryText.trim().isEmpty()) {
            System.out.println(" Cannot create enquiry - text is empty");
            return null;
        }
        
        System.out.println(" Creating enquiry for applicant: " + applicant.getName() + 
                          " about project: " + projectName);
        
        // Get project (optional)
        Project project = null;
        if (projectName != null && !projectName.trim().isEmpty()) {
            project = projectController.getProjectById(projectName);
            if (project == null) {
                System.out.println(" Project not found: " + projectName);
            } else {
                System.out.println(" Found project: " + project.getProjectName());
            }
        }
        
        // Generate unique ID
        String enquiryId = "ENQ-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Create enquiry
        Enquiry enquiry = new Enquiry(enquiryId, applicant, project, enquiryText, new Date());
        
        // Add to data manager
        boolean added = enquiryDataManager.addEnquiry(enquiry);
        
        if (added) {
            // Explicitly add to applicant's list
            applicant.addEnquiry(enquiry);
            
            // Add to project if available
            if (project != null) {
                project.addEnquiry(enquiry);
            }
            
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
            
            System.out.println(" Enquiry added successfully: " + enquiryId);
            return enquiry;
        } else {
            System.out.println(" Failed to add enquiry");
            return null;
        }
    }
    
    /**
     * Updates an existing enquiry's text.
     * 
     * Allows an applicant to modify their own enquiry.
     * 
     * @param enquiryId Unique identifier of the enquiry
     * @param newEnquiryText Updated text for the enquiry
     * @param applicant The applicant modifying the enquiry
     * @return true if the update is successful, false otherwise
     */
    @Override
    public boolean updateEnquiry(String enquiryId, String newEnquiryText, Applicant applicant) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println(" Enquiry not found for update: " + enquiryId);
            return false;
        }
        
        // Check ownership
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println(" Applicant does not own this enquiry");
            return false;
        }
        
        // Update the enquiry
        enquiry.setEnquiryText(newEnquiryText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println(" Enquiry update result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }

    /**
     * Deletes an existing enquiry.
     * 
     * Allows an applicant to delete their own enquiry.
     * 
     * @param enquiryId Unique identifier of the enquiry
     * @param applicant The applicant deleting the enquiry
     * @return true if the deletion is successful, false otherwise
     */
    @Override
    public boolean deleteEnquiry(String enquiryId, Applicant applicant) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println(" Enquiry not found for deletion: " + enquiryId);
            return false;
        }
        
        // Check ownership
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println(" Applicant does not own this enquiry");
            return false;
        }
        
        // Remove from applicant's list
        applicant.removeEnquiry(enquiry);
        
        // Remove from project if applicable
        if (enquiry.getProject() != null) {
            enquiry.getProject().removeEnquiry(enquiry);
        }
        
        // Delete the enquiry
        boolean deleted = enquiryDataManager.deleteEnquiry(enquiryId);
        System.out.println(" Enquiry deletion result: " + deleted);
        
        if (deleted) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return deleted;
    }
    
    /**
     * Adds a reply to an enquiry by an HDB Officer.
     * 
     * Allows an officer to respond to an existing enquiry.
     * 
     * @param enquiryId Unique identifier of the enquiry
     * @param replyText The response text
     * @param officer The HDB Officer responding
     * @return true if the reply is successfully added, false otherwise
     */
    @Override
    public boolean replyToEnquiryAsOfficer(String enquiryId, String replyText, HDBOfficer officer) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println(" Enquiry not found for reply: " + enquiryId);
            return false;
        }
        
        // Set the reply
        enquiry.setReply(replyText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println(" Officer reply result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }
    
    /**
     * Adds a reply to an enquiry by an HDB Manager.
     * 
     * Allows a manager to respond to an existing enquiry.
     * 
     * @param enquiryId Unique identifier of the enquiry
     * @param replyText The response text
     * @param manager The HDB Manager responding
     * @return true if the reply is successfully added, false otherwise
     */
    @Override
    public boolean replyToEnquiryAsManager(String enquiryId, String replyText, HDBManager manager) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println(" Enquiry not found for reply: " + enquiryId);
            return false;
        }
        
        // Set the reply
        enquiry.setReply(replyText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println(" Manager reply result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }

    /**
     * Retrieves all enquiries created by a specific applicant.
     * 
     * @param applicant The applicant whose enquiries are to be retrieved
     * @return A list of enquiries created by the applicant
     */
    @Override
    public List<Enquiry> getEnquiriesByApplicant(Applicant applicant) {
        if (applicant == null) {
            System.out.println(" Cannot get enquiries - applicant is null");
            return new ArrayList<>();
        }
        
        // Refresh from data manager to ensure we have the latest
        System.out.println(" Getting enquiries for applicant: " + applicant.getName() + 
                          " (NRIC: " + applicant.getNric() + ")");
        
        List<Enquiry> enquiries = enquiryDataManager.getEnquiriesByApplicant(applicant.getNric());
        
        // Merge with applicant's own list to ensure completeness
        List<Enquiry> applicantEnquiries = applicant.getEnquiries();
        
        // Combine the two sources (avoid duplicates)
        List<Enquiry> result = new ArrayList<>(enquiries);
        for (Enquiry enquiry : applicantEnquiries) {
            if (!containsEnquiry(result, enquiry.getEnquiryId())) {
                result.add(enquiry);
            }
        }
        
        System.out.println(" Found " + result.size() + " enquiries for applicant");
        return result;
    }
    
    private boolean containsEnquiry(List<Enquiry> enquiries, String enquiryId) {
        for (Enquiry e : enquiries) {
            if (e.getEnquiryId().equals(enquiryId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves enquiries for a specific project assigned to an HDB Officer.
     * 
     * @param projectId Unique identifier of the project
     * @param officer The HDB Officer assigned to the project
     * @return A list of enquiries for the specified project
     */
    @Override
    public List<Enquiry> getEnquiriesForOfficer(String projectId, HDBOfficer officer) {
        if (projectId == null || officer == null) {
            System.out.println(" Invalid parameters for getEnquiriesForOfficer");
            return new ArrayList<>();
        }
        
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println(" Project not found: " + projectId);
            return new ArrayList<>();
        }
        
        // Check if officer is assigned to project
        if (!officer.isAssignedToProject(project)) {
            System.out.println(" Officer not assigned to project: " + projectId);
            return new ArrayList<>();
        }
        
        List<Enquiry> enquiries = project.getEnquiries();
        System.out.println(" Found " + enquiries.size() + " enquiries for project: " + projectId);
        
        return enquiries;
    }
    
    /**
     * Retrieves all enquiries for projects managed by an HDB Manager.
     * 
     * @param manager The HDB Manager
     * @return A list of all enquiries across the manager's projects
     */
    @Override
    public List<Enquiry> getAllEnquiriesForManager(HDBManager manager) {
        if (manager == null) {
            System.out.println(" Manager is null");
            return new ArrayList<>();
        }
        
        List<Enquiry> allEnquiries = new ArrayList<>();
        
        // Get all projects managed by this manager
        List<Project> managedProjects = projectController.getProjectsByManager(manager);
        
        // Collect all enquiries from these projects
        for (Project project : managedProjects) {
            allEnquiries.addAll(project.getEnquiries());
        }
        
        System.out.println(" Found " + allEnquiries.size() + " enquiries for manager: " + 
                           manager.getName());
        
        return allEnquiries;
    }
    
    /**
     * Retrieves all enquiries for a specific project.
     * 
     * @param projectId Unique identifier of the project
     * @return A list of enquiries for the specified project
     */
    @Override
    public List<Enquiry> getEnquiriesByProject(String projectId) {
        if (projectId == null) {
            System.out.println(" Project ID is null");
            return new ArrayList<>();
        }
        
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println(" Project not found: " + projectId);
            return new ArrayList<>();
        }
        
        List<Enquiry> enquiries = project.getEnquiries();
        System.out.println(" Found " + enquiries.size() + " enquiries for project: " + projectId);
        
        return enquiries;
    }

    /**
     * Retrieves all answered enquiries for a specific project.
     * 
     * @param projectId Unique identifier of the project
     * @return A list of answered enquiries
     */
    @Override
    public List<Enquiry> getAnsweredEnquiries(String projectId) {
        List<Enquiry> allEnquiries = getEnquiriesByProject(projectId);
        List<Enquiry> answeredEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : allEnquiries) {
            if (enquiry.isAnswered()) {
                answeredEnquiries.add(enquiry);
            }
        }
        
        System.out.println(" Found " + answeredEnquiries.size() + " answered enquiries");
        
        return answeredEnquiries;
    }
    
    /**
     * Retrieves all unanswered enquiries for a specific project.
     * 
     * @param projectId Unique identifier of the project
     * @return A list of unanswered enquiries
     */
    @Override
    public List<Enquiry> getUnansweredEnquiries(String projectId) {
        List<Enquiry> allEnquiries = getEnquiriesByProject(projectId);
        List<Enquiry> unansweredEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : allEnquiries) {
            if (!enquiry.isAnswered()) {
                unansweredEnquiries.add(enquiry);
            }
        }
        
        System.out.println(" Found " + unansweredEnquiries.size() + " unanswered enquiries");
        
        return unansweredEnquiries;
    }
}