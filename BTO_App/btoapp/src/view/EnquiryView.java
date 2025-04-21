package view;

import controller.EnquiryController;
import controller.ProjectController;
import enquiry.Enquiry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.User;
import view.abstracts.ARenderView;
import view.interfaces.ViewInterface;

/**
 * View for enquiry-related operations in the BTO Management System.
 */
public class EnquiryView extends ARenderView implements ViewInterface {
    
    private User currentUser;
    private EnquiryController enquiryController;
    private ProjectController projectController;
    private Scanner scanner;
    
    /**
     * Constructor for EnquiryView.
     * 
     * @param currentUser The currently logged-in user
     * @param enquiryController Controller for enquiry operations
     * @param projectController Controller for project operations
     */
    public EnquiryView(User currentUser, EnquiryController enquiryController, 
                      ProjectController projectController) {
        this.currentUser = currentUser;
        this.enquiryController = enquiryController;
        this.projectController = projectController;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Displays the interface for creating a new enquiry.
     */
    public void displayCreateEnquiry() {
        if (!(currentUser instanceof Applicant)) {
            showError("Only applicants can create enquiries.");
            return;
        }
        
        Applicant applicant = (Applicant) currentUser;
        
        printHeader("CREATE NEW ENQUIRY");
        
        // Get available projects
        List<Project> availableProjects = projectController.getVisibleProjectsForApplicant(applicant);
        
        if (availableProjects.isEmpty()) {
            showError("No available projects found to submit enquiry for.");
            return;
        }
        
        System.out.println("Select a project to enquire about:");
        int index = 1;
        for (Project project : availableProjects) {
            System.out.printf("%d. %s\n", index++, project.getProjectName());
        }
        
        System.out.print("\nEnter selection (1-" + availableProjects.size() + "): ");
        int projectChoice = getIntInput();
        
        if (projectChoice < 1 || projectChoice > availableProjects.size()) {
            showError("Invalid selection.");
            return;
        }
        
        Project selectedProject = availableProjects.get(projectChoice - 1);
        
        System.out.println("\nEnter your enquiry:");
        String enquiryText = scanner.nextLine();
        
        if (enquiryText.trim().isEmpty()) {
            showError("Enquiry cannot be empty.");
            return;
        }
        
        // Create enquiry
        Enquiry enquiry = enquiryController.createEnquiry(
            applicant, selectedProject.getProjectName(), enquiryText);
        
        if (enquiry != null) {
            showMessage("Enquiry created successfully with ID: " + enquiry.getEnquiryId());
        } else {
            showError("Failed to create enquiry. Please try again later.");
        }
    }
    
    /**
     * Displays the user's enquiries.
     */
    public void displayMyEnquiries() {
        if (!(currentUser instanceof Applicant)) {
            showError("Only applicants can view their enquiries.");
            return;
        }
        
        Applicant applicant = (Applicant) currentUser;
        
        printHeader("MY ENQUIRIES");
        
        List<Enquiry> enquiries = enquiryController.getEnquiriesByApplicant(applicant);
        
        if (enquiries.isEmpty()) {
            showMessage("You have not submitted any enquiries yet.");
            return;
        }
        
        displayEnquiriesList(enquiries);
        
        System.out.print("\nEnter enquiry ID to view details, or 0 to return: ");
        String enquiryId = scanner.nextLine();
        
        if (!enquiryId.equals("0")) {
            Enquiry selectedEnquiry = null;
            for (Enquiry enq : enquiries) {
                if (enq.getEnquiryId().equals(enquiryId)) {
                    selectedEnquiry = enq;
                    break;
                }
            }
            
            if (selectedEnquiry != null) {
                displayEnquiryDetails(selectedEnquiry);
                
                // Display enquiry options
                displayApplicantEnquiryOptions(selectedEnquiry);
            } else {
                showError("Invalid enquiry ID.");
            }
        }
    }
    
    /**
     * Displays enquiries for an HDB officer's assigned project.
     */
    public void displayProjectEnquiries() {
        if (!(currentUser instanceof HDBOfficer)) {
            showError("Only HDB Officers can access this view.");
            return;
        }
        
        HDBOfficer officer = (HDBOfficer) currentUser;
        
        // Check if officer is assigned to a project
        if (!officer.isProjectAssigned()) {
            showError("You are not assigned to any project yet.");
            return;
        }
        
        Project project = officer.getAssignedProject();
        
        printHeader("ENQUIRIES FOR: " + project.getProjectName());
        
        List<Enquiry> enquiries = enquiryController.getEnquiriesForOfficer(
            project.getProjectName(), officer);
        
        if (enquiries.isEmpty()) {
            showMessage("No enquiries found for this project.");
            return;
        }
        
        // Filter options
        System.out.println("Filter by status:");
        System.out.println("1. All Enquiries");
        System.out.println("2. Unanswered Enquiries");
        System.out.println("3. Answered Enquiries");
        
        System.out.print("\nEnter selection (1-3): ");
        int filterChoice = getIntInput();
        
        List<Enquiry> filteredEnquiries;
        
        switch (filterChoice) {
            case 2:
                filteredEnquiries = enquiryController.getUnansweredEnquiries(project.getProjectName());
                break;
            case 3:
                filteredEnquiries = enquiryController.getAnsweredEnquiries(project.getProjectName());
                break;
            default:
                filteredEnquiries = enquiries;
        }
        
        if (filteredEnquiries.isEmpty()) {
            showMessage("No enquiries found with the selected filter.");
            return;
        }
        
        displayEnquiriesList(filteredEnquiries);
        
        System.out.print("\nEnter enquiry ID to view/reply, or 0 to return: ");
        String enquiryId = scanner.nextLine();
        
        if (!enquiryId.equals("0")) {
            Enquiry selectedEnquiry = null;
            for (Enquiry enq : filteredEnquiries) {
                if (enq.getEnquiryId().equals(enquiryId)) {
                    selectedEnquiry = enq;
                    break;
                }
            }
            
            if (selectedEnquiry != null) {
                displayEnquiryDetails(selectedEnquiry);
                
                // Display reply option for officer
                displayOfficerEnquiryOptions(selectedEnquiry, officer);
            } else {
                showError("Invalid enquiry ID.");
            }
        }
    }
    
    /**
     * Displays all enquiries for an HDB manager.
     */
    public void displayAllEnquiries() {
        if (!(currentUser instanceof HDBManager)) {
            showError("Only HDB Managers can access this view.");
            return;
        }
        
        HDBManager manager = (HDBManager) currentUser;
        
        printHeader("ALL ENQUIRIES");
        
        List<Enquiry> enquiries = enquiryController.getAllEnquiriesForManager(manager);
        
        if (enquiries.isEmpty()) {
            showMessage("No enquiries found for your projects.");
            return;
        }
        
        // Filter options
        System.out.println("Filter by status:");
        System.out.println("1. All Enquiries");
        System.out.println("2. Unanswered Enquiries");
        System.out.println("3. Answered Enquiries");
        
        System.out.print("\nEnter selection (1-3): ");
        int filterChoice = getIntInput();
        
        List<Enquiry> filteredEnquiries = new ArrayList<>();
        
        switch (filterChoice) {
            case 2:
                // Filter for unanswered enquiries without using streams
                for (Enquiry enquiry : enquiries) {
                    if (!enquiry.isAnswered()) {
                        filteredEnquiries.add(enquiry);
                    }
                }
                break;
            case 3:
                // Filter for answered enquiries without using streams
                for (Enquiry enquiry : enquiries) {
                    if (enquiry.isAnswered()) {
                        filteredEnquiries.add(enquiry);
                    }
                }
                break;
            default:
                filteredEnquiries = enquiries;
        }
        
        if (filteredEnquiries.isEmpty()) {
            showMessage("No enquiries found with the selected filter.");
            return;
        }
        
        displayEnquiriesList(filteredEnquiries);
        
        System.out.print("\nEnter enquiry ID to view/reply, or 0 to return: ");
        String enquiryId = scanner.nextLine();
        
        if (!enquiryId.equals("0")) {
            Enquiry selectedEnquiry = null;
            for (Enquiry enq : filteredEnquiries) {
                if (enq.getEnquiryId().equals(enquiryId)) {
                    selectedEnquiry = enq;
                    break;
                }
            }
            
            if (selectedEnquiry != null) {
                displayEnquiryDetails(selectedEnquiry);
                
                // Display manager enquiry options
                displayManagerEnquiryOptions(selectedEnquiry, manager);
            } else {
                showError("Invalid enquiry ID.");
            }
        }
    }
    
    /**
     * Displays a list of enquiries.
     * 
     * @param enquiries The list of enquiries to display
     */
    private void displayEnquiriesList(List<Enquiry> enquiries) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        System.out.println("ID | Project | Submission Date | Status");
        System.out.println("-------------------------------------");
        
        for (Enquiry enquiry : enquiries) {
            System.out.printf("%-20s | %-15s | %-16s | %s\n",
                             enquiry.getEnquiryId(),
                             enquiry.getProject().getProjectName(),
                             dateFormat.format(enquiry.getSubmissionDate()),
                             enquiry.isAnswered() ? "Answered" : "Pending");
        }
    }
    
    /**
     * Displays detailed information about an enquiry.
     * 
     * @param enquiry The enquiry to display
     */
    private void displayEnquiryDetails(Enquiry enquiry) {
        printHeader("ENQUIRY DETAILS");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        System.out.println("Enquiry ID: " + enquiry.getEnquiryId());
        System.out.println("Project: " + enquiry.getProject().getProjectName());
        System.out.println("Applicant: " + enquiry.getApplicant().getName());
        System.out.println("Submission Date: " + dateFormat.format(enquiry.getSubmissionDate()));
        System.out.println("Status: " + (enquiry.isAnswered() ? "Answered" : "Pending"));
        
        System.out.println("\nEnquiry Text:");
        System.out.println(enquiry.getEnquiryText());
        
        if (enquiry.isAnswered()) {
            System.out.println("\nReply:");
            System.out.println(enquiry.getReply());
        }
    }
    
    /**
     * Displays options for an applicant's enquiry.
     * 
     * @param enquiry The enquiry
     */
    private void displayApplicantEnquiryOptions(Enquiry enquiry) {
        // Only allow editing or deleting if enquiry is not answered
        if (!enquiry.isAnswered()) {
            System.out.println("\n1. Edit Enquiry");
            System.out.println("2. Delete Enquiry");
            System.out.println("0. Return");
            
            System.out.print("\nEnter selection (0-2): ");
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    editEnquiry(enquiry);
                    break;
                case 2:
                    deleteEnquiry(enquiry);
                    break;
            }
        } else {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Displays options for an HDB officer to reply to an enquiry.
     * 
     * @param enquiry The enquiry
     * @param officer The HDB officer
     */
    private void displayOfficerEnquiryOptions(Enquiry enquiry, HDBOfficer officer) {
        if (!enquiry.isAnswered()) {
            System.out.println("\n1. Reply to Enquiry");
            System.out.println("0. Return");
            
            System.out.print("\nEnter selection (0-1): ");
            int choice = getIntInput();
            
            if (choice == 1) {
                replyToEnquiry(enquiry, officer.getNric());
            }
        } else {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Displays options for an HDB manager to reply to an enquiry.
     * 
     * @param enquiry The enquiry
     * @param manager The HDB manager
     */
    private void displayManagerEnquiryOptions(Enquiry enquiry, HDBManager manager) {
        if (!enquiry.isAnswered()) {
            System.out.println("\n1. Reply to Enquiry");
            System.out.println("0. Return");
            
            System.out.print("\nEnter selection (0-1): ");
            int choice = getIntInput();
            
            if (choice == 1) {
                replyToEnquiry(enquiry, manager.getNric());
            }
        } else {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Edits an enquiry.
     * 
     * @param enquiry The enquiry to edit
     */
    private void editEnquiry(Enquiry enquiry) {
        printHeader("EDIT ENQUIRY");
        
        System.out.println("Current Enquiry Text:");
        System.out.println(enquiry.getEnquiryText());
        
        System.out.println("\nEnter new enquiry text:");
        String newText = scanner.nextLine();
        
        if (newText.trim().isEmpty()) {
            showError("Enquiry text cannot be empty.");
            return;
        }
        
        boolean updated = enquiryController.updateEnquiry(
            enquiry.getEnquiryId(), newText, (Applicant) currentUser);
        
        if (updated) {
            showMessage("Enquiry updated successfully.");
        } else {
            showError("Failed to update enquiry. Please try again later.");
        }
    }
    
    /**
     * Deletes an enquiry.
     * 
     * @param enquiry The enquiry to delete
     */
    private void deleteEnquiry(Enquiry enquiry) {
        printHeader("DELETE ENQUIRY");
        
        System.out.println("Are you sure you want to delete this enquiry? (Y/N)");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = enquiryController.deleteEnquiry(
                enquiry.getEnquiryId(), (Applicant) currentUser);
            
            if (deleted) {
                showMessage("Enquiry deleted successfully.");
            } else {
                showError("Failed to delete enquiry. Please try again later.");
            }
        } else {
            showMessage("Deletion cancelled.");
        }
    }
    
    /**
     * Replies to an enquiry.
     * 
     * @param enquiry The enquiry to reply to
     * @param responderNric The NRIC of the responder
     */
    private void replyToEnquiry(Enquiry enquiry, String responderNric) {
        printHeader("REPLY TO ENQUIRY");
        
        System.out.println("Enter your reply:");
        String replyText = scanner.nextLine();
        
        if (replyText.trim().isEmpty()) {
            showError("Reply text cannot be empty.");
            return;
        }
        
        boolean replied = false;
        
        if (currentUser instanceof HDBOfficer) {
            replied = enquiryController.replyToEnquiryAsOfficer(
                enquiry.getEnquiryId(), replyText, (HDBOfficer) currentUser);
        } else if (currentUser instanceof HDBManager) {
            replied = enquiryController.replyToEnquiryAsManager(
                enquiry.getEnquiryId(), replyText, (HDBManager) currentUser);
        }
        
        if (replied) {
            showMessage("Reply sent successfully.");
        } else {
            showError("Failed to send reply. Please try again later.");
        }
    }
    
    /**
     * Shows a message to the user.
     *
     * @param message The message to display
     */
    @Override
    public void showMessage(String message) {
        System.out.println("\n>>> " + message);
    }
    
    /**
     * Shows an error message to the user.
     *
     * @param error The error message to display
     */
    @Override
    public void showError(String error) {
        System.out.println("\n!!! ERROR: " + error);
    }

    /**
     * Gets an integer input from the user.
     * 
     * @return The integer input
     */
    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}