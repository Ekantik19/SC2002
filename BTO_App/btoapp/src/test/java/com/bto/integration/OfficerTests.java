package com.bto.integration;

import com.bto.model.User;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Enquiry;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HDB Officer functionality
 * Covers test cases 10-16 from Appendix A
 */
public class OfficerTests extends BaseTest {
    
    /**
     * Test Case 10: HDB Officer Registration Eligibility
     * Expected: System allows registration only under compliant conditions
     */
    @Test
    public void testCase10_HDBOfficerRegistrationEligibility() {
        String testName = "HDB Officer Registration Eligibility";
        System.out.println("\nTest Case 10: " + testName);
        
        try {
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Get project to register for
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            List<Project> projects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for registration", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Test case: Officer is already registered for another project
            // First, register for one project
            boolean registerResult1 = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult1);
            printPass(testName, "Officer successfully registered for project");
            
            // Try to register for a second project (should fail)
            if (projects.size() > 1) {
                Project secondProject = projects.get(1);
                boolean registerResult2 = projectController.registerOfficerForProject(
                    (HDBOfficer)officer, secondProject.getProjectName());
                
                assertFalse("System should prevent registration for second project", registerResult2);
                
                if (!registerResult2) {
                    printPass(testName, "System prevented registration for second project");
                } else {
                    printFail(testName, "System incorrectly allowed registration for second project");
                    
                    // Clean up second registration
                    projectController.removeOfficerFromProject((HDBManager)manager, 
                        secondProject.getProjectName(), ((HDBOfficer)officer).getNRIC());
                }
            }
            
            // Test case: Officer tries to apply for the same project
            User officerAsApplicant = authController.login("T2109876H", "password");
            boolean applyResult = applicationController.applyForProject(
                (HDBOfficer)officerAsApplicant, projectName, "2-Room");
            
            assertFalse("System should prevent officer from applying for project they're handling", applyResult);
            
            if (!applyResult) {
                printPass(testName, "System prevented officer from applying for project they're handling");
            } else {
                printFail(testName, "System incorrectly allowed officer to apply for project they're handling");
                
                // Clean up application
                applicationController.withdrawApplication((HDBOfficer)officerAsApplicant);
            }
            
            // Clean up officer registration
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 11: HDB Officer Registration Status
     * Expected: Officers can view pending or approved status updates on their profiles
     */
    @Test
    public void testCase11_HDBOfficerRegistrationStatus() {
        String testName = "HDB Officer Registration Status";
        System.out.println("\nTest Case 11: " + testName);
        
        try {
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to register for
            List<Project> projects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for registration", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Officer registers for project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            printPass(testName, "Officer successfully registered for project");
            
            // Check registration status (should be pending)
            String status = projectController.getOfficerRegistrationStatus(
                (HDBOfficer)officer, projectName);
            
            assertNotNull("Should get a registration status", status);
            assertEquals("Status should be Pending", "Pending", status);
            
            if (status != null && status.equalsIgnoreCase("Pending")) {
                printPass(testName, "Officer registration status correctly shows as Pending");
            } else {
                printFail(testName, "Officer registration status not showing as Pending");
            }
            
            // Manager approves registration
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Check registration status again (should be approved)
            status = projectController.getOfficerRegistrationStatus(
                (HDBOfficer)officer, projectName);
            
            assertNotNull("Should get a registration status", status);
            assertEquals("Status should be Approved", "Approved", status);
            
            if (status != null && status.equalsIgnoreCase("Approved")) {
                printPass(testName, "Officer registration status correctly updated to Approved");
            } else {
                printFail(testName, "Officer registration status not updated to Approved");
            }
            
            // Check if project is reflected in officer's profile
            List<String> officerProjects = projectController.getOfficerProjects((HDBOfficer)officer);
            boolean projectFound = officerProjects.contains(projectName);
            
            assertTrue("Project should be reflected in officer's profile", projectFound);
            
            if (projectFound) {
                printPass(testName, "Project correctly reflected in officer's profile");
            } else {
                printFail(testName, "Project not reflected in officer's profile");
            }
            
            // Clean up officer registration
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 12: Project Detail Access for HDB Officer
     * Expected: Officers can always access full project details, even when visibility is turned off
     */
    @Test
    public void testCase12_ProjectDetailAccessForHDBOfficer() {
        String testName = "Project Detail Access for HDB Officer";
        System.out.println("\nTest Case 12: " + testName);
        
        try {
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to work with
            List<Project> projects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Toggle project visibility off
            boolean toggleResult = projectController.toggleProjectVisibility(
                (HDBManager)manager, projectName, false);
            
            assertTrue("Manager should be able to toggle project visibility", toggleResult);
            
            // Check if officer can still access project details
            Project officerProject = projectController.getProjectDetails(
                (HDBOfficer)officer, projectName);
            
            assertNotNull("Officer should be able to access project details when visibility is off", officerProject);
            
            if (officerProject != null) {
                printPass(testName, "Officer can access project details when visibility is off");
            } else {
                printFail(testName, "Officer cannot access project details when visibility is off");
            }
            
            // Toggle visibility back on for other tests
            toggleResult = projectController.toggleProjectVisibility((HDBManager)manager, projectName, true);
            assertTrue("Manager should be able to toggle project visibility back on", toggleResult);
            
            // Clean up officer registration
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 13: Restriction on Editing Project Details
     * Expected: Edit functionality is disabled or absent for HDB Officers
     */
    @Test
    public void testCase13_RestrictionOnEditingProjectDetails() {
        String testName = "Restriction on Editing Project Details";
        System.out.println("\nTest Case 13: " + testName);
        
        try {
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to work with
            List<Project> projects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Try to edit project details (should fail)
            String newNeighborhood = "Tampines";
            boolean editResult = projectController.editProject(
                (HDBOfficer)officer, projectName, "neighborhood", newNeighborhood);
            
            assertFalse("System should prevent officer from editing project details", editResult);
            
            if (!editResult) {
                printPass(testName, "System prevented officer from editing project details");
            } else {
                printFail(testName, "System incorrectly allowed officer to edit project details");
                
                // Revert the change made by officer
                projectController.editProject((HDBManager)manager, projectName, 
                    "neighborhood", project.getNeighborhood());
            }
            
            // Clean up officer registration
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 14: Response to Project Enquiries
     * Expected: Officers & Managers can access and respond to enquiries efficiently
     */
    @Test
    public void testCase14_ResponseToProjectEnquiries() {
        String testName = "Response to Project Enquiries";
        System.out.println("\nTest Case 14: " + testName);
        
        try {
            // Login as applicant to create enquiry
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to work with
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Submit enquiry as applicant
            String enquiryContent = "Test enquiry for officer response";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            // Officer views project enquiries
            List<Enquiry> officerEnquiries = enquiryController.getProjectEnquiries(
                (HDBOfficer)officer, projectName);
            
            boolean enquiryVisibleToOfficer = officerEnquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId);
            
            assertTrue("Officer should be able to view project enquiries", enquiryVisibleToOfficer);
            
            if (enquiryVisibleToOfficer) {
                printPass(testName, "Officer can view project enquiries");
            } else {
                printFail(testName, "Officer cannot view project enquiries");
                
                // Clean up
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                projectController.removeOfficerFromProject((HDBManager)manager, 
                    projectName, ((HDBOfficer)officer).getNRIC());
                return;
            }
            
            // Officer responds to enquiry
            String responseContent = "Test response from officer";
            boolean responseResult = enquiryController.respondToEnquiry(
                (HDBOfficer)officer, enquiryId, responseContent);
            
            assertTrue("Officer should be able to respond to enquiry", responseResult);
            
            if (responseResult) {
                printPass(testName, "Officer successfully responded to enquiry");
            } else {
                printFail(testName, "Officer could not respond to enquiry");
                
                // Clean up
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                projectController.removeOfficerFromProject((HDBManager)manager, 
                    projectName, ((HDBOfficer)officer).getNRIC());
                return;
            }
            
            // Applicant views response
            var enquiryWithResponse = enquiryController.getEnquiry((Applicant)applicant, enquiryId);
            
            assertNotNull("Should be able to retrieve enquiry with response", enquiryWithResponse);
            assertNotNull("Enquiry should have a response", enquiryWithResponse.getResponse());
            assertEquals("Response should match officer's response", responseContent, enquiryWithResponse.getResponse());
            
            if (enquiryWithResponse != null && 
                enquiryWithResponse.getResponse() != null && 
                enquiryWithResponse.getResponse().equals(responseContent)) {
                printPass(testName, "Applicant can view officer's response");
            } else {
                printFail(testName, "Applicant cannot view officer's response");
            }
            
            // Clean up
            boolean deleteResult = enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
            assertTrue("Should be able to delete enquiry", deleteResult);
            
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 15: Flat Selection and Booking Management
     * Expected: Officers retrieve the correct application, update flat availability accurately, 
     * and correctly log booking details in the applicant's profile
     */
    @Test
    public void testCase15_FlatSelectionAndBookingManagement() {
        String testName = "Flat Selection and Booking Management";
        System.out.println("\nTest Case 15: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to work with
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Apply for project as applicant
            boolean applyResult = applicationController.applyForProject(
                (Applicant)applicant, projectName, flatType);
            
            assertTrue("Applicant should be able to apply for project", applyResult);
            
            // Manager approves application
            var application = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve application", application);
            
            boolean approveApplicationResult = applicationController.approveApplication(
                (HDBManager)manager, application.getApplicationId());
            
            assertTrue("Manager should be able to approve application", approveApplicationResult);
            
            // Get initial flat count
            int initialFlatCount = projectController.getRemainingUnits(projectName, flatType);
            
            // Officer books flat for applicant
            boolean bookResult = applicationController.bookFlat(
                (HDBOfficer)officer, ((Applicant)applicant).getNRIC(), projectName, flatType);
            
            assertTrue("Officer should be able to book flat", bookResult);
            
            // Verify flat count decreased
            int newFlatCount = projectController.getRemainingUnits(projectName, flatType);
            assertEquals("Flat count should decrease by 1 after booking", initialFlatCount - 1, newFlatCount);
            
            if (newFlatCount == initialFlatCount - 1) {
                printPass(testName, "Flat count correctly decreased after booking");
            } else {
                printFail(testName, "Flat count not correctly updated after booking");
            }
            
            // Verify application status updated to "Booked"
            var updatedApplication = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve updated application", updatedApplication);
            assertEquals("Application status should be updated to Booked", "Booked", updatedApplication.getStatus());
            
            if (updatedApplication != null && updatedApplication.getStatus().equalsIgnoreCase("Booked")) {
                printPass(testName, "Application status correctly updated to Booked");
            } else {
                printFail(testName, "Application status not correctly updated to Booked");
            }
            
            // Verify applicant's profile updated with flat type
            var applicantProfile = applicationController.getApplicantProfile((Applicant)applicant);
            assertNotNull("Should be able to retrieve applicant profile", applicantProfile);
            assertNotNull("Profile should have booked flat type", applicantProfile.getBookedFlatType());
            assertEquals("Booked flat type should match", flatType, applicantProfile.getBookedFlatType());
            
            if (applicantProfile != null && 
                applicantProfile.getBookedFlatType() != null && 
                applicantProfile.getBookedFlatType().equals(flatType)) {
                printPass(testName, "Applicant profile correctly updated with flat type");
            } else {
                printFail(testName, "Applicant profile not correctly updated with flat type");
            }
            
            // Clean up
            boolean withdrawResult = applicationController.withdrawApplication((Applicant)applicant);
            assertTrue("Should be able to withdraw application", withdrawResult);
            
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 16: Receipt Generation for Flat Booking
     * Expected: Accurate and complete receipts are generated for each successful booking
     */
    @Test
    public void testCase16_ReceiptGenerationForFlatBooking() {
        String testName = "Receipt Generation for Flat Booking";
        System.out.println("\nTest Case 16: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Get project to work with
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Apply for project as applicant
            boolean applyResult = applicationController.applyForProject(
                (Applicant)applicant, projectName, flatType);
            
            assertTrue("Applicant should be able to apply for project", applyResult);
            
            // Manager approves application
            var application = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve application", application);
            
            boolean approveApplicationResult = applicationController.approveApplication(
                (HDBManager)manager, application.getApplicationId());
            
            assertTrue("Manager should be able to approve application", approveApplicationResult);
            
            // Officer books flat for applicant
            boolean bookResult = applicationController.bookFlat(
                (HDBOfficer)officer, ((Applicant)applicant).getNRIC(), projectName, flatType);
            
            assertTrue("Officer should be able to book flat", bookResult);
            
            // Generate receipt
            String receipt = applicationController.generateReceipt(
                (HDBOfficer)officer, ((Applicant)applicant).getNRIC());
            
            assertNotNull("Should be able to generate receipt", receipt);
            assertFalse("Receipt should not be empty", receipt.isEmpty());
            
            if (receipt != null && !receipt.isEmpty()) {
                printPass(testName, "Receipt successfully generated");
                
                // Verify receipt content includes all required details
                boolean hasApplicantName = receipt.contains("John");
                boolean hasNRIC = receipt.contains("S1234567A");
                boolean hasAge = receipt.contains("35");
                boolean hasMaritalStatus = receipt.contains("Single");
                boolean hasFlatType = receipt.contains("2-Room");
                boolean hasProjectName = receipt.contains(projectName);
                
                assertTrue("Receipt should contain applicant name", hasApplicantName);
                assertTrue("Receipt should contain NRIC", hasNRIC);
                assertTrue("Receipt should contain age", hasAge);
                assertTrue("Receipt should contain marital status", hasMaritalStatus);
                assertTrue("Receipt should contain flat type", hasFlatType);
                assertTrue("Receipt should contain project name", hasProjectName);
                
                if (hasApplicantName && hasNRIC && hasAge && hasMaritalStatus && 
                    hasFlatType && hasProjectName) {
                    printPass(testName, "Receipt contains all required details");
                } else {
                    printFail(testName, "Receipt missing some required details");
                }
            } else {
                printFail(testName, "Could not generate receipt");
            }
            
            // Clean up
            boolean withdrawResult = applicationController.withdrawApplication((Applicant)applicant);
            assertTrue("Should be able to withdraw application", withdrawResult);
            
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
}