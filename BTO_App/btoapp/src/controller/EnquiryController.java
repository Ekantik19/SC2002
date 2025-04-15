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
 */
public class EnquiryController extends ABaseController implements IEnquiryController {
    
    private ProjectController projectController;
    private EnquiryDataManager enquiryDataManager;
    
    /**
     * Constructor for EnquiryController.
     */
    public EnquiryController(ProjectController projectController, EnquiryDataManager enquiryDataManager) {
        this.projectController = projectController;
        this.enquiryDataManager = enquiryDataManager;
        
        System.out.println("DEBUG: EnquiryController initialized");
        
        // Load enquiries from data manager
        loadEnquiries();
    }
    
    /**
     * Loads enquiries from the data manager.
     */
    private void loadEnquiries() {
        System.out.println("DEBUG: Loading enquiries via controller");
        List<Enquiry> enquiries = enquiryDataManager.loadEnquiries();
        System.out.println("DEBUG: Controller loaded " + enquiries.size() + " enquiries");
    }
    
    @Override
    public Enquiry createEnquiry(Applicant applicant, String projectName, String enquiryText) {
        // Validate input
        if (applicant == null) {
            System.out.println("DEBUG: Cannot create enquiry - applicant is null");
            return null;
        }
        
        if (enquiryText == null || enquiryText.trim().isEmpty()) {
            System.out.println("DEBUG: Cannot create enquiry - text is empty");
            return null;
        }
        
        System.out.println("DEBUG: Creating enquiry for applicant: " + applicant.getName() + 
                          " about project: " + projectName);
        
        // Get project (optional)
        Project project = null;
        if (projectName != null && !projectName.trim().isEmpty()) {
            project = projectController.getProjectById(projectName);
            if (project == null) {
                System.out.println("DEBUG: Project not found: " + projectName);
                System.out.println("DEBUG: Creating enquiry without project reference");
            } else {
                System.out.println("DEBUG: Found project: " + project.getProjectName());
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
            
            System.out.println("DEBUG: Enquiry added successfully: " + enquiryId);
            return enquiry;
        } else {
            System.out.println("DEBUG: Failed to add enquiry to data manager");
            return null;
        }
    }
    
    @Override
    public boolean updateEnquiry(String enquiryId, String newEnquiryText, Applicant applicant) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println("DEBUG: Enquiry not found for update: " + enquiryId);
            return false;
        }
        
        // Check ownership
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("DEBUG: Applicant does not own this enquiry");
            return false;
        }
        
        // Update the enquiry
        enquiry.setEnquiryText(newEnquiryText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println("DEBUG: Enquiry update result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }
    
    @Override
    public boolean deleteEnquiry(String enquiryId, Applicant applicant) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println("DEBUG: Enquiry not found for deletion: " + enquiryId);
            return false;
        }
        
        // Check ownership
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("DEBUG: Applicant does not own this enquiry");
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
        System.out.println("DEBUG: Enquiry deletion result: " + deleted);
        
        if (deleted) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return deleted;
    }
    
    @Override
    public boolean replyToEnquiryAsOfficer(String enquiryId, String replyText, HDBOfficer officer) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println("DEBUG: Enquiry not found for reply: " + enquiryId);
            return false;
        }
        
        // Set the reply
        enquiry.setReply(replyText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println("DEBUG: Officer reply result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }
    
    @Override
    public boolean replyToEnquiryAsManager(String enquiryId, String replyText, HDBManager manager) {
        // Find the enquiry
        Enquiry enquiry = enquiryDataManager.getEnquiryById(enquiryId);
        if (enquiry == null) {
            System.out.println("DEBUG: Enquiry not found for reply: " + enquiryId);
            return false;
        }
        
        // Set the reply
        enquiry.setReply(replyText);
        
        // Save changes
        boolean updated = enquiryDataManager.updateEnquiry(enquiry);
        System.out.println("DEBUG: Manager reply result: " + updated);
        
        if (updated) {
            // Save all enquiries to ensure persistence
            enquiryDataManager.saveEnquiries(enquiryDataManager.getAllEnquiries());
        }
        
        return updated;
    }
    
    @Override
    public Enquiry getEnquiryById(String enquiryId) {
        return enquiryDataManager.getEnquiryById(enquiryId);
    }
    
    @Override
    public List<Enquiry> getEnquiriesByApplicant(Applicant applicant) {
        if (applicant == null) {
            System.out.println("DEBUG: Cannot get enquiries - applicant is null");
            return new ArrayList<>();
        }
        
        // Refresh from data manager to ensure we have the latest
        System.out.println("DEBUG: Getting enquiries for applicant: " + applicant.getName() + 
                          " (NRIC: " + applicant.getNric() + ")");
        
        List<Enquiry> enquiries = enquiryDataManager.getEnquiriesByApplicant(applicant.getNric());
        System.out.println("DEBUG: Found " + enquiries.size() + " enquiries for applicant in data manager");
        
        // Merge with applicant's own list to ensure completeness
        List<Enquiry> applicantEnquiries = applicant.getEnquiries();
        System.out.println("DEBUG: Applicant's own list has " + applicantEnquiries.size() + " enquiries");
        
        // Combine the two sources (avoid duplicates)
        List<Enquiry> result = new ArrayList<>(enquiries);
        for (Enquiry enquiry : applicantEnquiries) {
            if (!containsEnquiry(result, enquiry.getEnquiryId())) {
                result.add(enquiry);
            }
        }
        
        System.out.println("DEBUG: Returning " + result.size() + " enquiries for applicant");
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
    
    @Override
    public List<Enquiry> getEnquiriesForOfficer(String projectId, HDBOfficer officer) {
        if (projectId == null || officer == null) {
            System.out.println("DEBUG: Invalid parameters for getEnquiriesForOfficer");
            return new ArrayList<>();
        }
        
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("DEBUG: Project not found: " + projectId);
            return new ArrayList<>();
        }
        
        // Check if officer is assigned to project
        if (!officer.isAssignedToProject(project)) {
            System.out.println("DEBUG: Officer not assigned to project: " + projectId);
            return new ArrayList<>();
        }
        
        List<Enquiry> enquiries = project.getEnquiries();
        System.out.println("DEBUG: Found " + enquiries.size() + " enquiries for project: " + projectId);
        
        return enquiries;
    }
    
    @Override
    public List<Enquiry> getAllEnquiriesForManager(HDBManager manager) {
        if (manager == null) {
            System.out.println("DEBUG: Manager is null");
            return new ArrayList<>();
        }
        
        List<Enquiry> allEnquiries = new ArrayList<>();
        
        // Get all projects managed by this manager
        List<Project> managedProjects = projectController.getProjectsByManager(manager);
        
        // Collect all enquiries from these projects
        for (Project project : managedProjects) {
            allEnquiries.addAll(project.getEnquiries());
        }
        
        System.out.println("DEBUG: Found " + allEnquiries.size() + " enquiries for manager: " + 
                           manager.getName());
        
        return allEnquiries;
    }
    
    @Override
    public List<Enquiry> getEnquiriesByProject(String projectId) {
        if (projectId == null) {
            System.out.println("DEBUG: Project ID is null");
            return new ArrayList<>();
        }
        
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("DEBUG: Project not found: " + projectId);
            return new ArrayList<>();
        }
        
        List<Enquiry> enquiries = project.getEnquiries();
        System.out.println("DEBUG: Found " + enquiries.size() + " enquiries for project: " + projectId);
        
        return enquiries;
    }
    
    @Override
    public List<Enquiry> getAnsweredEnquiries(String projectId) {
        List<Enquiry> allEnquiries = getEnquiriesByProject(projectId);
        List<Enquiry> answeredEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : allEnquiries) {
            if (enquiry.isAnswered()) {
                answeredEnquiries.add(enquiry);
            }
        }
        
        System.out.println("DEBUG: Found " + answeredEnquiries.size() + " answered enquiries");
        
        return answeredEnquiries;
    }
    
    @Override
    public List<Enquiry> getUnansweredEnquiries(String projectId) {
        List<Enquiry> allEnquiries = getEnquiriesByProject(projectId);
        List<Enquiry> unansweredEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : allEnquiries) {
            if (!enquiry.isAnswered()) {
                unansweredEnquiries.add(enquiry);
            }
        }
        
        System.out.println("DEBUG: Found " + unansweredEnquiries.size() + " unanswered enquiries");
        
        return unansweredEnquiries;
    }
}