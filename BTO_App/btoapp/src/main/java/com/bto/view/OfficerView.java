package com.bto.view;

import com.bto.controller.ApplicationController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.model.HDBOfficer;
import com.bto.view.abstracts.ARenderView;

/**
 * OfficerView class is responsible for rendering the HDB Officer view,
 * prompting the officer to select various options in the BTO Management System.
 * 
 * This class extends the abstract base view class {@link ARenderView}.
 */
public class OfficerView extends ARenderView {
    private HDBOfficer officer;
    private ApplicationController applicationController;
    private ProjectController projectController;
    private EnquiryController enquiryController;

    /**
     * Constructs a new OfficerView with the necessary controllers.
     * 
     * @param officer The HDB Officer using the view
     * @param applicationController Controller for application-related operations
     * @param projectController Controller for project-related operations
     * @param enquiryController Controller for enquiry-related operations
     */
    public OfficerView(HDBOfficer officer,
                       ApplicationController applicationController,
                       ProjectController projectController,
                       EnquiryController enquiryController) {
        this.officer = officer;
        this.applicationController = applicationController;
        this.projectController = projectController;
        this.enquiryController = enquiryController;
    }

    /**
     * Renders the application based on the user's selection.
     * 
     * @param selection The selected menu option
     *                  <ul>
     *                      <li>0: Main menu</li>
     *                      <li>1: Register for Project</li>
     *                      <li>2: Check Registration Status</li>
     *                      <li>3: View Projects</li>
     *                      <li>4: Process Applications</li>
     *                      <li>5: Handle Enquiries</li>
     *                      <li>6: Generate Booking Receipt</li>
     *                      <li>7: Apply for Project (as Applicant)</li>
     *                      <li>8: Change Password</li>
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
                registerForProject();
                break;
            case 2:
                checkRegistrationStatus();
                break;
            case 3:
                viewProjects();
                break;
            case 4:
                processApplications();
                break;
            case 5:
                handleEnquiries();
                break;
            case 6:
                generateBookingReceipt();
                break;
            case 7:
                applyForProject();
                break;
            case 8:
                changePassword();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                delay(2);
                renderApp(0);
        }
    }

    /**
     * Renders the main choice menu for the HDB Officer.
     */
    @Override
    public void renderChoice() {
        printBorder("HDB Officer Portal");
        System.out.println("Welcome, Officer " + officer.getUserID());
        System.out.println("Select an option:");
        System.out.println("(1) Register for Project");
        System.out.println("(2) Check Registration Status");
        System.out.println("(3) View Projects");
        System.out.println("(4) Process Applications");
        System.out.println("(5) Handle Enquiries");
        System.out.println("(6) Generate Booking Receipt");
        System.out.println("(7) Apply for Project (as Applicant)");
        System.out.println("(8) Change Password");
        System.out.println("(0) Exit");
    }

    // Placeholder methods for each menu option
    private void registerForProject() {
        System.out.println("Registering for Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void checkRegistrationStatus() {
        System.out.println("Checking Registration Status");
        pressEnterToContinue();
        renderApp(0);
    }

    private void viewProjects() {
        System.out.println("Viewing Projects");
        pressEnterToContinue();
        renderApp(0);
    }

    private void processApplications() {
        System.out.println("Processing Applications");
        pressEnterToContinue();
        renderApp(0);
    }

    private void handleEnquiries() {
        System.out.println("Handling Enquiries");
        pressEnterToContinue();
        renderApp(0);
    }

    private void generateBookingReceipt() {
        System.out.println("Generating Booking Receipt");
        pressEnterToContinue();
        renderApp(0);
    }

    private void applyForProject() {
        System.out.println("Applying for Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void changePassword() {
        System.out.println("Changing Password");
        pressEnterToContinue();
        renderApp(0);
    }
}