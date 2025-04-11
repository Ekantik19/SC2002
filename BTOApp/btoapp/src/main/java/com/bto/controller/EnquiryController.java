package com.bto.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IEnquiryController;
import com.bto.datamanager.EnquiryDataManager;
import com.bto.enquiry.Enquiry;
import com.bto.enquiry.EnquiryEditor;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;

/**
 * Controller for managing enquiries in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class EnquiryController extends ABaseController implements IEnquiryController {
    
    private Map<String, Enquiry> enquiryMap;
    private Map<String, EnquiryEditor> editorMap;
    private ProjectController projectController;
    private EnquiryDataManager enquiryDataManager;
    
    /**
     * Constructor for EnquiryController.
     * 
     * @param projectController The project controller to use
     * @param enquiryDataManager The enquiry data manager to use
     */
    public EnquiryController(ProjectController projectController, EnquiryDataManager enquiryDataManager) {
        this.enquiryMap = new HashMap<>();
        this.editorMap = new HashMap<>();
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
        for (Enquiry enquiry : enquiries) {
            enquiryMap.put(enquiry.getEnquiryId(), enquiry);
            
            // Add to project's enquiries if not already there
            Project project = enquiry.getProject();
            if (project != null) {
                project.addEnquiry(enquiry);
                
                // Ensure we have an editor for this project
                getOrCreateEditor(project);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Enquiry createEnquiry(Applicant applicant, String projectId, String enquiryText) {
        // Validate input
        if (!validateNotNull(applicant, "Applicant") || 
            !validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(enquiryText, "Enquiry Text")) {
            return null;
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return null;
        }
        
        // Get or create editor for project
        EnquiryEditor editor = getOrCreateEditor(project);
        
        // Create enquiry
        Enquiry enquiry = (Enquiry) editor.create(enquiryText, applicant);
        if (enquiry != null) {
            // Add to map
            enquiryMap.put(enquiry.getEnquiryId(), enquiry);
            
            // Save to data manager
            enquiryDataManager.addEnquiry(enquiry);
        }
        
        return enquiry;
    }
    
    /**
     * Helper method to get or create an enquiry editor for a project.
     * 
     * @param project The project to get an editor for
     * @return The enquiry editor
     */
    private EnquiryEditor getOrCreateEditor(Project project) {
        String projectId = project.getProjectName().toUpperCase();
        
        if (!editorMap.containsKey(projectId)) {
            EnquiryEditor editor = new EnquiryEditor(project);
            editorMap.put(projectId, editor);
            return editor;
        }
        
        return editorMap.get(projectId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateEnquiry(String enquiryId, String newEnquiryText, Applicant applicant) {
        // Validate input
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(newEnquiryText, "New Enquiry Text") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        // Get enquiry
        Enquiry enquiry = enquiryMap.get(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found");
            return false;
        }
        
        // Check if applicant is the owner of the enquiry
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("Unauthorized to update this enquiry");
            return false;
        }
        
        // Get editor for project
        EnquiryEditor editor = getOrCreateEditor(enquiry.getProject());
        
        // Update enquiry
        boolean updated = editor.edit(enquiry, newEnquiryText);
        
        if (updated) {
            // Update in data manager
            enquiryDataManager.updateEnquiry(enquiry);
        }
        
        return updated;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteEnquiry(String enquiryId, Applicant applicant) {
        // Validate input
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNull(applicant, "Applicant")) {
            return false;
        }
        
        // Get enquiry
        Enquiry enquiry = enquiryMap.get(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found");
            return false;
        }
        
        // Check if applicant is the owner of the enquiry
        if (!enquiry.getApplicant().getNric().equals(applicant.getNric())) {
            System.out.println("Unauthorized to delete this enquiry");
            return false;
        }
        
        // Get editor for project
        EnquiryEditor editor = getOrCreateEditor(enquiry.getProject());
        
        // Delete enquiry
        boolean deleted = editor.delete(enquiry);
        
        if (deleted) {
            // Remove from map
            enquiryMap.remove(enquiryId);
            
            // Remove from data manager
            enquiryDataManager.deleteEnquiry(enquiryId);
        }
        
        return deleted;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replyToEnquiryAsOfficer(String enquiryId, String replyText, HDBOfficer officer) {
        // Validate input
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(replyText, "Reply Text") || 
            !validateNotNull(officer, "Officer")) {
            return false;
        }
        
        // Get enquiry
        Enquiry enquiry = enquiryMap.get(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found");
            return false;
        }
        
        // Get editor for project
        EnquiryEditor editor = getOrCreateEditor(enquiry.getProject());
        
        // Reply to enquiry
        boolean replied = officer.replyToEnquiry(enquiry, replyText, editor);
        
        if (replied) {
            // Update in data manager
            enquiryDataManager.updateEnquiry(enquiry);
        }
        
        return replied;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replyToEnquiryAsManager(String enquiryId, String replyText, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID") || 
            !validateNotNullOrEmpty(replyText, "Reply Text") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Get enquiry
        Enquiry enquiry = enquiryMap.get(enquiryId);
        if (enquiry == null) {
            System.out.println("Enquiry not found");
            return false;
        }
        
        // Get editor for project
        EnquiryEditor editor = getOrCreateEditor(enquiry.getProject());
        
        // Reply to enquiry
        boolean replied = manager.replyToEnquiry(enquiry, replyText, editor);
        
        if (replied) {
            // Update in data manager
            enquiryDataManager.updateEnquiry(enquiry);
        }
        
        return replied;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Enquiry getEnquiryById(String enquiryId) {
        if (!validateNotNullOrEmpty(enquiryId, "Enquiry ID")) {
            return null;
        }
        
        return enquiryMap.get(enquiryId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getEnquiriesByApplicant(Applicant applicant) {
        if (!validateNotNull(applicant, "Applicant")) {
            return new ArrayList<>();
        }
        
        return applicant.getEnquiries();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getEnquiriesByProject(String projectId) {
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return new ArrayList<>();
        }
        
        return project.getEnquiries();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getEnquiriesForOfficer(String projectId, HDBOfficer officer) {
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(officer, "Officer")) {
            return new ArrayList<>();
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return new ArrayList<>();
        }
        
        // Check if officer is assigned to project
        if (!officer.isAssignedToProject(project)) {
            System.out.println("Officer is not assigned to this project");
            return new ArrayList<>();
        }
        
        return project.getEnquiries();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getAllEnquiriesForManager(HDBManager manager) {
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        // Get all projects managed by the manager
        List<Project> managedProjects = projectController.getProjectsByManager(manager);
        
        // Collect all enquiries from these projects
        List<Enquiry> allEnquiries = new ArrayList<>();
        for (Project project : managedProjects) {
            allEnquiries.addAll(project.getEnquiries());
        }
        
        return allEnquiries;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getAnsweredEnquiries(String projectId) {
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return new ArrayList<>();
        }
        
        // Filter answered enquiries
        return project.getEnquiries().stream()
            .filter(Enquiry::isAnswered)
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enquiry> getUnansweredEnquiries(String projectId) {
        if (!validateNotNullOrEmpty(projectId, "Project ID")) {
            return new ArrayList<>();
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return new ArrayList<>();
        }
        
        // Filter unanswered enquiries
        return project.getEnquiries().stream()
            .filter(e -> !e.isAnswered())
            .collect(Collectors.toList());
    }
    
    /**
     * Saves all enquiries to the data manager.
     * 
     * @return true if all enquiries were successfully saved, false otherwise
     */
    public boolean saveAllEnquiries() {
        return enquiryDataManager.saveEnquiries(new ArrayList<>(enquiryMap.values()));
    }
    
    /**
     * Reloads all enquiries from the data manager.
     * 
     * @return true if enquiries were successfully reloaded, false otherwise
     */
    public boolean reloadEnquiries() {
        enquiryMap.clear();
        editorMap.clear();
        loadEnquiries();
        return true;
    }
}