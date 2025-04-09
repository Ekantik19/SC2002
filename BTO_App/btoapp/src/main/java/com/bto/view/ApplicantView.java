package com.bto.view;

import com.bto.controller.ApplicationController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.model.Applicant;
import com.bto.view.abstracts.ARenderView;

/**
 * ApplicantView class is responsible for rendering the Applicant view,
 * prompting the applicant to select various options in the BTO Management System.
 * 
 * This class extends the abstract base view class {@link ARenderView}.
 */
public class ApplicantView extends ARenderView {
    private Applicant applicant;
    private ApplicationController applicationController;
    private ProjectController projectController;
    private EnquiryController enquiryController;

    /**
     * Constructs a new ApplicantView with the necessary controllers.
     * 
     * @param applicant The Applicant using the view
     * @param applicationController Controller for application-related operations
     * @param projectController Controller for project-related operations
     * @param enquiryController Controller for enquiry-related operations
     */
    public ApplicantView(Applicant applicant,
                         ApplicationController applicationController,
                         ProjectController projectController,
                         EnquiryController enquiryController) {
        this.applicant = applicant;
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
     *                      <li>1: View Projects</li>
     *                      <li>2: Apply for Project</li>
     *                      <li>3: View Application Status</li>
     *                      <li>4: Request Withdrawal</li>
     *                      <li>5: Book Flat</li>
     *                      <li>6: Create Enquiry</li>
     *                      <li>7: View Enquiries</li>
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
                viewProjects();
                break;
            case 2:
                applyForProject();
                break;
            case 3:
                viewApplicationStatus();
                break;
            case 4:
                requestWithdrawal();
                break;
            case 5:
                bookFlat();
                break;
            case 6:
                createEnquiry();
                break;
            case 7:
                viewEnquiries();
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
     * Renders the main choice menu for the Applicant.
     */
    @Override
    public void renderChoice() {
        printBorder("Applicant Portal");
        System.out.println("Welcome, Applicant " + applicant.getUserID());
        System.out.println("Select an option:");
        System.out.println("(1) View Projects");
        System.out.println("(2) Apply for Project");
        System.out.println("(3) View Application Status");
        System.out.println("(4) Request Withdrawal");
        System.out.println("(5) Book Flat");
        System.out.println("(6) Create Enquiry");
        System.out.println("(7) View Enquiries");
        System.out.println("(8) Change Password");
        System.out.println("(0) Exit");
    }

    // Placeholder methods for each menu option
    private void viewProjects() {
        System.out.println("Viewing Projects");
        pressEnterToContinue();
        renderApp(0);
    }

    private void applyForProject() {
        System.out.println("Applying for Project");
        pressEnterToContinue();
        renderApp(0);
    }

    private void viewApplicationStatus() {
        System.out.println("Viewing Application Status");
        pressEnterToContinue();
        renderApp(0);
    }

    private void requestWithdrawal() {
        System.out.println("Requesting Withdrawal");
        pressEnterToContinue();
        renderApp(0);
    }

    private void bookFlat() {
        System.out.println("Booking Flat");
        pressEnterToContinue();
        renderApp(0);
    }

    private void createEnquiry() {
        System.out.println("Creating Enquiry");
        pressEnterToContinue();
        renderApp(0);
    }

    private void viewEnquiries() {
        System.out.println("Viewing Enquiries");
        pressEnterToContinue();
        renderApp(0);
    }

    private void changePassword() {
        System.out.println("Changing Password");
        pressEnterToContinue();
        renderApp(0);
    }
}