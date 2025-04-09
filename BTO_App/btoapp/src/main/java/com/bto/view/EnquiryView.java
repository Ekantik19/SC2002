package com.bto.view;

import java.util.List;

import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.model.Applicant;
import com.bto.model.Enquiry;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.User;
import com.bto.view.abstracts.ARenderView;

/**
 * View class for managing enquiries in the BTO Management System.
 * Allows users to create, view, edit, and delete enquiries.
 */
public class EnquiryView extends ARenderView {
    private EnquiryController enquiryController;
    private ProjectController projectController;
    private User currentUser;

    /**
     * Constructor for EnquiryView.
     *
     * @param enquiryController Controller for managing enquiries
     * @param projectController Controller for accessing projects
     * @param currentUser The currently logged in user
     */
    public EnquiryView(EnquiryController enquiryController, ProjectController projectController, User currentUser) {
        super();
        this.enquiryController = enquiryController;
        this.projectController = projectController;
        this.currentUser = currentUser;
    }

    /**
     * Renders the application based on the selection.
     *
     * @param selection The selection made by the user for navigation.
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        
        switch (selection) {
            case 0:
                renderChoice();
                break;
            case 1:
                createEnquiry();
                break;
            case 2:
                viewEnquiries();
                break;
            case 3:
                if (currentUser instanceof Applicant) {
                    editEnquiry();
                } else {
                    System.out.println("Only applicants can edit enquiries.");
                    pressEnterToContinue();
                    renderApp(0);
                }
                break;
            case 4:
                if (currentUser instanceof Applicant) {
                    deleteEnquiry();
                } else {
                    System.out.println("Only applicants can delete enquiries.");
                    pressEnterToContinue();
                    renderApp(0);
                }
                break;
            case 5:
                if (currentUser instanceof HDBOfficer || currentUser instanceof HDBManager) {
                    viewProjectEnquiries();
                } else {
                    System.out.println("Access denied. Only HDB staff can access this function.");
                    pressEnterToContinue();
                    renderApp(0);
                }
                break;
            case 6:
                if (currentUser instanceof HDBOfficer || currentUser instanceof HDBManager) {
                    respondToEnquiry();
                } else {
                    System.out.println("Access denied. Only HDB staff can access this function.");
                    pressEnterToContinue();
                    renderApp(0);
                }
                break;
            default:
                System.out.println("Invalid option selected.");
                delay(1);
                renderApp(0);
                break;
        }
    }

    /**
     * Renders the choices available to the user.
     */
    @Override
    public void renderChoice() {
        printBorder("Enquiry Management");
        System.out.println("1. Create New Enquiry");
        System.out.println("2. View My Enquiries");
        
        // Options for applicants
        if (currentUser instanceof Applicant) {
            System.out.println("3. Edit Enquiry");
            System.out.println("4. Delete Enquiry");
        }
        
        // Options for HDB Officers and Managers
        if (currentUser instanceof HDBOfficer || currentUser instanceof HDBManager) {
            System.out.println("5. View Project Enquiries");
            System.out.println("6. Respond to Enquiry");
        }
        
        System.out.println("0. Return to Main Menu");
        
        int maxOption = (currentUser instanceof Applicant) ? 4 :
                        (currentUser instanceof HDBOfficer || currentUser instanceof HDBManager) ? 6 : 2;
        
        int choice = getInputInt("Enter your choice: ", maxOption);
        renderApp(choice);
    }
    
    /**
     * Create a new enquiry about a project.
     */
    private void createEnquiry() {
        printBorder("Create New Enquiry");
        
        // Get and display available projects
        List<Project> availableProjects = projectController.getAvailableProjects(currentUser);
        
        if (availableProjects.isEmpty()) {
            System.out.println("No projects available for enquiry.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        for (int i = 0; i < availableProjects.size(); i++) {
            Project project = availableProjects.get(i);
            System.out.println((i + 1) + ". " + project.getProjectName() + " - " + project.getNeighborhood());
        }
        
        // Get project selection and enquiry text
        int projectChoice = getInputInt("\nSelect a project (0 to cancel): ", availableProjects.size());
        if (projectChoice == 0) {
            renderApp(0);
            return;
        }
        
        Project selectedProject = availableProjects.get(projectChoice - 1);
        String enquiryText = getInputString("\nEnter your enquiry about " + selectedProject.getProjectName() + ":\n");
        
        if (enquiryText.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Submit the enquiry
        boolean success = enquiryController.createEnquiry(currentUser, selectedProject, enquiryText);
        System.out.println(success ? "Enquiry submitted successfully." : "Failed to submit enquiry.");
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * View all enquiries created by the current user.
     */
    private void viewEnquiries() {
        printBorder("My Enquiries");
        
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        displayEnquiries(userEnquiries);
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Edit an existing enquiry.
     */
    private void editEnquiry() {
        printBorder("Edit Enquiry");
        
        Applicant applicant = (Applicant) currentUser;
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries to edit.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Display enquiries and mark which ones can be edited
        for (int i = 0; i < userEnquiries.size(); i++) {
            Enquiry enquiry = userEnquiries.get(i);
            System.out.println((i + 1) + ". Project: " + enquiry.getProject().getProjectName());
            System.out.println("   Enquiry: " + enquiry.getEnquiryText());
            System.out.println("   Status: " + (enquiry.hasResponse() ? "Already responded (cannot edit)" : "Can be edited"));
            System.out.println("-------------------------------------------");
        }
        
        // Get enquiry to edit
        int enquiryChoice = getInputInt("\nSelect an enquiry to edit (0 to cancel): ", userEnquiries.size());
        if (enquiryChoice == 0) {
            renderApp(0);
            return;
        }
        
        Enquiry selectedEnquiry = userEnquiries.get(enquiryChoice - 1);
        
        if (selectedEnquiry.hasResponse()) {
            System.out.println("This enquiry already has a response and cannot be edited.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Get new text and update
        System.out.println("\nCurrent enquiry: " + selectedEnquiry.getEnquiryText());
        String newText = getInputString("Enter new enquiry text:\n");
        
        if (newText.trim().isEmpty()) {
            System.out.println("Enquiry cannot be empty.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        boolean success = enquiryController.editEnquiry(
            applicant, 
            selectedEnquiry.getProject().getProjectName(), 
            selectedEnquiry.getEnquiryText(), 
            newText
        );
        
        System.out.println(success ? "Enquiry updated successfully." : "Failed to update enquiry.");
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Delete an existing enquiry.
     */
    private void deleteEnquiry() {
        printBorder("Delete Enquiry");
        
        Applicant applicant = (Applicant) currentUser;
        List<Enquiry> userEnquiries = enquiryController.getUserEnquiries(currentUser);
        
        if (userEnquiries.isEmpty()) {
            System.out.println("You have no enquiries to delete.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Display enquiries
        displayEnquiries(userEnquiries);
        
        // Get enquiry to delete
        int enquiryChoice = getInputInt("\nSelect an enquiry to delete (0 to cancel): ", userEnquiries.size());
        if (enquiryChoice == 0) {
            renderApp(0);
            return;
        }
        
        Enquiry selectedEnquiry = userEnquiries.get(enquiryChoice - 1);
        
        // Confirm and delete
        String confirm = getInputString("Are you sure you want to delete this enquiry? (Y/N): ");
        if (!"Y".equalsIgnoreCase(confirm)) {
            System.out.println("Deletion cancelled.");
            renderApp(0);
            return;
        }
        
        boolean success = enquiryController.deleteEnquiry(
            applicant, 
            selectedEnquiry.getProject().getProjectName(), 
            selectedEnquiry.getEnquiryText()
        );
        
        System.out.println(success ? "Enquiry deleted successfully." : "Failed to delete enquiry.");
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * View enquiries for projects the staff member is handling.
     */
    private void viewProjectEnquiries() {
        printBorder("View Project Enquiries");
        
        List<Project> staffProjects = getStaffProjects();
        if (staffProjects.isEmpty()) {
            System.out.println("You are not handling any projects.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Display projects
        for (int i = 0; i < staffProjects.size(); i++) {
            Project project = staffProjects.get(i);
            System.out.println((i + 1) + ". " + project.getProjectName() + " - " + project.getNeighborhood());
        }
        
        // Select project and display enquiries
        int projectChoice = getInputInt("\nSelect a project (0 to cancel): ", staffProjects.size());
        if (projectChoice == 0) {
            renderApp(0);
            return;
        }
        
        Project selectedProject = staffProjects.get(projectChoice - 1);
        List<Enquiry> projectEnquiries = getProjectEnquiries(selectedProject);
        
        if (projectEnquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        printSingleBorder("Enquiries for " + selectedProject.getProjectName());
        displayEnquiries(projectEnquiries);
        
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Respond to an enquiry.
     */
    private void respondToEnquiry() {
        printBorder("Respond to Enquiry");
        
        // Get pending enquiries for all staff projects
        List<Project> staffProjects = getStaffProjects();
        if (staffProjects.isEmpty()) {
            System.out.println("You are not handling any projects.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        List<Enquiry> pendingEnquiries = getPendingEnquiries(staffProjects);
        if (pendingEnquiries.isEmpty()) {
            System.out.println("No pending enquiries to respond to.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Display pending enquiries
        printSingleUnderline("Pending Enquiries");
        for (int i = 0; i < pendingEnquiries.size(); i++) {
            Enquiry enquiry = pendingEnquiries.get(i);
            System.out.println((i + 1) + ". Project: " + enquiry.getProject().getProjectName());
            System.out.println("   From: " + enquiry.getUser().getUserID());
            System.out.println("   Enquiry: " + enquiry.getEnquiryText());
            System.out.println("-------------------------------------------");
        }
        
        // Get enquiry to respond to
        int enquiryChoice = getInputInt("\nSelect an enquiry to respond to (0 to cancel): ", pendingEnquiries.size());
        if (enquiryChoice == 0) {
            renderApp(0);
            return;
        }
        
        Enquiry selectedEnquiry = pendingEnquiries.get(enquiryChoice - 1);
        String response = getInputString("\nEnter your response:\n");
        
        if (response.trim().isEmpty()) {
            System.out.println("Response cannot be empty.");
            pressEnterToContinue();
            renderApp(0);
            return;
        }
        
        // Submit response
        boolean success = enquiryController.respondToEnquiry(
            currentUser,
            selectedEnquiry.getUser().getUserID(),
            selectedEnquiry.getProject().getProjectName(),
            response
        );
        
        System.out.println(success ? "Response submitted successfully." : "Failed to submit response.");
        pressEnterToContinue();
        renderApp(0);
    }
    
    /**
     * Get a list of projects the current staff member is handling.
     * 
     * @return List of projects
     */
    private List<Project> getStaffProjects() {
        if (currentUser instanceof HDBOfficer) {
            return ((HDBOfficer) currentUser).getAssignedProject() != null ?
                   List.of(((HDBOfficer) currentUser).getAssignedProject()) : List.of();
        } else if (currentUser instanceof HDBManager) {
            return ((HDBManager) currentUser).getCreatedProjects();
        }
        return List.of();
    }
    
    /**
     * Get enquiries for a specific project based on user role.
     * 
     * @param project The project to get enquiries for
     * @return List of enquiries for the project
     */
    private List<Enquiry> getProjectEnquiries(Project project) {
        if (currentUser instanceof HDBOfficer) {
            return enquiryController.getProjectEnquiries(
                (HDBOfficer) currentUser, project.getProjectName());
        } else {
            return enquiryController.getProjectEnquiries(project);
        }
    }
    
    /**
     * Get pending enquiries for a list of projects.
     * 
     * @param projects List of projects to check
     * @return List of pending enquiries
     */
    private List<Enquiry> getPendingEnquiries(List<Project> projects) {
        return enquiryController.getPendingEnquiries(projects);
    }
    
    /**
     * Display a list of enquiries in a formatted manner.
     * 
     * @param enquiries The enquiries to display
     */
    private void displayEnquiries(List<Enquiry> enquiries) {
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            System.out.println((i + 1) + ". Project: " + enquiry.getProject().getProjectName());
            System.out.println("   Date: " + enquiry.getCreationDate());
            System.out.println("   Enquiry: " + enquiry.getEnquiryText());
            
            if (enquiry.hasResponse()) {
                System.out.println("   Response: " + enquiry.getResponse());
                System.out.println("   Response Date: " + enquiry.getResponseDate());
            } else {
                System.out.println("   Status: Pending response");
            }
            System.out.println("-------------------------------------------");
        }
    }
}