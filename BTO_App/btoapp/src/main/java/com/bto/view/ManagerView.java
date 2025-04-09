package com.bto.view;

import com.bto.controller.ApplicationController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.controller.ReportController;
import com.bto.model.HDBManager;
import com.bto.view.abstracts.ARenderView;

/**
 * ManagerView class is responsible for rendering the HDB Manager view,
 * prompting the manager to select various options in the BTO Management System.
 * 
 * This class extends the abstract base view class {@link ARenderView}.
 */
public class ManagerView extends ARenderView {
    private HDBManager manager;
    private ApplicationController applicationController;
    private ProjectController projectController;
    private EnquiryController enquiryController;
    private ReportController reportController;

    /**
     * Constructs a new ManagerView with the necessary controllers.
     * 
     * @param manager The HDB Manager using the view
     * @param applicationController Controller for application-related operations
     * @param projectController Controller for project-related operations
     * @param enquiryController Controller for enquiry-related operations
     * @param reportController Controller for report-related operations
     */
    public ManagerView(HDBManager manager,
                       ApplicationController applicationController,
                       ProjectController projectController,
                       EnquiryController enquiryController,
                       ReportController reportController) {
        this.manager = manager;
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.enquiryController = enquiryController;
        this.reportController = reportController;
    }

    /**
     * Renders the application based on the user's selection.
     * 
     * @param selection The selected menu option
     *                  <ul>
     *                      <li>0: Main menu</li>
     *                      <li>1: Create BTO Project</li>
     *                      <li>2: Edit BTO Project</li>
     *                      <li>3: Delete BTO Project</li>
     *                      <li>4: Toggle Project Visibility</li>
     *                      <li>5: View Projects</li>
     *                      <li>6: Manage Officer Registrations</li>
     *                      <li>7: Process Applications</li>
     *                      <li>8: Handle Withdrawal Requests</li>
     *                      <li>9: Generate Reports</li>
     *                      <li>10: View All Enquiries</li>
     *                      <li>11: Change Password</li>
     *                  </ul>
     */
    @Override
    public void renderApp(int selection) {
        clearCLI();
        switch (selection) {
            case 0:
                renderChoice();
                break;
            case 1:
                createBTOProject();
                break;
            case 2:
                editBTOProject();
                break;
            case 3:
                deleteBTOProject();
                break;
            case 4:
                toggleProjectVisibility();
                break;
            case 5:
                viewProjects();
                break;
            case 6:
                manageOfficerRegistrations();
                break;
            case 7:
                processApplications();
                break;
            case 8:
                handleWithdrawalRequests();
                break;
            case 9:
                generateReports();
                break;
            case 10:
                viewAllEnquiries();
                break;
            case 11:
                changePassword();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                delay(2);
                renderApp(0);
        }
    }

    /**
     * Renders the main choice menu for the HDB Manager.
     */
    @Override
    public void renderChoice() {
        printBorder("HDB Manager Portal");
        System.out.println("Welcome, Manager " + manager.getUserID());
        System.out.println("Select an option:");
        System.out.println("(1) Create BTO Project");
        System.out.println("(2) Edit BTO Project");
        System.out.println("(3) Delete BTO Project");
        System.out.println("(4) Toggle Project Visibility");
        System.out.println("(5) View Projects");
        System.out.println("(6) Manage Officer Registrations");
        System.out.println("(7) Process Applications");
        System.out.println("(8) Handle Withdrawal Requests");
        System.out.println("(9) Generate Reports");
        System.out.println("(10) View All Enquiries");
        System.out.println("(11) Change Password");
        System.out.println("(0) Exit");
    }

    // Placeholder methods for each menu option
    private void createBTOProject() {
        System.out.println("Creating BTO Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void editBTOProject() {
        System.out.println("Editing BTO Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void deleteBTOProject() {
        System.out.println("Deleting BTO Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void toggleProjectVisibility() {
        System.out.println("Toggling Project Visibility");
        pressEnterToContinue();
        renderApp(0);
    }

    private void viewProjects() {
        System.out.println("Viewing Projects");
        pressEnterToContinue();
        renderApp(0);
    }

    private void manageOfficerRegistrations() {
        System.out.println("Managing Officer Registrations");
        pressEnterToContinue();
        renderApp(0);
    }

    private void processApplications() {
        System.out.println("Processing Applications");
        pressEnterToContinue();
        renderApp(0);
    }

    private void handleWithdrawalRequests() {
        System.out.println("Handling Withdrawal Requests");
        pressEnterToContinue();
        renderApp(0);
    }

    private void generateReports() {
        System.out.println("Generating Reports");
        pressEnterToContinue();
        renderApp(0);
    }

    private void viewAllEnquiries() {
        System.out.println("Viewing All Enquiries");
        pressEnterToContinue();
        renderApp(0);
    }

    private void changePassword() {
        System.out.println("Changing Password");
        pressEnterToContinue();
        renderApp(0);
    }
}