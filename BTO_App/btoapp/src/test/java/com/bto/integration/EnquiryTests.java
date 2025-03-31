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
 * Test cases for Enquiry functionality
 * This tests some aspects of enquiry handling across all user types
 */
public class EnquiryTests extends BaseTest {
    
    /**
     * Test enquiry submission functionality
     */
    @Test
    public void testEnquirySubmission() {
        String testName = "Enquiry Submission";
        System.out.println("\nTest: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Get project for enquiry
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for enquiry", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Submit enquiry
            String enquiryContent = "Test enquiry for submission testing";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            if (enquiryId > 0) {
                printPass(testName, "Successfully submitted enquiry");
                
                // Clean up
                boolean deleteResult = enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                assertTrue("Should be able to delete enquiry", deleteResult);
            } else {
                printFail(testName, "Could not submit enquiry");
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test enquiry viewing functionality
     */
    @Test
    public void testEnquiryViewing() {
        String testName = "Enquiry Viewing";
        System.out.println("\nTest: " + testName);
        
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
            
            // Get project for enquiry
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for enquiry", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Submit enquiry
            String enquiryContent = "Test enquiry for viewing testing";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            // Test applicant viewing their own enquiry
            List<Enquiry> applicantEnquiries = enquiryController.getEnquiriesByUser((Applicant)applicant);
            boolean applicantCanView = applicantEnquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId);
            
            assertTrue("Applicant should be able to view their own enquiry", applicantCanView);
            
            if (applicantCanView) {
                printPass(testName, "Applicant can view their own enquiry");
            } else {
                printFail(testName, "Applicant cannot view their own enquiry");
            }
            
            // Test officer viewing project enquiries
            List<Enquiry> officerEnquiries = enquiryController.getProjectEnquiries(
                (HDBOfficer)officer, projectName);
            
            boolean officerCanView = officerEnquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId);
            
            assertTrue("Officer should be able to view project enquiries", officerCanView);
            
            if (officerCanView) {
                printPass(testName, "Officer can view project enquiries");
            } else {
                printFail(testName, "Officer cannot view project enquiries");
            }
            
            // Test manager viewing all enquiries
            List<Enquiry> managerEnquiries = enquiryController.getAllEnquiries((HDBManager)manager);
            
            boolean managerCanView = managerEnquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId);
            
            assertTrue("Manager should be able to view all enquiries", managerCanView);
            
            if (managerCanView) {
                printPass(testName, "Manager can view all enquiries");
            } else {
                printFail(testName, "Manager cannot view all enquiries");
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
     * Test enquiry response functionality
     */
    @Test
    public void testEnquiryResponding() {
        String testName = "Enquiry Responding";
        System.out.println("\nTest: " + testName);
        
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
            
            // Get project for enquiry
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for enquiry", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Register and approve officer for the project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Submit enquiry
            String enquiryContent = "Test enquiry for response testing";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            // Officer responds to enquiry
            String officerResponse = "Test response from officer";
            boolean officerResponseResult = enquiryController.respondToEnquiry(
                (HDBOfficer)officer, enquiryId, officerResponse);
            
            assertTrue("Officer should be able to respond to enquiry", officerResponseResult);
            
            if (officerResponseResult) {
                printPass(testName, "Officer successfully responded to enquiry");
            } else {
                printFail(testName, "Officer could not respond to enquiry");
            }
            
            // Check if response is visible to applicant
            Enquiry enquiryWithResponse = enquiryController.getEnquiry((Applicant)applicant, enquiryId);
            
            assertNotNull("Should be able to retrieve enquiry with response", enquiryWithResponse);
            assertNotNull("Enquiry should have a response", enquiryWithResponse.getResponse());
            assertEquals("Response should match officer's response", officerResponse, enquiryWithResponse.getResponse());
            
            if (enquiryWithResponse != null && 
                enquiryWithResponse.getResponse() != null && 
                enquiryWithResponse.getResponse().equals(officerResponse)) {
                printPass(testName, "Applicant can view officer's response");
            } else {
                printFail(testName, "Applicant cannot view officer's response");
            }
            
            // Manager responds to enquiry
            String managerResponse = "Test response from manager";
            boolean managerResponseResult = enquiryController.respondToEnquiry(
                (HDBManager)manager, enquiryId, managerResponse);
            
            assertTrue("Manager should be able to respond to enquiry", managerResponseResult);
            
            if (managerResponseResult) {
                printPass(testName, "Manager successfully responded to enquiry");
            } else {
                printFail(testName, "Manager could not respond to enquiry");
            }
            
            // Check if manager's response overwrites officer's response
            enquiryWithResponse = enquiryController.getEnquiry((Applicant)applicant, enquiryId);
            
            assertNotNull("Should be able to retrieve enquiry with response", enquiryWithResponse);
            assertNotNull("Enquiry should have a response", enquiryWithResponse.getResponse());
            assertEquals("Response should match manager's response", managerResponse, enquiryWithResponse.getResponse());
            
            if (enquiryWithResponse != null && 
                enquiryWithResponse.getResponse() != null && 
                enquiryWithResponse.getResponse().equals(managerResponse)) {
                printPass(testName, "Manager's response correctly overwrites officer's response");
            } else {
                printFail(testName, "Manager's response did not correctly overwrite officer's response");
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
     * Test enquiry editing functionality
     */
    @Test
    public void testEnquiryEditing() {
        String testName = "Enquiry Editing";
        System.out.println("\nTest: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Get project for enquiry
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for enquiry", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Submit enquiry
            String enquiryContent = "Test enquiry for editing testing";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            // Edit enquiry
            String updatedContent = "Updated content for editing test";
            boolean editResult = enquiryController.editEnquiry(
                (Applicant)applicant, enquiryId, updatedContent);
            
            assertTrue("Should be able to edit enquiry", editResult);
            
            if (editResult) {
                printPass(testName, "Successfully edited enquiry");
            } else {
                printFail(testName, "Could not edit enquiry");
                
                // Clean up
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                return;
            }
            
            // Verify edit
            Enquiry updatedEnquiry = enquiryController.getEnquiry((Applicant)applicant, enquiryId);
            
            assertNotNull("Should be able to retrieve edited enquiry", updatedEnquiry);
            assertEquals("Enquiry content should match updated content", updatedContent, updatedEnquiry.getContent());
            
            if (updatedEnquiry != null && updatedEnquiry.getContent().equals(updatedContent)) {
                printPass(testName, "Enquiry content correctly updated");
            } else {
                printFail(testName, "Enquiry content not correctly updated");
            }
            
            // Clean up
            boolean deleteResult = enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
            assertTrue("Should be able to delete enquiry", deleteResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test enquiry deletion functionality
     */
    @Test
    public void testEnquiryDeletion() {
        String testName = "Enquiry Deletion";
        System.out.println("\nTest: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Get project for enquiry
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for enquiry", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Submit enquiry
            String enquiryContent = "Test enquiry for deletion testing";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            
            // Delete enquiry
            boolean deleteResult = enquiryController.deleteEnquiry(
                (Applicant)applicant, enquiryId);
            
            assertTrue("Should be able to delete enquiry", deleteResult);
            
            if (deleteResult) {
                printPass(testName, "Successfully deleted enquiry");
            } else {
                printFail(testName, "Could not delete enquiry");
                
                // Try to clean up anyway
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                return;
            }
            
            // Verify deletion
            Enquiry deletedEnquiry = enquiryController.getEnquiry((Applicant)applicant, enquiryId);
            
            assertNull("Deleted enquiry should not be found", deletedEnquiry);
            
            if (deletedEnquiry == null) {
                printPass(testName, "Enquiry correctly removed from system");
            } else {
                printFail(testName, "Enquiry still exists after deletion");
                
                // Try to clean up anyway
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
}