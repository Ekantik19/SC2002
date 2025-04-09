package com.bto.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
//10-16
public class OfficerTests extends BaseTest {
    
    @Test
    public void testCase10_OfficerRegistrationEligibility() {
        String testName = "HDB Officer Registration Eligibility";
        System.out.println("\nTest Case 10: " + testName);
        
        try {
            // Login as officer and manager
            HDBOfficer officer = (HDBOfficer) authController.login("T2109876H", "password");
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            
            // Get an available project
            Project project = projectController.getAllProjects(manager).get(0);
            
            // Register for project
            boolean registerResult = projectController.registerOfficerForProject(officer, project.getProjectName());
            assertTrue("Officer should register for project", registerResult);
            
            // Try to register for another project (should fail)
            Project secondProject = projectController.getAllProjects(manager).get(1);
            boolean secondRegisterResult = projectController.registerOfficerForProject(officer, secondProject.getProjectName());
            assertFalse("Officer should not register for second project", secondRegisterResult);
            
            // Clean up
            projectController.removeOfficerFromProject(manager, project.getProjectName(), officer.getUserID());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase11_OfficerRegistrationStatus() {
        String testName = "HDB Officer Registration Status";
        System.out.println("\nTest Case 11: " + testName);
        
        try {
            // Login as officer and manager
            HDBOfficer officer = (HDBOfficer) authController.login("T2109876H", "password");
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            
            // Get an available project
            Project project = projectController.getAllProjects(manager).get(0);
            
            // Register for project
            boolean registerResult = projectController.registerOfficerForProject(officer, project.getProjectName());
            assertTrue("Officer should register for project", registerResult);
            
            // Check initial status (should be Pending)
            String status = projectController.getOfficerRegistrationStatus(officer, project.getProjectName());
            assertEquals("Initial status should be Pending", "PENDING", status);
            
            // Approve registration
            boolean approveResult = projectController.approveOfficerRegistration(manager, project.getProjectName(), officer.getUserID());
            assertTrue("Manager should approve officer registration", approveResult);
            
            // Check updated status (should be Approved)
            status = projectController.getOfficerRegistrationStatus(officer, project.getProjectName());
            assertEquals("Status should be Approved", "APPROVED", status);
            
            // Clean up
            projectController.removeOfficerFromProject(manager, project.getProjectName(), officer.getUserID());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase14_ResponseToProjectEnquiries() {
        String testName = "Response to Project Enquiries";
        System.out.println("\nTest Case 14: " + testName);
        
        try {
            // Login as applicant, officer, and manager
            Applicant applicant = (Applicant) authController.login("S1234567A", "password");
            HDBOfficer officer = (HDBOfficer) authController.login("T2109876H", "password");
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            
            // Get an available project
            Project project = projectController.getAvailableProjects(applicant).get(0);
            
            // Register and approve officer for project
            projectController.registerOfficerForProject(officer, project.getProjectName());
            projectController.approveOfficerRegistration(manager, project.getProjectName(), officer.getUserID());
            
            // Submit enquiry
            String enquiryText = "Test enquiry about project details";
            int enquiryId = enquiryController.submitEnquiry(applicant, project, enquiryText);
            assertTrue("Enquiry submission should succeed", enquiryId > 0);
            
            // Officer responds to enquiry
            String responseText = "Official response to enquiry";
            boolean responseResult = enquiryController.respondToEnquiry(
                officer, applicant.getUserID(), project.getProjectName(), responseText
            );
            assertTrue("Officer should be able to respond to enquiry", responseResult);
            
            // Clean up
            enquiryController.deleteEnquiry(applicant, project.getProjectName(), enquiryText);
            projectController.removeOfficerFromProject(manager, project.getProjectName(), officer.getUserID());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase15_FlatSelectionAndBooking() {
        String testName = "Flat Selection and Booking Management";
        System.out.println("\nTest Case 15: " + testName);
        
        try {
            // Login as applicant, officer, and manager
            Applicant applicant = (Applicant) authController.login("S1234567A", "password");
            HDBOfficer officer = (HDBOfficer) authController.login("T2109876H", "password");
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            
            // Get an available project
            Project project = projectController.getAvailableProjects(applicant).get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Register and approve officer
            projectController.registerOfficerForProject(officer, projectName);
            projectController.approveOfficerRegistration(manager, projectName, officer.getUserID());
            
            // Apply for project
            applicationController.applyForProject(applicant, projectName, flatType);
            
            // Approve application
            Application application = applicationController.getApplication(applicant);
            applicationController.approveApplication(manager, application.getApplicant().getUserID());
            
            // Get initial flat count
            int initialFlatCount = projectController.getRemainingUnits(projectName, flatType);
            
            // Book flat
            boolean bookResult = applicationController.bookFlat(
                officer, applicant.getUserID(), projectName, flatType
            );
            assertTrue("Officer should book flat successfully", bookResult);
            
            // Verify flat count decreased
            int newFlatCount = projectController.getRemainingUnits(projectName, flatType);
            assertEquals("Flat count should decrease", initialFlatCount - 1, newFlatCount);
            
            // Clean up
            applicationController.withdrawApplication(applicant);
            projectController.removeOfficerFromProject(manager, projectName, officer.getUserID());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase16_ReceiptGeneration() {
        String testName = "Receipt Generation for Flat Booking";
        System.out.println("\nTest Case 16: " + testName);
        
        try {
            // Login as applicant, officer, and manager
            Applicant applicant = (Applicant) authController.login("S1234567A", "password");
            HDBOfficer officer = (HDBOfficer) authController.login("T2109876H", "password");
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            
            // Get an available project
            Project project = projectController.getAvailableProjects(applicant).get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Register and approve officer
            projectController.registerOfficerForProject(officer, projectName);
            projectController.approveOfficerRegistration(manager, projectName, officer.getUserID());
            
            // Apply for project
            applicationController.applyForProject(applicant, projectName, flatType);
            
            // Approve application
            Application application = applicationController.getApplication(applicant);
            applicationController.approveApplication(manager, application.getApplicant().getUserID());
            
            // Book flat
            applicationController.bookFlat(officer, applicant.getUserID(), projectName, flatType);
            
            // Generate receipt
            String receipt = applicationController.generateReceipt(officer, applicant.getUserID());
            
            // Verify receipt
            assertNotNull("Receipt should be generated", receipt);
            assertTrue("Receipt should contain applicant details", receipt.contains(applicant.getUserID()));
            assertTrue("Receipt should contain project details", receipt.contains(projectName));
            
            // Clean up
            applicationController.withdrawApplication(applicant);
            projectController.removeOfficerFromProject(manager, projectName, officer.getUserID());
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}