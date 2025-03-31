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
    // public EnquiryController() {
    //     this.dataManager = DataManager.getInstance();
    // }
    
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
            return true;
        } catch (Exception e) {
            System.err.println("Error creating enquiry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all enquiries created by a specific user.
     * 
     * @param user The user whose enquiries to retrieve
     * @return A list of enquiries created by the user
     */
    public List<Enquiry> getUserEnquiries(User user) {
        try {
            List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
            return allEnquiries.stream()
                    .filter(e -> e.getUser().getUserID().equals(user.getUserID()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error retrieving user enquiries: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Update an existing enquiry with new text.
     * 
     * @param enquiry The enquiry to update
     * @param newText The new text for the enquiry
     * @return True if the enquiry was updated successfully, false otherwise
     */
    public boolean updateEnquiry(Enquiry enquiry, String newText) {
        try {
            // Only allow updating if the enquiry has no response yet
            if (enquiry.getResponse() != null && !enquiry.getResponse().isEmpty()) {
                System.out.println("Cannot edit an enquiry that has already been responded to.");
                return false;
            }
            
            enquiry.updateEnquiry(newText);
            dataManager.updateEnquiry(enquiry);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating enquiry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete an existing enquiry.
     * 
     * @param enquiry The enquiry to delete
     * @return True if the enquiry was deleted successfully, false otherwise
     */
    public boolean deleteEnquiry(Enquiry enquiry) {
        try {
            dataManager.deleteEnquiry(enquiry);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting enquiry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a list of projects that the user can view and enquire about.
     * 
     * @param user The current user
     * @return A list of available projects
     */
    public List<Project> getAvailableProjects(User user) {
        List<Project> allProjects = dataManager.getAllProjects();
        
        // For HDB Managers, show all projects
        if (user instanceof HDBManager) {
            return allProjects;
        }
        
        List<Project> availableProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // For HDB Officers, show projects they're handling
            if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                if (officer.getAssignedProject() != null && 
                    officer.getAssignedProject().getProjectID() == project.getProjectID()) {
                    availableProjects.add(project);
                }
            } 
            // For Applicants, show visible projects appropriate for their status
            else if (user instanceof Applicant) {
                Applicant applicant = (Applicant) user;
                
                // Check if project is visible
                if (project.isVisible()) {
                    // Check if project is available for the applicant's marital status
                    boolean isEligible = false;
                    
                    if ("Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 21) {
                        // Married applicants 21+ can apply for any flat type
                        isEligible = true;
                    } else if (!"Married".equalsIgnoreCase(applicant.getMaritalStatus()) && applicant.getAge() >= 35) {
                        // Single applicants 35+ can only apply for 2-Room
                        // Check if project has 2-Room flats
                        isEligible = project.hasFlatType("2-Room");
                    }
                    
                    if (isEligible) {
                        availableProjects.add(project);
                    }
                }
                
                // Also add projects the applicant has already applied for, even if not visible
                List<Project> appliedProjects = dataManager.getAppliedProjects(applicant);
                for (Project appliedProject : appliedProjects) {
                    if (!availableProjects.contains(appliedProject)) {
                        availableProjects.add(appliedProject);
                    }
                }
            }
        }
        
        return availableProjects;
    }
    
    /**
     * Get all enquiries for a specific project.
     * 
     * @param project The project to get enquiries for
     * @return A list of enquiries for the project
     */
    public List<Enquiry> getProjectEnquiries(Project project) {
        try {
            List<Enquiry> allEnquiries = dataManager.getAllEnquiries();
            return allEnquiries.stream()
                    .filter(e -> e.getProject().getProjectID() == project.getProjectID())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error retrieving project enquiries: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get projects that the staff member is handling.
     * 
     * @param user The staff member (HDB Officer or Manager)
     * @return A list of projects the staff member is handling
     */
    public List<Project> getStaffProjects(User user) {
        List<Project> staffProjects = new ArrayList<>();
        
        if (user instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) user;
            if (officer.getAssignedProject() != null) {
                staffProjects.add(officer.getAssignedProject());
            }
        } else if (user instanceof HDBManager) {
            HDBManager manager = (HDBManager) user;
            // For managers, get all projects they created
            List<Project> allProjects = dataManager.getAllProjects();
            for (Project project : allProjects) {
                if (project.getManagerInCharge() != null && 
                    project.getManagerInCharge().getUserID().equals(manager.getUserID())) {
                    staffProjects.add(project);
                }
            }
        }
        
        return staffProjects;
    }
    
    /**
     * Get pending enquiries (without responses) for a list of projects.
     * 
     * @param projects The projects to check for pending enquiries
     * @return A list of pending enquiries
     */
    public List<Enquiry> getPendingEnquiries(List<Project> projects) {
        List<Enquiry> pendingEnquiries = new ArrayList<>();
        
        for (Project project : projects) {
            List<Enquiry> projectEnquiries = getProjectEnquiries(project);
            for (Enquiry enquiry : projectEnquiries) {
                if (enquiry.getResponse() == null || enquiry.getResponse().isEmpty()) {
                    pendingEnquiries.add(enquiry);
                }
            }
        }
        
        return pendingEnquiries;
    }
    
    /**
     * Add a response to an enquiry.
     * 
     * @param enquiry The enquiry to respond to
     * @param response The response text
     * @return True if the response was added successfully, false otherwise
     */
    public boolean respondToEnquiry(Enquiry enquiry, String response) {
        try {
            enquiry.addResponse(response);
            dataManager.updateEnquiry(enquiry);
            return true;
        } catch (Exception e) {
            System.err.println("Error responding to enquiry: " + e.getMessage());
            return false;
        }
    }

        /**
     * Get all enquiries in the system.
     * 
     * @return A list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return dataManager.getAllEnquiries();
    }
}