package com.bto.integration;

import com.bto.model.User;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Application;
import com.bto.model.Project;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for application functionality
 * Covers test cases 8-9 from Appendix A
 */
public class ApplicationTests extends BaseTest {
    
    /**
     * Test Case 8: Single Flat Booking per Successful Application
     * Expected: System allows booking one flat and restricts further bookings
     */
    @Test
    public void testCase8_SingleFlatBookingPerSuccessfulApplication() {
        String testName = "Single Flat Booking per Successful Application";
        System.out.println("\nTest Case 8: " + testName);
        
        try {
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Login as manager to approve applications
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Login as officer to help with booking
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Get project to apply for
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for application", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Apply for project
            boolean applyResult = applicationController.applyForProject(
                (Applicant)applicant, projectName, flatType);
            
            assertTrue("Should be able to apply for project", applyResult);
            
            // Manager approves application
            var application = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve application", application);
            
            boolean approveResult = applicationController.approveApplication(
                (HDBManager)manager, application.getApplicationId());
            
            assertTrue("Manager should be able to approve application", approveResult);
            printPass(testName, "Application successfully approved");
            
            // Officer books flat for applicant
            boolean bookResult = applicationController.bookFlat(
                (HDBOfficer)officer, ((Applicant)applicant).getNRIC(), projectName, flatType);
            
            assertTrue("Officer should be able to book flat", bookResult);
            printPass(testName, "Flat successfully booked");
            
            // Try to book another flat (should fail)
            boolean secondBookResult = applicationController.bookFlat(
                (HDBOfficer)officer, ((Applicant)applicant).getNRIC(), projectName, flatType);
            
            assertFalse("System should prevent booking a second flat", secondBookResult);
            
            if (!secondBookResult) {
                printPass(testName, "System prevented booking a second flat");
            } else {
                printFail(testName, "System incorrectly allowed booking a second flat");
            }
            
            // Try to apply for another project (should fail)
            if (projects.size() > 1) {
                Project anotherProject = projects.get(1);
                boolean secondApplyResult = applicationController.applyForProject(
                    (Applicant)applicant, anotherProject.getProjectName(), flatType);
                
                assertFalse("System should prevent applying for a second project", secondApplyResult);
                
                if (!secondApplyResult) {
                    printPass(testName, "System prevented applying for a second project");
                } else {
                    printFail(testName, "System incorrectly allowed applying for a second project");
                    
                    // Clean up second application
                    applicationController.withdrawApplication((Applicant)applicant);
                }
            }
            
            // Clean up application
            boolean withdrawResult = applicationController.withdrawApplication((Applicant)applicant);
            assertTrue("Should be able to withdraw application", withdrawResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 9: Applicant's enquiries management
     * Expected: Enquiries can be successfully submitted, displayed, modified, and removed
     */
    @Test
    public void testCase9_ApplicantEnquiriesManagement() {
        String testName = "Applicant's Enquiries Management";
        System.out.println("\nTest Case 9: " + testName);
        
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
            String enquiryContent = "Test enquiry about project availability";
            int enquiryId = enquiryController.submitEnquiry(
                (Applicant)applicant, projectName, enquiryContent);
            
            assertTrue("Should get a valid enquiry ID", enquiryId > 0);
            printPass(testName, "Enquiry successfully submitted");
            
            // View enquiries
            var enquiries = enquiryController.getEnquiriesByUser((Applicant)applicant);
            boolean enquiryFound = enquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId && e.getContent().equals(enquiryContent));
            
            assertTrue("Should be able to find submitted enquiry", enquiryFound);
            
            if (enquiryFound) {
                printPass(testName, "Enquiry successfully retrieved");
            } else {
                printFail(testName, "Submitted enquiry not found");
                return;
            }
            
            // Edit enquiry
            String updatedContent = "Updated test enquiry content";
            boolean editResult = enquiryController.editEnquiry(
                (Applicant)applicant, enquiryId, updatedContent);
            
            assertTrue("Should be able to edit enquiry", editResult);
            
            if (editResult) {
                printPass(testName, "Enquiry successfully edited");
            } else {
                printFail(testName, "Could not edit enquiry");
                
                // Clean up
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
                return;
            }
            
            // Verify edit
            enquiries = enquiryController.getEnquiriesByUser((Applicant)applicant);
            boolean editVerified = enquiries.stream()
                .anyMatch(e -> e.getEnquiryId() == enquiryId && e.getContent().equals(updatedContent));
            
            assertTrue("Should find enquiry with updated content", editVerified);
            
            if (editVerified) {
                printPass(testName, "Enquiry edit verified");
            } else {
                printFail(testName, "Enquiry edit not reflected");
            }
            
            // Delete enquiry
            boolean deleteResult = enquiryController.deleteEnquiry(
                (Applicant)applicant, enquiryId);
            
            assertTrue("Should be able to delete enquiry", deleteResult);
            
            if (deleteResult) {
                printPass(testName, "Enquiry successfully deleted");
            } else {
                printFail(testName, "Could not delete enquiry");
            }
            
            // Verify deletion
            enquiries = enquiryController.getEnquiriesByUser((Applicant)applicant);
            boolean deletionVerified = enquiries.stream()
                .noneMatch(e -> e.getEnquiryId() == enquiryId);
            
            assertTrue("Should not find deleted enquiry", deletionVerified);
            
            if (deletionVerified) {
                printPass(testName, "Enquiry deletion verified");
            } else {
                printFail(testName, "Enquiry still exists after deletion");
                
                // Clean up if still exists
                enquiryController.deleteEnquiry((Applicant)applicant, enquiryId);
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
}